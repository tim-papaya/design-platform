package com.papaya.design.platform.bot.tg.core.command.input

const val CUSTOM_MODE = "Свой вариант"

interface UserInputSelectingMode {
    val textShowingToUser: String
    val data: String
}