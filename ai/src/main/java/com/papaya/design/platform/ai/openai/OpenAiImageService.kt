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
private const val API_URL = "https://api.openai.com/v1/images/edits"

@Profile("prod")
@Service
class OpenAiImageService(
    private val httpClientService: HttpClientService,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
    private val systemPrompts: List<String> =
        listOf(
            "Создай реалистичное изображение на основе примера. Не добавляй никаких дополнительных предметов, кроме тех что есть в примере.",
            """Ты опытный дизайнер и 3D-визуализатор. По готовому коллажу или мудборду профессионально создай реалистичное 3D-изображение комнаты. 
                |Дизайнер, создававший коллаж или мудборд, будет показывать твою генерацию клиенту, генерация должна отражать настроение и атмосферу коллажа или мудборда. 
                |На 3D-изображении должны быть в точности отражены предметы, которые есть на коллаже или мудборде. 
                |Ты не должен отклоняться от технического задания и добавлять предметы интерьера на свое усмотрение, 
                |предметы с коллажа или мудборда должны сохранить свой цвет, свою форму, фактуру и прочие характеристики. 
                |Если на коллаже или мудборде уже отрисованы стены, пол, потолок, они должны быть отражены на 3D-визуализации ровно так, 
                |как и на коллаже или мудборде. Если на коллаже нет стен, пола, потолка, тебе разрешается добавить их на 
                |3D-визуализацию самостоятельно в стилистике предметов и общего настроения на коллаже или мудборде""".trimMargin()
        ),
) : AiImageService {


    override suspend fun generateImage(
        userPrompt: String?,
        vararg images: ByteArray,
        systemPromptVariation: Int,
        callback: (String) -> Unit,
    ) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "gpt-image-1")
            .addFormDataPart(
                name = "image",
                filename = "example.png",
                body = images.first().toRequestBody("image/png".toMediaType())
            )
            .addFormDataPart("prompt", "${systemPrompts[systemPromptVariation]}\n$userPrompt")
            .addFormDataPart("output_format", "jpeg")
            .addFormDataPart("quality", "high")
            .addFormDataPart("size", "1024x1024")
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        val response = httpClientService.executeRequest(request)

        log.info("Get response from ai - $response")

        callback.invoke(objectMapper.extractImageInB64(response))
    }

    override fun variationNumber() = systemPrompts.size
}