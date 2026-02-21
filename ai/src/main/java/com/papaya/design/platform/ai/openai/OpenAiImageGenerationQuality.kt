package com.papaya.design.platform.ai.openai

import com.papaya.design.platform.ai.AiImageGenerationQuality
import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.ai.photo.PhotoWithContent

data class OpenAiImageGenerationQuality(
    val quality: String,
    val inputFidelity: String,
    val size: String
)

fun AiImageGenerationQuality.toOpenAi(inputMainImage: PhotoWithContent) =
    when (this) {
        AiImageGenerationQuality.LOW -> OpenAiImageGenerationQuality(
            "low",
            "low",
            Photo.Orientation.SQUARE.toOpenAiSize()
        )
        AiImageGenerationQuality.AVERAGE -> OpenAiImageGenerationQuality(
            "medium",
            "low",
            inputMainImage.currentPhoto.imageOrientation.toOpenAiSize()
        )
        AiImageGenerationQuality.HIGH -> OpenAiImageGenerationQuality(
            "high",
            "high",
            inputMainImage.currentPhoto.imageOrientation.toOpenAiSize()
        )
    }