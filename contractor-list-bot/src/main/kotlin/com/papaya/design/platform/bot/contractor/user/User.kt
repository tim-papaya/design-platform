package com.papaya.design.platform.bot.contractor.user

data class User(
    val userId: Long,
    val userState: ContractorUserState,
    val category: String?,
)
