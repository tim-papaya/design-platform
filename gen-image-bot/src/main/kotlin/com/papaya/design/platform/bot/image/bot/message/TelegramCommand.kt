package com.papaya.design.platform.bot.image.bot.message

enum class TelegramCommand(
    val text: String
){
    START_CMD("start"),
    REAL_IMAGE_CMD("gen_real"),
    REAL_IMAGE_EXT_CMD("gen_real_ext"),
    NO_COMMAND("no_command"),
}