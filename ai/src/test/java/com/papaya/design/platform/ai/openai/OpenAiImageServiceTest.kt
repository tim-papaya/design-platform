package com.papaya.design.platform.ai.openai

import com.papaya.design.platform.ai.AiConfig
import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.ai.photo.PhotoWithContent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.io.File
import java.util.Base64.getDecoder

@SpringBootTest(classes = [AiConfig::class])
@Import(AiConfig::class)
@Disabled("Integration test - requires API key and network access")
class OpenAiImageServiceTest {
    @Autowired
    lateinit var openAiImageService: OpenAiImageService

    @Test
    fun `should generate image successfully`() = runBlocking {
        val imageBytes = Thread.currentThread().contextClassLoader.getResource("example.png").readBytes()
        val image = PhotoWithContent(Photo("test", "test", 1024, 1024), imageBytes)
        openAiImageService.generateImage(null, "", null, listOf(image)) { resImagesB64 ->
            File("result.png").writeBytes(getDecoder().decode(resImagesB64.first()))
        }
    }
}
