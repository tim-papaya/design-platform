package com.papaya.design.platform.bot.contractor.user

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger { }

@Service
class SimpleUserService(
    private val userRepository: UserRepository
) : UserService {

    private val users = ConcurrentHashMap<Long, UserEntity>()

    override fun getUser(userId: Long): User =
        users.getValue(userId).toModel()

    override fun getUserOrNull(userId: Long): User? =
        users.get(userId)?.toModel()

    override fun saveUser(
        userId: Long,
        changeMapper: (UserEntity) -> Unit
    ): User =
        users[userId]
        .let { it ?: userRepository.findByUserId(userId) }
        .let { it ?: UserEntity().also { it.userId = userId } }
        .also { changeMapper.invoke(it) }
        .also {
            users[userId] = it
            userRepository.save(it)
            log.info { "User cache: $users" }
        }.toModel()

    @PostConstruct
    fun init() {
        users.putAll(userRepository.findAll().associateBy { it.userId })
    }
}