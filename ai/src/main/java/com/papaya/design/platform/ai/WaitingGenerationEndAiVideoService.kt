package com.papaya.design.platform.ai

import com.papaya.design.platform.ai.photo.PhotoWithContent
import kotlinx.coroutines.delay

private const val MAX_TIMES_TO_DELAY_FOR_VIDEO_GENERATION_END = 5
private const val FIRST_DELAY = 60_000L
private const val STEP_DELAY = 20_000L

abstract class WaitingGenerationEndAiVideoService(
    val audioAdditionService: AudioAdditionService,
) : AiVideoService {
    override suspend fun generateVideo(
        systemPrompt: String?,
        userPrompt: String?,
        inputReferenceJpeg: PhotoWithContent,
        generationParameters: GenerationParameters,
        onError: (message: String) -> Unit,
        callback: (video: ByteArray) -> Unit
    ) {
        try {
            val prompt = buildPrompt(systemPrompt, userPrompt)
            val taskId = createImageToVideoTask(prompt, inputReferenceJpeg, generationParameters, onError)

            delay(FIRST_DELAY)

            val generationSuccessful = waitVideoGenerationEnd(taskId, onError)

            if (!generationSuccessful) {
                onError("Video generation timed out")
                return
            }
            val bytes = downloadVideo(taskId)
            val videWithAudio = audioAdditionService.addAudioToVideo(bytes)
            callback(videWithAudio)
        } catch (e: Exception) {
            onError(e.message ?: "Unexpected error while generating video")
        }
    }

    abstract fun createImageToVideoTask(
        prompt: String,
        inputReferenceJpeg: PhotoWithContent,
        generationParameters: GenerationParameters,
        onError: (message: String) -> Unit
    ): String

    abstract fun downloadVideo(taskId: String): ByteArray

    abstract fun pollVideoGenerationStatus(taskId: String): Boolean

    suspend fun waitVideoGenerationEnd(taskId: String, onError: (String) -> Unit, executedTimes: Int = 1): Boolean {
        if (executedTimes > MAX_TIMES_TO_DELAY_FOR_VIDEO_GENERATION_END) return false
        delay(STEP_DELAY)

        return if (pollVideoGenerationStatus(taskId)) true
        else waitVideoGenerationEnd(taskId, onError, executedTimes + 1)
    }

    fun buildPrompt(systemPrompt: String?, userPrompt: String?): String = when {
        userPrompt.isNullOrBlank() && systemPrompt.isNullOrBlank() -> throw IllegalArgumentException("user and System promts are null")
        !userPrompt.isNullOrBlank() && !systemPrompt.isNullOrBlank() -> "$systemPrompt\n$userPrompt"
        !userPrompt.isNullOrBlank() -> userPrompt
        !systemPrompt.isNullOrBlank() -> systemPrompt
        else -> throw IllegalStateException("Some prompt generation branches not covered")
    }
}
