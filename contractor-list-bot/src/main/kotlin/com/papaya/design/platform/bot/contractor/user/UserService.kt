package com.papaya.design.platform.bot.contractor.user

interface UserService {
    fun getUser(userId: Long): User

    fun getUserOrNull(userId: Long): User?

    fun saveUser(userId: Long, changeMapper: (UserEntity) -> Unit = {}): User
}