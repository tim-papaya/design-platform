package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiImageResponse(
    @JsonProperty("data")
    val data: List<ImageData>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ImageData(
    @JsonProperty("b64_json")
    val b64Json: String? = null,
)

