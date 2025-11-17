package com.papaya.design.platform.ai.concurrency

import com.papaya.design.platform.ai.AiVideoService
import com.papaya.design.platform.ai.DelegateAiVideoService
import com.papaya.design.platform.ai.GenerationParameters
import com.papaya.design.platform.ai.photo.PhotoWithContent
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

private val log = KotlinLogging.logger {}

@Service
@Profile("prod")
@Primary
class ConcurrencyLimitedAiVideoService(
    @DelegateAiVideoService
    private val delegate: AiVideoService,
    @Value("\${com.papaya.design.platform.ai.video.max-concurrency:10}")
    private val maxConcurrentRequests: Int,
) : AiVideoService {

    init {
        require(maxConcurrentRequests > 0) {
            "com.papaya.design.platform.ai.video.max-concurrency must be positive"
        }
    }

    private val semaphore = Semaphore(maxConcurrentRequests)
    private val activeRequests = AtomicInteger(0)

    override suspend fun generateVideo(
        systemPrompt: String?,
        userPrompt: String?,
        inputReferenceJpeg: PhotoWithContent,
        generationParameters: GenerationParameters,
        onError: (message: String) -> Unit,
        callback: (video: ByteArray) -> Unit
    ) {
        val available = semaphore.availablePermits
        log.info { "Video generation requested: available_permits=$available active=${activeRequests.get()} limit=$maxConcurrentRequests" }
        semaphore.withPermit {
            val currentActive = activeRequests.incrementAndGet()
            log.info { "Video generation started: active=$currentActive limit=$maxConcurrentRequests" }
            try {
                delegate.generateVideo(
                    systemPrompt,
                    userPrompt,
                    inputReferenceJpeg,
                    generationParameters,
                    onError,
                    callback
                )
            } finally {
                val remaining = activeRequests.decrementAndGet()
                log.info { "Video generation finished: active=$remaining limit=$maxConcurrentRequests" }
            }
        }
    }
}
