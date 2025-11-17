package com.papaya.design.platform.ai.kling

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.papaya.design.platform.ai.AudioAdditionService
import com.papaya.design.platform.ai.DelegateAiVideoService
import com.papaya.design.platform.ai.GenerationParameters
import com.papaya.design.platform.ai.WaitingGenerationEndAiVideoService
import com.papaya.design.platform.ai.photo.PhotoWithContent
import com.papaya.design.platform.ai.video.VideoModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Base64

private val log = mu.KotlinLogging.logger { }

@Service
@Profile("kling-video")
@DelegateAiVideoService
class KlingAiVideoService(
    @Value("\${kling.api.base-url}") private val baseUrl: String,
    @Value("\${kling.api.image-to-video-path}") private val imageToVideoPath: String,
    private val klingJwtProvider: KlingJwtProvider,
    audioAdditionService: AudioAdditionService,
) : WaitingGenerationEndAiVideoService(audioAdditionService) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofMinutes(5))
        .build()

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun createImageToVideoTask(
        prompt: String,
        inputReferenceJpeg: PhotoWithContent,
        generationParameters: GenerationParameters,
        onError: (message: String) -> Unit
    ): String {
        val base64Image = Base64.getEncoder().encodeToString(inputReferenceJpeg.bytes)

        val payload = mapOf(
            "model_name" to (generationParameters.model ?: VideoModel.KLING_V2_5_TURBO.model),
            "mode" to (generationParameters.mode ?: "pro"),
            "cfg_scale" to 0.5,
            "prompt" to prompt,
            "duration" to (generationParameters.seconds ?: "5"),
            "image" to base64Image,
        )

        val json = objectMapper.writeValueAsString(payload)
        val body = json.toRequestBody()

        val request = buildRequest(url = "$baseUrl/$imageToVideoPath") {
            post(body)
        }

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
                ?: throw IllegalStateException("Empty response body from Kling image-to-video API")

            log.info { "Kling response for create video task: ${response.message} body: $body" }
            if (!response.isSuccessful) {
                throw IllegalStateException("Image-to-video request failed with status ${response.code}: $body")
            }

            val jsonRoot = objectMapper.readTree(body)

            return jsonRoot.path("data").path("task_id").takeIf { it.isTextual }?.asText()
                ?: throw IllegalStateException("task_id not found in Kling image-to-video response")
        }
    }

    override fun pollVideoGenerationStatus(taskId: String): Boolean {
        val request = buildRequest(url = "$baseUrl/$imageToVideoPath/$taskId") {
            get()
        }

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            log.info { "Kling response for get video generation status: ${response.message} body: $body" }
            if (!response.isSuccessful) {
                error("Image-to-video status request failed with status $body")
            }

            val jsonRoot = objectMapper.readTree(body)

            return jsonRoot.path("data").path("task_status").asText().lowercase() == "succeed"
        }
    }

    private fun buildRequest(url: String, action: Request.Builder.() -> Request.Builder): Request =
        Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${klingJwtProvider.getToken()}")
            .addHeader("Content-Type", "application/json")
            .action()
            .build()

    override fun downloadVideo(taskId: String): ByteArray {
        val statusRequest = buildRequest(url = "$baseUrl/$imageToVideoPath/$taskId") {
            get()
        }

        client.newCall(statusRequest).execute().use { response ->
            val body = response.body?.string()
            log.info { "Kling response for get video generation status: ${response.message} body: $body" }
            if (!response.isSuccessful) {
                val message = "Image-to-video status request failed with status"
                error("$message $body")
            }
            val jsonRoot = objectMapper.readTree(body)
            val videosNode = jsonRoot.path("data").path("task_result").withArrayProperty("videos")

            if (videosNode.size() > 1) {
                log.warn { "More than one video generated, using the first one message: ${response.message} body: $body" }
            }
            val videoUrl = videosNode.first().path("url").asText()

            val videoDownloadRequest = Request.Builder()
                .url(videoUrl)
                .get()
                .build()

            client.newCall(videoDownloadRequest).execute().use { videoResponse ->
                return videoResponse.body?.bytes()
                    ?: error("Error on download video from url $videoUrl: ${response.message} body: ${videoResponse.body?.string()}")
            }
        }
    }
}
