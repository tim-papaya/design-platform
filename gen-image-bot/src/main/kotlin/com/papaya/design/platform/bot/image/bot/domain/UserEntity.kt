package com.papaya.design.platform.bot.image.bot.domain

import jakarta.persistence.*

@Entity(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var uniqueId: Long = 0

    var userId: Long = 0

    @Enumerated(value = EnumType.STRING)
    var userState: UserState = UserState.READY_FOR_CMD

    var generationsNumber: Int = 0

    var userPrompt: String? = null

    @OneToMany(
        fetch = FetchType.EAGER,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE],
        orphanRemoval = true
    )
    @JoinColumn(name = "user_unique_id", nullable = false)
    var photos: List<PhotoEntity> = listOf()
}

fun UserEntity.toModel() =
    User(
        this.userId,
        this.userState,
        this.userPrompt,
        this.photos.map { it.toModel() },
        this.generationsNumber
    )

fun User.toEntity() =
    UserEntity().also {
        it.userId = this.userId
        it.userState = this.userState
        it.userPrompt = this.userPrompt
        it.photos = this.photos.map { it.toEntity() }
        it.generationsNumber = this.generationsNumber
    }
