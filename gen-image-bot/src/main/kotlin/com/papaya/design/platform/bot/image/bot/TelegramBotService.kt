package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.AiImageService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.util.Base64.getDecoder
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger { }

private const val START_CMD = "start"
private const val REAL_IMAGE_CMD = "image"
private const val HELLO_CMD = "hello"

@Service
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    @Autowired
    private val aiImageService: AiImageService
) : BotService {

    // Хранение состояния пользователей, ожидающих загрузки изображения
    private val usersWaitingForImage = ConcurrentHashMap<Long, Boolean>()

    // Хранение информации о том, какие пользователи уже получили приветствие
    private val welcomedUsers = ConcurrentHashMap<Long, Boolean>()

    /**
     * Создает клавиатуру с основными командами
     */
    private fun createMainKeyboard(): KeyboardReplyMarkup {
        return KeyboardReplyMarkup(
            keyboard = listOf(
                listOf(
                    KeyboardButton("🖼️ Генерация интерьера"),
                    KeyboardButton("👋 Привет")
                )
            ),
            resizeKeyboard = true,
            oneTimeKeyboard = false
        )
    }
    private fun welcomeKeyboard(): KeyboardReplyMarkup {
        return KeyboardReplyMarkup(
            keyboard = listOf(
                listOf(
                    KeyboardButton("✨ Начать"),
                )
            ),
            resizeKeyboard = true,
            oneTimeKeyboard = false
        )
    }

    /**
     * Создает клавиатуру для удаления кнопок
     */
    private fun removeKeyboard(): ReplyKeyboardRemove {
        return ReplyKeyboardRemove(removeKeyboard = true)
    }

    /**
     * Отправляет первое приветствие новому пользователю
     */
    private fun sendFirstTimeWelcome(chatId: Long) {
        val welcomeResult = bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = """
                Roomio by DIROMANOVA – интерьер вашей мечты за пару минут!
                Забудьте про долгие рендеры и дорогие визуализации:
                ✨	Хотите реалистичную 3D-визуализацию по вашему коллажу или мудборду?
                🏠	Нужно обновить интерьер по вашему фото или описанию?
                🎨	Или готовы целиком довериться ИИ, который создаст стильный и уютный интерьер для жизни или продажи?
                 Просто загрузите фото, коллаж или напишите идею – дальше поработает Roomio. 
                 За вдохновением и работами: @Roomio_DIROMANOVA
                 Ваши идеи. Ваш дизайн. Ваше «я дома». 
                 Нажмите /start - создадим ваш интерьер мечты вместе.
            """.trimIndent(),
            replyMarkup = welcomeKeyboard()
        )

        welcomeResult.fold({
            welcomedUsers[chatId] = true
            log.info("First-time welcome sent to user $chatId")
        }, { e ->
            log.error("Error sending first-time welcome to user $chatId: $e")
        })
    }

    private val bot = bot {
        token = apiKey
        dispatch {
            command(START_CMD) {
                val chatId = message.chat.id

                // Отмечаем пользователя как приветствованного
                welcomedUsers[chatId] = true

                // Отправляем приветственное сообщение
                val welcomeResult = bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "🏠 Добро пожаловать в бот генерации реалистичных интерьеров!"
                )

                welcomeResult.fold({
                    // После успешного приветствия показываем меню
                    val menuResult = bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Выберите действие:",
                        replyMarkup = createMainKeyboard()
                    )
                    menuResult.fold({
                        log.info("Start command processed for user $chatId")
                    }, { e ->
                        log.error("Error showing menu in $START_CMD command: $e")
                    })
                }, { e ->
                    log.error("Error in $START_CMD command: $e")
                })
            }
            command(HELLO_CMD) {
                val chatId = message.chat.id

                // Отмечаем пользователя как приветствованного
                welcomedUsers[chatId] = true

                val result = bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Привет! Выберите действие:",
                    replyMarkup = createMainKeyboard()
                )
                result.fold({
                    // do nothing
                }, { e ->
                    log.info("Error in $HELLO_CMD command")
                })
            }
            command(REAL_IMAGE_CMD) {
                val chatId = message.chat.id

                // Отмечаем пользователя как приветствованного
                welcomedUsers[chatId] = true
                usersWaitingForImage[chatId] = true

                val result = bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Начнем генерацию реалистичного интерьера. Пожалуйста, пришлите изображение.",
                    replyMarkup = removeKeyboard()
                )
                result.fold({
                    log.info("User $chatId is now waiting for image")
                }, { e ->
                    log.error("Error in $REAL_IMAGE_CMD command: $e")
                    usersWaitingForImage.remove(chatId)
                })
            }

            message {
                val chatId = message.chat.id
                val messageText = message.text

                // Проверяем, первый ли раз пользователь взаимодействует с ботом
                if (welcomedUsers[chatId] != true) {
                    sendFirstTimeWelcome(chatId)
                    return@message
                }

                // Проверяем, ожидает ли пользователь загрузки изображения
                if (usersWaitingForImage[chatId] == true && message.photo != null) {
                    handlePhotoMessage(chatId, message.photo!!)
                } else if (usersWaitingForImage[chatId] == true) {
                    // Пользователь прислал не фото
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Пожалуйста, пришлите изображение для генерации реалистичного интерьера."
                    )
                } else {
                    // Обрабатываем нажатия кнопок
                    when (messageText) {
                        "👋 Привет" -> {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Привет! Выберите действие:",
                                replyMarkup = createMainKeyboard()
                            )
                        }

                        "✨ Начать" -> {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Добро пожаловать! Выберите действие:",
                                replyMarkup = createMainKeyboard()
                            )
                        }

                        "🖼️ Генерация интерьера" -> {
                            usersWaitingForImage[chatId] = true
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Начнем генерацию реалистичного интерьера. Пожалуйста, пришлите изображение.",
                                replyMarkup = removeKeyboard()
                            )
                        }
                    }
                }
            }

        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }

    private fun handlePhotoMessage(chatId: Long, photos: List<PhotoSize>) {
        try {
            usersWaitingForImage.remove(chatId)

            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Получил изображение! Начинаю генерацию реалистичного интерьера..."
            )

            val firstPhoto = photos.first()


            // Получаем файл
            val fileResult = bot.getFile(firstPhoto.fileId)

            fileResult.fold({ file ->
                val fileUrl = "https://api.telegram.org/file/bot$apiKey/${file?.result?.filePath}"

                // Загружаем изображение и конвертируем в ByteArray
                val imageBytes = downloadImageAsBytes(fileUrl)

                // Вызываем AI сервис для генерации
                runBlocking {
                    aiImageService.generateImage(
                        userPrompt = null,
                        images = arrayOf(imageBytes)
                    ) { base64Image ->
                        // Отправляем результат пользователю
                        sendGeneratedImage(chatId, base64Image)
                    }
                }

            }, { error ->
                log.error("Error getting file: $error")
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Ошибка при получении файла: $error",
                    replyMarkup = createMainKeyboard()
                )
            })

        } catch (e: Exception) {
            log.error("Error handling photo message: ${e.message}", e)
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Произошла ошибка при обработке изображения: ${e.message}",
                replyMarkup = createMainKeyboard()
            )
        }
    }

    private fun downloadImageAsBytes(url: String): ByteArray {
        return URL(url).readBytes()
    }

    private fun sendGeneratedImage(chatId: Long, base64Image: String) {
        try {
            // Декодируем base64 в ByteArray
            val imageBytes = getDecoder().decode(base64Image)

            // Отправляем фото через Telegram API
            val result = bot.sendPhoto(
                chatId = ChatId.fromId(chatId),
                photo = TelegramFile.ByByteArray(
                    fileBytes = imageBytes,
                    filename = "generated_interior_${System.currentTimeMillis()}.png"
                ),
                caption = "Сгенерированный реалистичный интерьер готов! 🏠✨"
            )

            result.fold({
                log.info("Successfully sent generated image to user $chatId")
                // Показываем кнопки обратно после завершения генерации
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Что делаем дальше?",
                    replyMarkup = createMainKeyboard()
                )
            }, { error ->
                log.error("Error sending generated image: $error")
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Генерация завершена, но произошла ошибка при отправке изображения: $error",
                    replyMarkup = createMainKeyboard()
                )
            })

        } catch (e: Exception) {
            log.error("Error sending generated image: ${e.message}", e)
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Произошла ошибка при отправке сгенерированного изображения: ${e.message}",
                replyMarkup = createMainKeyboard()
            )
        }
    }
}