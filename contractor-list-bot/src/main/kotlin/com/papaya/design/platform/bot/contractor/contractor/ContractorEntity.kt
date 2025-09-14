package com.papaya.design.platform.bot.contractor.contractor

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "contractors")
class ContractorEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    @Column(nullable = false)
    lateinit var name: String

    @Column
    var phone: String? = null

    @Column
    var link: String? = null

    @Column(nullable = false)
    var addedByUserId: Long = 0

    @Column
    var comment: String? = null

    @Column(nullable = false)
    lateinit var category: String
}

fun ContractorEntity.toModel(): Contractor =
    Contractor(
        name = name,
        phone = phone,
        link = link,
        addedByUserId = addedByUserId,
        comment = comment,
        category = category
    )

fun Contractor.toEntity(): ContractorEntity =
    ContractorEntity().also {
        it.name = this.name
        it.phone = this.phone
        it.link = this.link
        it.addedByUserId = this.addedByUserId
        it.comment = this.comment
    }
