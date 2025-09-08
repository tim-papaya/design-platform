package com.papaya.design.platform.bot.image.bot.domain

import com.papaya.design.platform.ai.openai.OpenAiImageService

class User {
    var id: Long = 0
    lateinit var userPrompt: String
    lateinit var userState: UserState
    var photos: List<Photo> = listOf()
    var qualityPreset: OpenAiImageService.QualityPreset = OpenAiImageService.QualityPreset.AVERAGE
}