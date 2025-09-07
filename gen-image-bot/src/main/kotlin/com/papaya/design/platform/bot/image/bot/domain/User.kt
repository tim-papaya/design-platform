package com.papaya.design.platform.bot.image.bot.domain

class User {
    lateinit var userPrompt: String
    lateinit var userState: UserState
    lateinit var photos: List<Photo>
}