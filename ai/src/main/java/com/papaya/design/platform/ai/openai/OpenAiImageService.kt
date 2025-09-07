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
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
    private val systemPrompts: List<String> =
        listOf(
            """Ты опытный дизайнер и 3D-визуализатор. По готовому коллажу или мудборду профессионально создай реалистичное 3D-изображение комнаты. 
                Дизайнер, создававший коллаж или мудборд, будет показывать твою генерацию клиенту, генерация должна отражать настроение и атмосферу коллажа или мудборда. 
                На 3D-изображении должны быть в точности отражены предметы, которые есть на коллаже или мудборде. 
                Ты не должен отклоняться от технического задания и добавлять предметы интерьера на свое усмотрение.
                Если на коллаже или мудборде уже отрисованы стены, пол, потолок, они должны быть отражены на 3D-визуализации ровно так, 
                как и на коллаже или мудборде. Если на коллаже нет стен, пола, потолка, тебе разрешается добавить их на 
                3D-визуализацию самостоятельно в стилистике предметов и общего настроения на коллаже или мудборде.
                Дополнительно пропиши - realistic materials, global illumination, 35mm lens.
                """.trimMargin(),
            """Сгенерируй реалистичное изображение как через chat CHAT-GPT-5. 
               ОПРЕДЕЛИ РАСПОЛОЖЕНИЕ И ФОРМУ ОБЪЕКТОВ, ПЕРЕД ГЕНЕРАЦИЕЙ.
               НЕ МЕНЯЙ ОБЪЕКТЫ ВООБЩЕ, ТОЛЬКО ДОБАВЬ РЕАЛИСТИЧНОСТИ.
               Добавь качества и фотореалистчности изображению.
                """.trimMargin(),
        ),
) : AiImageService {


    override suspend fun generateImage(
        userPrompt: String?,
        vararg images: ByteArray,
        callback: (List<String>) -> Unit,
    ) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "gpt-image-1")
            .addFormDataPart("prompt", systemPrompts[2])
            .addFormDataPart("output_format", "png")
            .addFormDataPart("quality", "high")
            .addFormDataPart("input_fidelity", "high")
            .addFormDataPart(
                name = "image",
                filename = "image_${LocalDateTime.now()}.jpeg",
                body = images.first().toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        val response = httpClientService.executeRequest(request)

        log.info("Got response from ai - $response")

        callback.invoke(objectMapper.extractImagesInB64(response))
    }
}
