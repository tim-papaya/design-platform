package com.papaya.design.platform.ai

interface AiImageService {

    suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        images: List<ByteArray>,
        callback: (base64Images: List<String>) -> Unit
    )
}
