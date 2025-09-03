package com.papaya.design.platform.ai


interface AiImageService {

    suspend fun generateImage(userPrompt: String?, vararg images: ByteArray, callback: (base64Image: String) -> Unit)
}