package com.papaya.design.platform.bot.image.bot.domain

import com.papaya.design.platform.ai.openai.OpenAiImageService

data class User(
    val userId: Long,
    val userState: UserState,
    val userPrompt: String?,
    val photos: List<Photo> = listOf(),
    val qualityPreset: OpenAiImageService.QualityPreset = OpenAiImageService.QualityPreset.AVERAGE,
    val generationsNumber: Int,
)
