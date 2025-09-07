package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.HttpClientService
import com.papaya.design.platform.ai.extractImageInB64
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Profile("test-tg-bot")
@Service
class MockImageService(
    private val objectMapper: ObjectMapper,
) : AiImageService {
    override suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        images: List<ByteArray>,
        callback: (List<String>) -> Unit
    ) {
        val responseFromAi = Thread.currentThread().contextClassLoader.getResource("mock_result_b64.json").readText()

        callback.invoke(listOf(objectMapper.extractImageInB64(responseFromAi)))
    }
}
