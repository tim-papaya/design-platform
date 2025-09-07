package com.papaya.design.platform.bot.image.bot.user

import com.papaya.design.platform.bot.image.bot.domain.User

interface UserService {
    fun getUser(usedId: Long): User

    fun getUserOrNull(usedId: Long): User?

    fun addUser(userId: Long) : User
}