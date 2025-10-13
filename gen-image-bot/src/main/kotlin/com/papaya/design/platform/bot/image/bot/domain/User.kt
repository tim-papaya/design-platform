package com.papaya.design.platform.bot.image.bot.domain

data class User(
    val userId: Long,
    val userState: UserState,
    val userName: String?,
    val userPrompt: String?,
    val photos: List<Photo> = listOf(),
    val generationsNumber: Int,
)
