package com.papaya.design.platform.ai

import com.papaya.design.platform.ai.photo.PhotoWithContent

interface AiVideoService {
    suspend fun generateVideo(
        systemPrompt: String?,
        userPrompt: String?,
        inputReferenceJpeg: PhotoWithContent,
        generationParameters: GenerationParameters,
        onError: (message: String) -> Unit,
        callback: (video: ByteArray) -> Unit
    )
}
