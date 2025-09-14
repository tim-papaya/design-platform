package com.papaya.design.platform.bot.contractor.tg.jpa.user

data class User(
    val userId: Long,
    val userState: ContractorUserState,
    val category: String?,
)
