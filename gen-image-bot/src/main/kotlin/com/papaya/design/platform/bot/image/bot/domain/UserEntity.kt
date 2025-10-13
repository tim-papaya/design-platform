package com.papaya.design.platform.bot.image.bot.domain

import jakarta.persistence.*

@Entity(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var uniqueId: Long = 0

    var userId: Long = 0

    var userName: String? = null

    @Enumerated(value = EnumType.STRING)
    var userState: UserState = UserState.READY_FOR_CMD

    var generations: Int = 0

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
        this.userName,
        this.userPrompt,
        this.photos.map { it.toModel() },
        this.generations
    )

fun User.toEntity() =
    UserEntity().also {
        it.userId = this.userId
        it.userState = this.userState
        it.userName = this.userName
        it.userPrompt = this.userPrompt
        it.photos = this.photos.map { it.toEntity() }
        it.generations = this.generationsNumber
    }
