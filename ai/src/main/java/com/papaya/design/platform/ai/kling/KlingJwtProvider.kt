package com.papaya.design.platform.ai.kling

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
@Profile("prod", "kling-video")
class KlingJwtProvider(
    @Value("\${kling.api.access-key}") private val accessKey: String,
    @Value("\${kling.api.secret-key}") private val secretKey: String,
    @Value("\${kling.api.token-ttl-seconds:1800}") private val ttlSeconds: Long
) {
    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var expiresAt: Instant? = null

    private val algorithm: Algorithm = Algorithm.HMAC256(secretKey)

    fun getToken(): String {
        val now = Instant.now()
        val currentExpiry = expiresAt

        if (cachedToken == null || currentExpiry == null || now.isAfter(currentExpiry.minusSeconds(60))) {
            val exp = now.plusSeconds(ttlSeconds)
            val notBefore = now.minusSeconds(5)
            val header = mapOf("alg" to "HS256", "typ" to "JWT")

            val token = JWT.create()
                .withIssuer(accessKey)
                .withHeader(header)
                .withExpiresAt(Date.from(exp))
                .withNotBefore(Date.from(notBefore))
                .sign(algorithm)

            cachedToken = token
            expiresAt = exp
        }

        return cachedToken!!
    }
}
