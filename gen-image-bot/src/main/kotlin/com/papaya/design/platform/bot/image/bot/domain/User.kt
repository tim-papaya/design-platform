package com.papaya.design.platform.bot.image.bot.domain

import com.papaya.design.platform.ai.photo.Photo

data class User(
    val userId: Long,
    val userState: UserState,
    val userName: String?,
    val userPrompt: String?,
    val photos: List<Photo> = listOf(),
    val generationsNumber: Int,
    val isAcceptedRules: Boolean
)
