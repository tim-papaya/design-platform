package com.papaya.design.platform.bot.contractor.contractor

data class Contractor(
    val name: String,
    val phone: String?,
    val link: String?,
    val addedByUserId: Long,
    val comment: String?,
    val category: String
)
