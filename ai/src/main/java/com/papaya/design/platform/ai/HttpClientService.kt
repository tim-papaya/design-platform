package com.papaya.design.platform.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.*
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.concurrent.TimeUnit

@Service
class HttpClientService {
    
    private val logger = KotlinLogging.logger {}
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun executeRequest(request: Request): String = withContext(Dispatchers.IO) {
        try {
            logger.info { "Executing HTTP request: ${request.method} ${request.url}" }
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error { "HTTP request failed: ${response.code} ${response.message}" }
                    logger.error { "HTTP error body: ${response.body?.string()}" }
                    throw IOException("Unexpected response code: ${response.code}")
                }
                
                val responseBody = response.body?.string() ?: ""
                logger.debug { "Response received, length: ${responseBody.length}" }
                responseBody
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing HTTP request" }
            throw e
        }
    }
}
