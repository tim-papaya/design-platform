package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.DelegateAiImageService
import com.papaya.design.platform.ai.extractImageInB64
import com.papaya.design.platform.ai.photo.PhotoWithContent
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("test")
@Service
@DelegateAiImageService
class MockImageService(
    private val objectMapper: ObjectMapper,
) : AiImageService {
    override suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        model: String?,
        images: List<PhotoWithContent>,
        callback: (List<String>) -> Unit
    ) {
        val responseFromAi = Thread.currentThread().contextClassLoader.getResource("mock_result_b64.json").readText()

        callback.invoke(listOf(objectMapper.extractImageInB64(responseFromAi)))
    }
}
