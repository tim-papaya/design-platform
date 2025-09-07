package com.papaya.design.platform.bot.image.bot.user

import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger { }

@Service
class SimpleUserService : UserService {

    val users = ConcurrentHashMap<Long, User>()

    override fun getUser(usedId: Long): User =
        users[usedId].let {
            if (it != null) it
            else {
                log.error { "User $usedId not found in persistence " }
                throw IllegalStateException("User not found")
            }
        }

    override fun getUserOrNull(usedId: Long): User? =
        users[usedId]

    override fun addUser(userId: Long): User {
        val newUser = User().apply {
            userState = UserState.READY_FOR_CMD
        }
        users[userId] = newUser

        return newUser
    }
}