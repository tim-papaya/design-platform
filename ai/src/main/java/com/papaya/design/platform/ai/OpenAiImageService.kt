package com.papaya.design.platform.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value

class OpenAiImageService(
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
    @Value("\${papaya.design.platfor.ai.image.system-prompt}")
    private val systemPrompt: String =
        "Создай реалистичное изображение на основе примера. Не добавляй никаких дополнительных предметов, кроме тех что есть в примере.",
) : AiImageService {
    override suspend fun generateImage(
        userPrompt: String?,
        vararg images: ByteArray,
        callback: (String) -> Unit
    ) {
        val body = MultipartBody.Builder()
            .addFormDataPart("model", "gpt-image-1")
            .addFormDataPart(
                name = "image",
                filename = "example.png",
                body = images.first().toRequestBody("image/png".toMediaType())
            )
            .addFormDataPart(
                "prompt",
                systemPrompt
            ).build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/edits")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()


    }
}