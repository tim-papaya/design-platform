package com.papaya.design.platform.ai

import com.papaya.design.platform.ai.photo.PhotoWithContent

interface AiImageService {
    suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        images: List<PhotoWithContent>,
        callback: (base64Images: List<String>) -> Unit
    )
}
