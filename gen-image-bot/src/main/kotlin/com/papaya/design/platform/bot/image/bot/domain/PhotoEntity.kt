package com.papaya.design.platform.bot.image.bot.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "photos")
class PhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var uniqueId: Long = 0

    lateinit var fileId: String
    lateinit var fileUniqueId: String
}

fun PhotoEntity.toModel() =
    Photo(
        this.fileId,
        this.fileUniqueId
    )

fun Photo.toEntity() =
    PhotoEntity().also {
        it.fileId = this.fileId
        it.fileUniqueId = this.fileUniqueId
    }