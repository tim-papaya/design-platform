package com.papaya.design.platform.ai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import mu.KotlinLogging
import org.springframework.ai.image.ImagePrompt
import org.springframework.ai.openai.OpenAiImageModel
import org.springframework.ai.openai.OpenAiImageOptions
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.content.Media
import org.springframework.ai.image.ImageResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.util.MimeTypeUtils
import org.springframework.core.io.ClassPathResource

@SpringBootApplication
class GenImageApplication {
    private val log = KotlinLogging.logger {}

    @Autowired
    lateinit var openAiImageModel: OpenAiImageModel
    
    @Autowired 
    lateinit var openAiChatModel: OpenAiChatModel

    @EventListener(ContextRefreshedEvent::class)
    fun onStart() {
        log.info("Hello from start")
        
        // Шаг 1: Анализируем входное изображение с помощью GPT-4 Vision
        val inputImage = ClassPathResource("example.jpg") // Поместите ваше изображение в src/main/resources/

        log.info(inputImage.description)

        val response = openAiChatModel.call(
            Prompt.builder()
                .messages(
                    UserMessage.builder()
                        .media(Media(MimeTypeUtils.IMAGE_JPEG, inputImage))
                        .text("Сгенерируй максимиально реалистичное изображение. Не добавляй никаких деталей, кроме тех что есть в примере.")
                        .build()
                )
                .build()
        )


        log.info("Сгенерированное изображение: ${response.result.output.text}")
        log.info("Сгенерированное изображение: ${response.result.output.media}")
    }

    private fun generateImageByImageModel(enhancedPrompt: String): ImageResponse {
        val imageResponse = openAiImageModel.call(
            ImagePrompt(
                enhancedPrompt,
                OpenAiImageOptions
                    .builder()
                    .quality("hd")
                    .N(1)
                    .height(1024)
                    .width(1024)
                    .build()
            )
        )
        return imageResponse
    }

    private fun readAnalysisFromFile(): String =
        Thread.currentThread().contextClassLoader.getResource("analysis.txt")?.readText()
            ?: throw IllegalStateException("No analysis file")

    private fun generateImageDescription(inputImage: ClassPathResource): String? {
        val imageAnalysisMessage = UserMessage.builder()
            .text("Детально опиши это изображение, включая все объекты, цвета, композицию и стиль. Твое описание будет использовано для генерации похожего изображения.")
            .media(Media(MimeTypeUtils.IMAGE_JPEG, inputImage))
            .build()

        val analysisResponse = openAiChatModel.call(imageAnalysisMessage)
        return analysisResponse
    }

    fun genImage() {

    }
}

fun main(args: Array<String>) {
    runApplication<GenImageApplication>(*args)
}
