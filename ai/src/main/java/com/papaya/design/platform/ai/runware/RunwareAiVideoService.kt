package com.papaya.design.platform.ai.runware

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.papaya.design.platform.ai.AudioAdditionService
import com.papaya.design.platform.ai.DelegateAiVideoService
import com.papaya.design.platform.ai.GenerationParameters
import com.papaya.design.platform.ai.WaitingGenerationEndAiVideoService
import com.papaya.design.platform.ai.photo.PhotoWithContent
import com.papaya.design.platform.ai.video.VideoModel
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

private val log = KotlinLogging.logger { }

@Service
@Profile("runware-video")
@DelegateAiVideoService
class RunwareAiVideoService(
    @Value("\${runware.api.base-url}") private val baseUrl: String,
    @Value("\${runware.api.image-to-video-path}") private val imageToVideoPath: String,
    @Value("\${runware.api-key}")
    private val apiKey: String,
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

        val payload = RunwareImageToVideoPayload(
            model = generationParameters.model ?: VideoModel.RUNWARE_KLING_V2_5_TURBO_STD.model,
            cfgScale = 0.5,
            positivePrompt = prompt,
            duration = generationParameters.seconds ?: 5,
            frameImages = listOf(
                RunwareFrameImage(inputImage = base64Image, frame = "first"),
            )
        )

        val json = objectMapper.writeValueAsString(payload)
            .wrapWithArray()
        val body = json.toRequestBody()

        val request = buildRequest(url = "$baseUrl/$imageToVideoPath") {
            post(body)
        }

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
                ?: throw IllegalStateException("Empty response body from Runware image-to-video API")

            log.info { "Runware response for create video task: ${response.message} body: $body" }
            if (!response.isSuccessful) {
                throw IllegalStateException("Image-to-video request failed with status ${response.code}: $body")
            }

            val createResponse = objectMapper.readValue<RunwareCreateTaskResponse>(body)

            if (createResponse.data.size > 1) {
                log.warn { "More than one video generated, using the first one message: ${response.message} body: $body" }
            }
            return createResponse.data.firstOrNull()?.taskUUID
                ?: throw IllegalStateException("taskUUID not found in Runware image-to-video response")
        }
    }

    override fun pollVideoGenerationStatus(taskId: String): Boolean {
        val request = buildRequest(url = "$baseUrl/$imageToVideoPath") {
            val json = objectMapper.writeValueAsString(RunwareStatus(taskId))
                .wrapWithArray()
            post(json.toRequestBody())
        }

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            log.info { "Runware response for get video generation status: ${response.message} body: $body" }
            if (!response.isSuccessful) {
                error("Image-to-video status request failed with status $body")
            }

            val statusResponse = body?.let { objectMapper.readValue<RunwareTaskStatusResponse>(it) }
                ?: throw IllegalStateException("Empty response body from Runware status API")

            return statusResponse.data.first().status.equals("success", ignoreCase = true)
        }
    }

    private fun buildRequest(url: String, action: Request.Builder.() -> Request.Builder): Request =
        Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .action()
            .build()

    override fun downloadVideo(taskId: String): ByteArray {
        val statusRequest = buildRequest(url = "$baseUrl/$imageToVideoPath") {
            val json = objectMapper.writeValueAsString(RunwareStatus(taskId))
                .wrapWithArray()
            post(json.toRequestBody())
        }

        client.newCall(statusRequest).execute().use { response ->
            val body = response.body?.string()
            log.info { "Runware response for get video by url: ${response.message} body: $body" }
            if (!response.isSuccessful) {
                val message = "Video download request failed with status"
                error("$message $body")
            }
            val statusResponse = body?.let { objectMapper.readValue<RunwareTaskStatusResponse>(it) }
                ?: error("Empty response body from Runware image-to-video status API")
            val videoUrl = statusResponse.data.first().videoUrl

            if (videoUrl.isNullOrBlank()) {
                error("Video is not found: ${response.message} body: $body")
            }

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

private fun String.wrapWithArray() = "[ $this ]"

private data class RunwareImageToVideoPayload(
    val model: String,
    @JsonProperty("CFGScale")
    val cfgScale: Double,
    val positivePrompt: String,
    val duration: Int,
    val frameImages: List<RunwareFrameImage>,
    val taskType: String = "videoInference",
    @JsonProperty("taskUUID")
    val taskUUID: String = UUID.randomUUID().toString(),
)

private data class RunwareFrameImage(
    val inputImage: String,
    val frame: String,
)

private data class RunwareCreateTaskResponse(
    val data: List<RunwareTaskDescriptor>,
)

private data class RunwareTaskDescriptor(
    val taskType: String,
    @JsonProperty("taskUUID")
    val taskUUID: String,
)

private data class RunwareTaskStatusResponse(
    val data: List<RunwareTaskStatusData>,
)

private data class RunwareTaskStatusData(
    val status: String,
    @JsonProperty("videoURL")
    val videoUrl: String?,
)

private data class RunwareStatus(
    @JsonProperty("taskUUID")
    val taskUUID: String,
    val taskType: String = "getResponse"
)
