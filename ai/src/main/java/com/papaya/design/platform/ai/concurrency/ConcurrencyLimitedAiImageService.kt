package com.papaya.design.platform.ai.concurrency

import com.papaya.design.platform.ai.AiImageService
import com.papaya.design.platform.ai.DelegateAiImageService
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
class ConcurrencyLimitedAiImageService(
    @DelegateAiImageService
    private val delegate: AiImageService,
    @Value("\${com.papaya.design.platform.ai.image.max-concurrency:20}")
    private val maxConcurrentRequests: Int,
) : AiImageService {

    init {
        require(maxConcurrentRequests > 0) {
            "com.papaya.design.platform.ai.image.max-concurrency must be positive"
        }
    }

    private val semaphore = Semaphore(maxConcurrentRequests)
    private val activeRequests = AtomicInteger(0)

    override suspend fun generateImage(
        userPrompt: String?,
        systemPrompt: String,
        model: String?,
        images: List<PhotoWithContent>,
        callback: (base64Images: List<String>) -> Unit
    ) {
        val available = semaphore.availablePermits
        log.info { "Image generation requested: available_permits=$available active=${activeRequests.get()} limit=$maxConcurrentRequests" }
        semaphore.withPermit {
            val currentActive = activeRequests.incrementAndGet()
            log.info { "Image generation started: active=$currentActive limit=$maxConcurrentRequests" }
            try {
                delegate.generateImage(userPrompt, systemPrompt, model, images, callback)
            } finally {
                val remaining = activeRequests.decrementAndGet()
                log.info { "Image generation finished: active=$remaining limit=$maxConcurrentRequests" }
            }
        }
    }
}
