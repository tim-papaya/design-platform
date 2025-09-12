package com.papaya.design.platform.bot.image.bot.domain

import com.papaya.design.platform.ai.openai.OpenAiImageService
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import org.hibernate.generator.EventType

@Entity(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var uniqueId: Long = 0

    var userId: Long = 0

    @Enumerated(value = EnumType.STRING)
    var userState: UserState = UserState.READY_FOR_CMD

    @Enumerated(value = EnumType.STRING)
    var qualityPreset: OpenAiImageService.QualityPreset = OpenAiImageService.QualityPreset.AVERAGE

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
        this.qualityPreset,
        this.generationsNumber
    )

fun User.toEntity() =
    UserEntity().also {
        it.userId = this.userId
        it.userState = this.userState
        it.userPrompt = this.userPrompt
        it.photos = this.photos.map { it.toEntity() }
        it.qualityPreset = this.qualityPreset
        it.generationsNumber = this.generationsNumber
    }
