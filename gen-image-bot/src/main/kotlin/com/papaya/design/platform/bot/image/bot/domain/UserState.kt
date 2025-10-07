package com.papaya.design.platform.bot.image.bot.domain

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.papaya.design.platform.bot.image.bot.message.paymentKeyboard
import com.papaya.design.platform.bot.image.bot.static.Payment

enum class UserState(
    val messageText: String = "",
    val replyMarkup: ReplyMarkup = KeyboardReplyMarkup(),
) {
    READY_FOR_CMD,
    WAITING_FOR_END_OF_PHOTO_GENERATION,

    REALISTIC_INTERIOR_WAITING_FOR_PHOTO,

    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT,
    EXTENDED_REALISTIC_INTERIOR_WAITING_ADDITIONAL_PHOTO,

    ROOM_UPGRADE_WAITING_FOR_PHOTO,
    ROOM_UPGRADE_WAITING_FOR_USER_OPTION,

    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PLAN,
    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_USER_OPTION,

    SELECTING_PAYMENT_OPTION(Payment.Text.SELECT_PAYMENT_OPTION, paymentKeyboard()),
}
