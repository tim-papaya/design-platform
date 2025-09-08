package com.papaya.design.platform.ai

import com.papaya.design.platform.ai.openai.OpenAiImageService.QualityPreset

interface AiImageService {

    suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        images: List<ByteArray>,
        qualityPreset: QualityPreset,
        callback: (base64Images: List<String>) -> Unit
    )
}
