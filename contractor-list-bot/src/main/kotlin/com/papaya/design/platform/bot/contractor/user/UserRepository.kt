package com.papaya.design.platform.bot.contractor.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<UserEntity, Long> {
    fun findByUserId(userId: Long): UserEntity?
}
