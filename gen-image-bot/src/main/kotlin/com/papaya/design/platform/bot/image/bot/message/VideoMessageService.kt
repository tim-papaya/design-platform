package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.network.bimap
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.AiVideoService
import com.papaya.design.platform.ai.GenerationParameters
import com.papaya.design.platform.ai.kling.KlingAiVideoService
import com.papaya.design.platform.ai.openai.MODERATION_BLOCKED_CODE
import com.papaya.design.platform.bot.image.bot.domain.UserState.WAITING_FOR_END_OF_PHOTO_GENERATION
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.user.UserService
import com.papaya.design.platform.bot.image.bot.log.TracingService
import com.papaya.design.platform.ai.openai.OpenAiImageService
import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.ai.photo.PhotoWithContent
import com.papaya.design.platform.bot.image.bot.image.downloadImageAsBytes
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.video.VideoMetadataExtractor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

const val GENERATION_COUNT_FOR_VIDEO = 1

private val log = KotlinLogging.logger { }

private const val TELEGRAM_IMAGE_URL = "https://api.telegram.org/file/bot"

@Lazy
@Service
class VideoMessageService(
    private val userService: UserService,
    private val messageService: MessageService,
    private val tracingService: TracingService,
    private val bot: Bot,
    private val aiVideoService: AiVideoService,
    @Value("\${telegram.api-key}")
    private val apiKey: String,
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun handleVideoMessage(
        id: TelegramId,
        userPrompt: String,
        inputReferenceJpeg: Photo,
        systemPrompt: String,
    ) {
        try {
            userService.saveUser(id) { it.userState = WAITING_FOR_END_OF_PHOTO_GENERATION }

            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = "🎬 ${General.Text.IMAGE_RECEIVED_FOR_GENERATION}",
                replyMarkup = removeKeyboard()
            )

            GlobalScope.launch {
                try {
                    log.info { "Will use input image for video generation" }
                    val downloadedPhoto =
                        bot.getFile(inputReferenceJpeg.fileId).bimap({ file ->
                            val fileUrl = "$TELEGRAM_IMAGE_URL$apiKey/${file?.result?.filePath}"
                            val imageBytes = downloadImageAsBytes(fileUrl)
                            log.info("Received photo from Telegram chat size - ${imageBytes.size}")
                            PhotoWithContent(inputReferenceJpeg, imageBytes)
                        }, { error ->
                            messageService.sendErrorMessage(id, "Error getting file: ${error.errorBody}")
                            null
                        })
                    if (downloadedPhoto == null) throw IllegalStateException("Can not download reference photo for video generation")

                    GlobalScope.launch { logDownloadedPhoto(userPrompt, id, downloadedPhoto) }

                    aiVideoService.generateVideo(
                        systemPrompt = systemPrompt,
                        userPrompt = userPrompt,
                        inputReferenceJpeg = downloadedPhoto,
                        generationParameters = GenerationParameters(),
                        onError = { message: String ->
                            if (message.contains(MODERATION_BLOCKED_CODE)) {
                                messageService.sendErrorMessage(
                                    id,
                                    message,
                                    Error.Text.MODERATION_ERROR_ON_PROCESSING_VIDEO
                                )
                            } else {
                                messageService.sendErrorMessage(id, message, Error.Text.ERROR_ON_PROCESSING_VIDEO)
                            }
                        }
                    ) {
                        sendGeneratedVideo(id, it)
                    }

                } catch (e: Exception) {
                    messageService.sendErrorMessage(id, Error.Text.ERROR_ON_PROCESSING_VIDEO, e)
                }
            }
        } catch (e: Exception) {
            messageService.sendErrorMessage(id, Error.Text.ERROR_ON_PROCESSING_VIDEO, e)
        }
    }

    private fun logDownloadedPhoto(
        userPrompt: String,
        id: TelegramId,
        dowloadedPhoto: PhotoWithContent
    ) {
        if (!(userPrompt.trim().isEmpty()))
            tracingService.logPrompt(id.chatId, userPrompt)
        tracingService.logImage(id.chatId, dowloadedPhoto.bytes, "jpeg")
    }

    private fun sendGeneratedVideo(id: TelegramId, videoBytes: ByteArray) {
        val dimensions = VideoMetadataExtractor.extractDimensions(videoBytes)
        val result = bot.sendVideo(
            chatId = ChatId.fromId(id.chatId),
            video = TelegramFile.ByByteArray(
                fileBytes = videoBytes,
                filename = "generated_video_${System.currentTimeMillis()}.mp4"
            ),
            width = dimensions?.width,
            height = dimensions?.height,
            caption = "✅ Видео сгенерировано",
        )
        result.fold(
            {
                messageService.sendGenerationCompletionMessage(
                    id, "Successfully sent generated video to user",
                    GENERATION_COUNT_FOR_VIDEO
                )
            },
            { error -> messageService.sendErrorMessage(id, "Error sending generated video: ${error.errorBody}") }
        )
    }
}
