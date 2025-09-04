package com.papaya.design.platform.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.openai.OpenAiImageResponse

internal fun ObjectMapper.extractImageInB64(response: String): String =
    this.readValue(response, OpenAiImageResponse::class.java)
        .data.first().b64Json ?: throw IllegalStateException("Generation exception")

internal fun ObjectMapper.extractImagesInB64(response: String): List<String> =
    this.readValue(response, OpenAiImageResponse::class.java)
        .data.map { it.b64Json ?: throw IllegalStateException("Generation exception") }