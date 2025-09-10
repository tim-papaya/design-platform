package com.papaya.design.platform.bot.image.bot.user

import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserEntity

interface UserService {
    fun getUser(userId: Long): User

    fun getUserOrNull(userId: Long): User?

    fun saveUser(user: User, changeMapper: (user: UserEntity) -> Unit = {}): User

    fun saveUser(userId: Long, changeMapper: (UserEntity) -> Unit = {}): User
}
