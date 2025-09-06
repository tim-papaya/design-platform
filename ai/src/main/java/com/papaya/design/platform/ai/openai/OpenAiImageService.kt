package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.HttpClientService
import com.papaya.design.platform.ai.extractImageInB64
import com.papaya.design.platform.ai.extractImagesInB64
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}
private const val API_URL = "https://api.openai.com/v1/images/edits"


class OpenAiImageService(
    private val httpClientService: HttpClientService,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
    private val systemPrompts: List<String> =
        listOf(
            "Создай реалистичное изображение, 3d рендер. И добавь туда чебурашку на диван",
            """Ты опытный дизайнер и 3D-визуализатор. По готовому коллажу или мудборду профессионально создай реалистичное 3D-изображение комнаты. 
                Дизайнер, создававший коллаж или мудборд, будет показывать твою генерацию клиенту, генерация должна отражать настроение и атмосферу коллажа или мудборда. 
                На 3D-изображении должны быть в точности отражены предметы, которые есть на коллаже или мудборде. 
                Ты не должен отклоняться от технического задания и добавлять предметы интерьера на свое усмотрение, 
                предметы с коллажа или мудборда должны сохранить свой цвет, свою форму, фактуру и прочие характеристики. 
                Если на коллаже или мудборде уже отрисованы стены, пол, потолок, они должны быть отражены на 3D-визуализации ровно так, 
                как и на коллаже или мудборде. Если на коллаже нет стен, пола, потолка, тебе разрешается добавить их на 
                3D-визуализацию самостоятельно в стилистике предметов и общего настроения на коллаже или мудборде
                """.trimMargin(),
            "A modern open-plan living room with kitchen, Scandinavian-minimalist style with light art-deco accents. On the left side, a large floor-to-ceiling window with sheer white curtains and grey drapes. In the center, a light beige sofa with two pillows (one pastel pink, one light grey). Above the sofa, a decorative ceiling rosette with a pendant chandelier made of cascading pink glass discs. Behind the sofa, a beige wall with an abstract painting in pink, beige, and gold tones. In front of the sofa, a small sculptural coffee table in beige (two inverted cones), with a black cup on top, standing on a light carpet with geometric patterns. To the right of the sofa, a tall narrow glass cabinet with transparent doors and glasses inside. The kitchen on the right has white glossy cabinets, a beige textured backsplash, a built-in cooktop, and a modern metallic faucet above the sink. On the countertop, a vase with flowers, pastries on a plate, and wine glasses. In the dining area, a round white table with a central ribbed leg, a flower vase, and two glasses. Around the table, three fabric-upholstered chairs in beige with thin metal legs. Light, soft, cozy atmosphere with pastel pink accents, neutral tones, and elegant Scandinavian decor, realistic photo render style."
        ),
) : AiImageService {


    override suspend fun generateImage(
        userPrompt: String?,
        systemPromptVariation: Int,
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

        log.info("Get response from ai - $response")

        callback.invoke(objectMapper.extractImagesInB64(response))
    }

    override fun variationNumber() = systemPrompts.size
}