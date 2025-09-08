package com.papaya.design.platform.bot.image.bot.message

enum class TelegramCommand(
    val text: String
){
    START_CMD("start"),
    REAL_IMAGE_CMD("gen_real"),
    EXT_REAL_IMAGE_CMD("gen_real_ext"),
    PLANED_REAL_IMAGE_CMD("gen_real_planed"),
    ROOM_UPGRADE_CMD("room_upgrade"),
    LOW_QUALITY("low_quality"),
    HIGH_QUALITY("high_quality"),
    AVERAGE_QUALITY("average_quality")
}
