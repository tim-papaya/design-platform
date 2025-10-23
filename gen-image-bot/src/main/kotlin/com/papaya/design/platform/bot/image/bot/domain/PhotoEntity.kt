package com.papaya.design.platform.bot.image.bot.domain

import com.papaya.design.platform.ai.photo.Photo
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "photos")
class PhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var uniqueId: Long = 0

    lateinit var fileId: String
    lateinit var fileUniqueId: String

    var width: Int = 0
    var height: Int = 0
}

fun PhotoEntity.toModel() =
    Photo(
        this.fileId,
        this.fileUniqueId,
        this.width,
        this.height
    )

fun Photo.toEntity() =
    PhotoEntity().also {
        it.fileId = this.fileId
        it.fileUniqueId = this.fileUniqueId
        it.width = this.width
        it.height = this.height
    }
