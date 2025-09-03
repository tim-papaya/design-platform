package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.AiImageService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.util.Base64.getDecoder
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger { }

private const val REAL_IMAGE_CMD = "real-image"
private const val HELLO_CMD = "hello"

@Service
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    @Autowired
    @Qualifier("openAiImageService")
    private val aiImageService: AiImageService
) : BotService {
    
    // Хранение состояния пользователей, ожидающих загрузки изображения
    private val usersWaitingForImage = ConcurrentHashMap<Long, Boolean>()

    private val bot = bot {
        token = apiKey
        dispatch {
            command(HELLO_CMD) {
                val result = bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Привет!")
                result.fold({
                    // do nothing
                }, { e ->
                    log.info("Error in $HELLO_CMD command")
                })
            }
            command(REAL_IMAGE_CMD) {
                val chatId = message.chat.id
                usersWaitingForImage[chatId] = true

                val result = bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Начнем генерацию реалистичного интерьера."
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
                
                // Проверяем, ожидает ли пользователь загрузки изображения
                if (usersWaitingForImage[chatId] == true && message.photo != null) {
                    handlePhotoMessage(chatId, message.photo!!)
                } else if (usersWaitingForImage[chatId] == true) {
                    // Пользователь прислал не фото
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Пожалуйста, пришлите изображение для генерации реалистичного интерьера."
                    )
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
                    text = "Ошибка при получении файла: $error"
                )
            })
            
        } catch (e: Exception) {
            log.error("Error handling photo message: ${e.message}", e)
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Произошла ошибка при обработке изображения: ${e.message}"
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
            }, { error ->
                log.error("Error sending generated image: $error")
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Генерация завершена, но произошла ошибка при отправке изображения: $error"
                )
            })
            
        } catch (e: Exception) {
            log.error("Error sending generated image: ${e.message}", e)
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Произошла ошибка при отправке сгенерированного изображения: ${e.message}"
            )
        }
    }
}