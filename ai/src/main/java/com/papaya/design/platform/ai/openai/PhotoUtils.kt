package com.papaya.design.platform.ai.openai

import com.papaya.design.platform.ai.photo.Photo

fun Photo.Orientation.toOpenAiSize() =
    when (this) {
        Photo.Orientation.HORIZONTAL -> "1536x1024"
        Photo.Orientation.VERTICAL -> "1024x1536"
        Photo.Orientation.SQUARE -> "1024x1024"
    }