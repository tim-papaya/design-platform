package com.papaya.design.platform.ai

interface AiImageService {

    suspend fun generateImage(
        userPrompt: String?,
        systemPromptVariation: Int,
        vararg images: ByteArray,
        callback: (base64Images: List<String>) -> Unit
    )

    fun variationNumber(): Int
}
