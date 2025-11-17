package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.AiVideoService
import com.papaya.design.platform.ai.DelegateAiVideoService
import com.papaya.design.platform.ai.GenerationParameters
import com.papaya.design.platform.ai.HttpClientService
import com.papaya.design.platform.ai.extractImagesInB64
import com.papaya.design.platform.ai.photo.PhotoWithContent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

const val MODERATION_BLOCKED_CODE = "moderation_blocked"

private val log = KotlinLogging.logger {}

private const val API_URL_VIDEOS = "https://api.openai.com/v1/videos"

private const val OPENAI_VIDEO_DEFAULT_WIDTH = 1280

private const val OPENAI_VIDEO_DEFAULT_HEIGHT = 720

private const val MAX_TIMES_TO_DELAY_FOR_VIDEO_GENERATION_END = 5

@Profile("open-ai-video")
@Service
@DelegateAiVideoService
class OpenAiIVideoService(
    private val httpClientService: HttpClientService,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
) : AiVideoService {

    private val http = OkHttpClient()

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun generateVideo(
        systemPrompt: String?,
        userPrompt: String?,
        inputReferenceJpeg: PhotoWithContent,
        generationParameters: GenerationParameters,
        onError: (message: String) -> Unit,
        callback: (video: ByteArray) -> Unit
    ) {
        val prompt = buildString {
            append(systemPrompt)
            if (!userPrompt.isNullOrBlank()) append(" ").append(userPrompt)
        }

        val centerCroppedPhoto = inputReferenceJpeg
            .upscaleIfSmaller(OPENAI_VIDEO_DEFAULT_WIDTH, OPENAI_VIDEO_DEFAULT_HEIGHT)
            .cropToCenter(OPENAI_VIDEO_DEFAULT_WIDTH, OPENAI_VIDEO_DEFAULT_HEIGHT)


        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("seconds", "4")
            .addFormDataPart("size", centerCroppedPhoto.currentPhoto.sizeAsString)
            .addFormDataPart("model", "sora-2")
            .apply {
                addFormDataPart(
                    name = "input_reference",
                    filename = "input_reference.jpg",
                    body = centerCroppedPhoto.bytes.toRequestBody("image/jpeg".toMediaType())
                )
            }
            .build()

        val createReq = Request.Builder()
            .url(API_URL_VIDEOS)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(multipart)
            .build()

        http.newCall(createReq).execute().use { resp ->
            if (!resp.isSuccessful) error("OpenAI create video failed: ${resp.code} ${resp.body?.string()}")
            val json = resp.body?.string().orEmpty()

            log.info { "Video response: $resp $json" }
            val videoId = objectMapper.readTree(json).path("id").asText()

            if (videoId.isBlank()) error("No video job id in response: $json")

            delay(40_000)
            waitForVideoGenerationCompleted(videoId, onError)
            callback.invoke(pollAndDownloadVideo(videoId))
        }
    }

    private suspend fun waitForVideoGenerationCompleted(
        videoId: String?,
        onError: (message: String) -> Unit,
        executedTimes: Int = 1
    ) {
        if (executedTimes >= MAX_TIMES_TO_DELAY_FOR_VIDEO_GENERATION_END) return

        val videoStatusRequest = createGetVideoStatusRequest(videoId)
        val resp = http.newCall(videoStatusRequest).execute()

        val json = objectMapper.readTree(resp.body?.string().orEmpty())

        if (!resp.isSuccessful || json.hasNonNull("error")) {
            val errorNode = json.get("error")
            val errorMessage = errorNode.get("message").textValue()

            when (val errorCode = errorNode.get("code").textValue()) {
                MODERATION_BLOCKED_CODE -> onError.invoke("$errorCode : $errorMessage")
                else -> onError.invoke("$errorMessage")
            }
            return
        }

        if (json.get("progress").asInt() < 100) {
            log.info { "Video $videoId status: ${json.get("progress").asInt()}%" }
            delay(20_000)
            waitForVideoGenerationCompleted(videoId, onError, executedTimes + 1)
        }
    }

    private fun createGetVideoStatusRequest(videoId: String?) =
        Request.Builder()
            .url("$API_URL_VIDEOS/$videoId")
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()

    private fun pollAndDownloadVideo(jobId: String): ByteArray {
        repeat(600) {
            val statusReq = Request.Builder()
                .url("$API_URL_VIDEOS/$jobId/content")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()

            http.newCall(statusReq).execute().use { resp ->
                if (!resp.isSuccessful) error("OpenAI status failed: ${resp.code} ${resp.body?.string()}")

                return resp.body?.bytes() ?: error("Can not find resulted video")
            }
        }
        error("Timeout waiting for video job $jobId")
    }

    private fun download(url: String): ByteArray {
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Download failed: ${resp.code}")
            return resp.body?.bytes() ?: error("Empty download body")
        }
    }
}
