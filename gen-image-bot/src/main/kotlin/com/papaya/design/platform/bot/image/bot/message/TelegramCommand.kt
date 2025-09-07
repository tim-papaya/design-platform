package com.papaya.design.platform.bot.image.bot.message

enum class TelegramCommand(
    val text: String
){
    START_CMD("start"),
    REAL_IMAGE_CMD("gen_real"),
    EXT_REAL_IMAGE_CMD("gen_real_ext"),
    ROOM_UPGRADE_CMD("room_upgrade"),
    NO_COMMAND("no_command"),
}
