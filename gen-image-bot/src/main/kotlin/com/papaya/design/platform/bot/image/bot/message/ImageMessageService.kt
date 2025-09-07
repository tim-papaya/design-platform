package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.bot.image.bot.static.IMAGE_RECEIVED_FOR_GENERATION_TEXT
import com.papaya.design.platform.bot.image.bot.user.UserService
import com.papaya.design.platform.bot.image.bot.domain.UserState.WAITING_FOR_END_OF_PHOTO_GENERATION
import com.papaya.design.platform.bot.image.bot.image.downloadImageAsBytes
import com.papaya.design.platform.bot.image.bot.static.IMAGE_GENERATED_TEXT
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64.getDecoder

private val log = KotlinLogging.logger { }

private const val TELEGRAM_IMAGE_URL = "https://api.telegram.org/file/bot"

@Service
class ImageMessageService(
    private val userService: UserService,
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    private val aiImageService: AiImageService,
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun handlePhotoMessage(
        bot: Bot,
        chatId: Long,
        photos: List<PhotoSize>
    ) {
        try {
            userService.getUser(chatId).userState = WAITING_FOR_END_OF_PHOTO_GENERATION

            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = IMAGE_RECEIVED_FOR_GENERATION_TEXT
            )

            val firstPhoto = photos.first()
            val fileReceivedResult = bot.getFile(firstPhoto.fileId)

            fileReceivedResult.fold({ file ->
                val fileUrl = "$TELEGRAM_IMAGE_URL$apiKey/${file?.result?.filePath}"
                val imageBytes = downloadImageAsBytes(fileUrl)
                log.info("Received photo from Telegram chat")

                GlobalScope.launch {
                    aiImageService.generateImage(
                        userPrompt = null,
                        images = arrayOf(imageBytes),
                    ) { base64Images ->
                        base64Images.forEach {
                            sendGeneratedImage(bot, chatId, it)
                        }
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
                text = "Произошла ошибка при обработке изображения. Пожалуйста, выберите кнопку - Поддержка.",
                replyMarkup = createMainKeyboard()
            )
        }
    }

    private fun sendGeneratedImage(
        bot: Bot,
        chatId: Long,
        base64Image: String
    ) {
        try {
            val imageBytes = getDecoder().decode(base64Image)

            val result = bot.sendPhoto(
                chatId = ChatId.fromId(chatId),
                photo = TelegramFile.ByByteArray(
                    fileBytes = imageBytes,
                    filename = "generated_interior_${System.currentTimeMillis()}.png"
                ),
                caption = IMAGE_GENERATED_TEXT
            )

            result.fold({
                log.info("Successfully sent generated image to user $chatId")
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
                text = "Произошла ошибка при отправке сгенерированного изображения. Пожалуйста, выберите кнопку - Поддержка.",
                replyMarkup = createMainKeyboard()
            )
        }
    }
}