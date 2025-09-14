package com.papaya.design.platform.bot.contractor.user

import com.papaya.design.platform.bot.contractor.tg.jpa.user.User
import com.papaya.design.platform.bot.contractor.tg.jpa.user.UserEntity
import com.papaya.design.platform.bot.contractor.tg.jpa.user.UserService
import com.papaya.design.platform.bot.contractor.tg.jpa.user.toModel
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class SimpleUserService : UserService {

    private val users = ConcurrentHashMap<Long, User>()

    override fun getUser(userId: Long): User =
        users.getValue(userId)

    override fun getUserOrNull(userId: Long): User? =
        users.getValue(userId)

    override fun saveUser(
        userId: Long,
        changeMapper: (UserEntity) -> Unit
    ): User = UserEntity()
        .also { changeMapper.invoke(it) }
        .toModel()
        .also { users[userId] = it }
}