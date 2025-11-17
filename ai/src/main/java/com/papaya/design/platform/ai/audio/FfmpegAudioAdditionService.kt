package com.papaya.design.platform.ai.audio

import com.papaya.design.platform.ai.AudioAdditionService
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

private val log = KotlinLogging.logger {}

@Service
class FfmpegAudioAdditionService : AudioAdditionService {
    private val resourceResolver = PathMatchingResourcePatternResolver()
    private val audioResources: List<Resource> = loadAudioResources()

    override fun addAudioToVideo(videoBytes: ByteArray): ByteArray {
        val inputVideoFile = createTempFile("ai-video-input-", ".mp4")
        val inputAudioFile = createTempFile("ai-audio-input-", ".mp3")
        val outputFile = createTempFile("ai-video-output-", ".mp4")

        try {
            Files.write(inputVideoFile, videoBytes)
            val audioResource = pickAudioResource()
            copyAudioResource(audioResource, inputAudioFile)

            runFfmpeg(inputVideoFile, inputAudioFile, outputFile)

            return Files.readAllBytes(outputFile)
        } finally {
            Files.deleteIfExists(inputVideoFile)
            Files.deleteIfExists(inputAudioFile)
            Files.deleteIfExists(outputFile)
        }
    }

    private fun copyAudioResource(resource: Resource, destination: Path) {
        resource.inputStream.use { input ->
            Files.newOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun runFfmpeg(videoFile: Path, audioFile: Path, outputFile: Path) {
        val process = ProcessBuilder(
            "ffmpeg",
            "-y",
            "-i", videoFile.toString(),
            "-i", audioFile.toString(),
            "-c:v", "copy",
            "-c:a", "aac",
            "-shortest",
            outputFile.toString()
        )
            .redirectErrorStream(true)
            .start()

        val ffmpegOutput = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            log.error { "FFmpeg failed to add audio to video. Exit code: $exitCode. Output: $ffmpegOutput" }
            throw IllegalStateException("Failed to add audio to video, ffmpeg exited with code $exitCode")
        }
    }

    companion object {
        private const val AUDIO_RESOURCE_PATTERN = "classpath*:music/*.mp3"
    }

    private fun createTempFile(prefix: String, suffix: String): Path =
        Files.createTempFile("$prefix${UUID.randomUUID()}-", suffix)

    private fun pickAudioResource(): Resource {
        if (audioResources.isEmpty()) {
            throw IllegalStateException("No audio resources found with pattern $AUDIO_RESOURCE_PATTERN")
        }
        return audioResources[ThreadLocalRandom.current().nextInt(audioResources.size)]
    }

    private fun loadAudioResources(): List<Resource> =
        resourceResolver.getResources(AUDIO_RESOURCE_PATTERN).filter { it.exists() }
}
