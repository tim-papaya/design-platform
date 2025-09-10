package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.network.bimap
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.bot.image.bot.domain.Photo
import com.papaya.design.platform.bot.image.bot.domain.UserState.WAITING_FOR_END_OF_PHOTO_GENERATION
import com.papaya.design.platform.bot.image.bot.image.downloadImageAsBytes
import com.papaya.design.platform.bot.image.bot.log.TracingService
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.user.UserService
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
    @Value("\${com.papaya.design.platform.bot.image.max.image.number:8}")
    private val maxNumberOfPhotos: Int,
    private val messageService: MessageService,
    private val tracingService: TracingService,
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun handlePhotoMessage(
        bot: Bot,
        id: TelegramId,
        photos: List<Photo>,
        commandState: StartGenerationOfImage,
        userPrompt: String? = null,
    ) {
        try {
            userService.getUser(id.userId).userState = WAITING_FOR_END_OF_PHOTO_GENERATION
            userService.saveUser(id.userId)

            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = General.Text.IMAGE_RECEIVED_FOR_GENERATION,
                replyMarkup = removeKeyboard()
            )
            GlobalScope.launch {
                try {
                    log.info {"Will use ${photos.size} input images for generation"}
                    val resultPhotos = photos
                        .take(maxNumberOfPhotos)
                        .mapNotNull { currentPhoto ->
                            bot.getFile(currentPhoto.fileId).bimap({ file ->
                                val fileUrl = "$TELEGRAM_IMAGE_URL$apiKey/${file?.result?.filePath}"
                                val imageBytes = downloadImageAsBytes(fileUrl)
                                log.info("Received photo from Telegram chat size - ${imageBytes.size}")
                                imageBytes
                            }, { error ->
                                messageService.sendErrorMessage(bot, id, "Error getting file: ${error.errorBody}")
                                null
                            })
                        }
                    // TODO Remove tracing
                    GlobalScope.launch {
                        if (!(userPrompt?.trim().isNullOrEmpty()))
                            tracingService.logPrompt(id.chatId, userPrompt!!)
                        resultPhotos.forEach { tracingService.logImage(id.chatId, it, "jpeg") }
                    }

                    aiImageService.generateImage(
                        userPrompt = userPrompt,
                        systemPrompt = commandState.systemPrompt,
                        images = resultPhotos,
                        qualityPreset = userService.getUser(id.userId).qualityPreset
                    ) { base64Images ->
                        val imageArray = base64Images
                            .map { getDecoder().decode(it) }

                        // TODO Remove tracing
                        GlobalScope.launch {
                            imageArray.forEach { tracingService.logResultImage(id.chatId, it, "png") }
                        }
                        log.info("Generated ${imageArray.size} images as output")
                        sendGeneratedImage(bot, id, imageArray.first())

                        if (imageArray.size > 1) {
                            log.error("To many output images")
                        }
                    }
                } catch (e: Exception) {
                    messageService.sendErrorMessage(bot, id, "Error handling photo message", e)
                }
            }

        } catch (e: Exception) {
            messageService.sendErrorMessage(bot, id, "Error handling photo message", e)
        }
    }

    private fun sendGeneratedImage(
        bot: Bot,
        id: TelegramId,
        imageBytes: ByteArray
    ) {
        try {
            val result = bot.sendPhoto(
                chatId = ChatId.fromId(id.chatId),
                photo = TelegramFile.ByByteArray(
                    fileBytes = imageBytes,
                    filename = "generated_interior_${System.currentTimeMillis()}.png"
                ),
                caption = General.Text.IMAGE_GENERATED
            )

            result.fold({
                messageService.sendGenerationCompletionMessage(bot, id, "Successfully sent generated image to user")
            }, { error ->
                messageService.sendErrorMessage(bot, id, "Error sending generated image: ${error.errorBody}")
            })

        } catch (e: Exception) {
            messageService.sendErrorMessage(bot, id, "Error sending generated image", e)
        }
    }
}