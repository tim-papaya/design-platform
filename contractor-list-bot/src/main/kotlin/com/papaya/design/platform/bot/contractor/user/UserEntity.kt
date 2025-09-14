package com.papaya.design.platform.bot.contractor.user

import jakarta.persistence.*
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var uniqueId: Long = 0

    var userId: Long = 0

    @Enumerated(value = EnumType.STRING)
    var userState: ContractorUserState = ContractorUserState.READY_FOR_CMD

    var category: String? = null

}

fun UserEntity.toModel() =
    User(
        this.userId,
        this.userState,
        this.category,
    )
