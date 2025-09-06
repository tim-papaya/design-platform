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
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.content.Media
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}
private const val API_URL = "https://api.openai.com/v1/images/edits"

private const val IMAGE_DESCRIPTION_PROMPT =
    """Детально опиши это изображение, включая формы и цвета предметов, и выдавай промпт для Stable Diffusion.
            Сохрани картины и постеры и надписи без изменений, опиши их.
            Опиши расположение объектов и их кол-во.
            """


@Profile("prod")
@Service
class OpenAiDoublePromptImageService(
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
    @Autowired
    private val defaultOpenAiChatModel: OpenAiChatModel
) : AiImageService {

    private val openAiChatModel: OpenAiChatModel =
        OpenAiChatModel.Builder(defaultOpenAiChatModel)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model("gpt-5")
                    .build()
            )
            .build()

    override suspend fun generateImage(
        userPrompt: String?,
        systemPromptVariation: Int,
        vararg images: ByteArray,
        callback: (List<String>) -> Unit,
    ) {
        val imageToUse = images.first()

//        val imageDescription = generateImageDescription(ByteArrayResource(imageToUse))
//
//        log.info("Image description - $imageDescription")

//        val maskToUse = Thread.currentThread().contextClassLoader.getResource("example_collage_mask.png").readBytes()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "gpt-image-1")
            .addFormDataPart("prompt", "${systemPrompts[1]}\n")
            .addFormDataPart("output_format", "png")
            .addFormDataPart("quality", "high")
            .addFormDataPart("input_fidelity", "high")
            .addFormDataPart(
                name = "image",
                filename = "image_${LocalDateTime.now()}.jpeg",
                body = imageToUse.toRequestBody("image/jpeg".toMediaType())
            )
            .addFormDataPart("size", "1024x1024")
//            .addFormDataPart(
//                name = "mask",
//                filename = "mask_${LocalDateTime.now()}.png",
//                body = maskToUse.toRequestBody("image/png".toMediaType())
//            )
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        val response = httpClientService.executeRequest(request)

        log.info("Got response from ai - ${response.take(100)}\n...\n${response.takeLast(100)}")

        callback.invoke(objectMapper.extractImagesInB64(response))
    }

    override fun variationNumber() = systemPrompts.size

    private fun generateImageDescription(inputImage: Resource): String? {
        val imageAnalysisMessage = UserMessage.builder()
            .text(IMAGE_DESCRIPTION_PROMPT)
            .media(Media(MimeTypeUtils.IMAGE_JPEG, inputImage))
            .build()

        val analysisResponse = openAiChatModel.call(imageAnalysisMessage)
        return analysisResponse
    }
}