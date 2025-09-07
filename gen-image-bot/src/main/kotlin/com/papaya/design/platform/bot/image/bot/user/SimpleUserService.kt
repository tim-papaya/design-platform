package com.papaya.design.platform.bot.image.bot.user

import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

@Service
class SimpleUserService : UserService {

    val users = ConcurrentHashMap<Long, User>()

    override fun getUser(usedId: Long): User =
        users[usedId] ?: throw IllegalStateException("User not found")

    override fun addUser(userId: Long) {
        users[userId] = User().apply {
            userState = UserState.NEW_USER
        }
    }
}