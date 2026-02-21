package com.papaya.design.platform.ai

import com.papaya.design.platform.ai.photo.PhotoWithContent

interface AiImageService {
    suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        model: String? = null,
        quality: AiImageGenerationQuality,
        images: List<PhotoWithContent>,
        callback: (base64Images: List<String>) -> Unit
    )
}
