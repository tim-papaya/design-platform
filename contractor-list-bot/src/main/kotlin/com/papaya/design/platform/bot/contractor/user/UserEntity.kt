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

    var name: String? = null

    @Enumerated(value = EnumType.STRING)
    @Transient
    var userState: ContractorUserState = ContractorUserState.MAIN_MENU_READY_FOR_CMD

    @Transient
    var category: String? = null

    @Transient
    var contractorName: String? = null

    override fun toString(): String {
        return "UserEntity(uniqueId=$uniqueId, userId=$userId, name=$name, userState=$userState, category=$category, contractorName=$contractorName)"
    }

}

fun UserEntity.toModel() =
    User(
        this.userId,
        this.userState,
        this.category,
        this.name?.let { "@$it" } ?: "Имя не найдено (id: ${this.userId})",
        this.contractorName
    )
