package com.papaya.design.platform.bot.tg.core.command

enum class GeneralTelegramCommand(
    override val cmdText: String,
    override val btnText: String
) : TelegramCommand {
    START_CMD("start", "Начать"),
    SUPPORT("support", "Поддержка"),
    BACK("back", "Назад"),
    NEXT("next", "Далее"),
    MAIN_MENU("main_menu", "В главное меню"),
}
