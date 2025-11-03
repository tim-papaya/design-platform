package com.papaya.design.platform.bot.image.bot.user

import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserEntity
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.domain.toModel
import com.papaya.design.platform.bot.image.bot.message.TelegramId
import mu.KotlinLogging
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class CachedUserService(
    private val userRepository: UserRepository,
) : UserService {

//    @Cacheable(value = ["users"], key = "#userId")
    override fun getUser(userId: Long): User {
        log.debug { "Get user $userId from db" }
        val result = userRepository.findByUserId(userId)
        if (result == null) log.error { "User $userId not found in db" }
        return result?.toModel() ?: throw IllegalStateException("User not found")
    }

//    @Cacheable(value = ["users"], key = "#userId")
    override fun getUserOrNull(userId: Long): User? {
        log.debug { "Get user $userId from db" }
        val result = userRepository.findByUserId(userId)

        return result?.toModel()
    }

//    @CachePut(value = ["users"], key = "#userId.userId")
    override fun saveUser(
        userId: TelegramId,
        changeMapper: (UserEntity) -> Unit
    ): User {
        log.debug { "Save user $userId to db" }
        val entity = userRepository.findByUserId(userId.userId)
            .let { userEntity -> userEntity ?: UserEntity().also { it.userId = userId.userId } }
            .also {
                it.userName = userId.userName
                changeMapper.invoke(it)
            }

        return userRepository.save(entity).toModel()
    }

//    @CachePut(value = ["users"], key = "#userId")
    override fun saveUser(
        userId: Long,
        changeMapper: (UserEntity) -> Unit
    ): User {
        log.debug { "Save user $userId to db" }
        val entity = userRepository.findByUserId(userId)
            .let { userEntity -> userEntity ?: UserEntity().also { it.userId = userId } }
            .also {
                changeMapper.invoke(it)
            }

        return userRepository.save(entity).toModel()
    }

    override fun getAllUserIds(): List<Long> =
        userRepository.findAll().map { it.userId }
}
