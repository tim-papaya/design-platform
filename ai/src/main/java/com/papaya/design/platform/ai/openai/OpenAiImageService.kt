package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.HttpClientService
import com.papaya.design.platform.ai.extractImagesInB64
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}
private const val API_URL = "https://api.openai.com/v1/images/edits"

@Profile("prod", "single-prompt")
@Service
class OpenAiImageService(
    private val httpClientService: HttpClientService,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.ai.openai.api-key}") private val apiKey: String,
) : AiImageService {

    data class QualityPreset(val quality: String, val fidelity: String, val outputExtension: String) {
        companion object {
            val LOW = QualityPreset("medium", "low", "jpeg")
            val AVERAGE = QualityPreset("high", "high", "jpeg")
            
            val HIGH = QualityPreset("high", "high", "png")
        }
    }

    override suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        images: List<ByteArray>,
        qualityPreset: QualityPreset,
        callback: (List<String>) -> Unit,
    ) {
        log.info("Will use quality preset - $qualityPreset")

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "gpt-image-1")
            .addFormDataPart("prompt", "$systemPrompt\n$userPrompt")
            .addFormDataPart("output_format", qualityPreset.outputExtension)
            .addFormDataPart("quality", qualityPreset.quality)
            .apply {
                if (qualityPreset != QualityPreset.HIGH)
                    addFormDataPart("size", "1024x1024")
            }
            .addFormDataPart("input_fidelity", qualityPreset.fidelity).apply {
                if (images.size == 1) {
                    addFormDataPart(
                        name = "image",
                        filename = "image_${LocalDateTime.now()}.jpeg",
                        body = images.first().toRequestBody("image/jpeg".toMediaType())
                    )
                } else {
                    images.forEach { imageByteArray ->
                        addFormDataPart(
                            name = "image[]",
                            filename = "image_${LocalDateTime.now()}.jpeg",
                            body = imageByteArray.toRequestBody("image/jpeg".toMediaType())
                        )
                    }
                }
            }.build()

        val request = Request.Builder().url(API_URL).addHeader("Authorization", "Bearer $apiKey").post(body).build()

        val response = httpClientService.executeRequest(request)

        log.info("Got response from ai - ${response.take(200)}...${response.takeLast(200)}")

        callback.invoke(objectMapper.extractImagesInB64(response))
    }
}
