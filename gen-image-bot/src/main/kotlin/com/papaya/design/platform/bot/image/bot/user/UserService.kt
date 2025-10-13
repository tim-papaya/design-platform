package com.papaya.design.platform.bot.image.bot.user

import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserEntity
import com.papaya.design.platform.bot.image.bot.message.TelegramId

interface UserService {
    fun getUser(userId: Long): User

    fun getUserOrNull(userId: Long): User?

    fun saveUser(userId: TelegramId, changeMapper: (UserEntity) -> Unit = {}): User

    fun saveUser(userId: Long, changeMapper: (UserEntity) -> Unit = {}): User

    fun getAllUserIds() : List<Long>
}
