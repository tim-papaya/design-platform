package com.papaya.design.platform.bot.image.bot.domain

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.papaya.design.platform.bot.image.bot.message.onlyBackKeyboard
import com.papaya.design.platform.bot.image.bot.message.realisticInteriorBatchKeyboard
import com.papaya.design.platform.bot.image.bot.message.paymentKeyboard
import com.papaya.design.platform.bot.image.bot.message.videoModes
import com.papaya.design.platform.bot.image.bot.static.Payment
import com.papaya.design.platform.bot.image.bot.static.ObjectRotation
import com.papaya.design.platform.bot.image.bot.static.Support
import com.papaya.design.platform.bot.image.bot.static.Video
import com.papaya.design.platform.bot.image.bot.static.RealisticInteriorBatch
import com.papaya.design.platform.bot.tg.core.command.workflow.StepEnum

enum class UserState(
    val messageText: String = "",
    val replyMarkup: ReplyMarkup = KeyboardReplyMarkup(),
) : StepEnum {
    READY_FOR_CMD,
    WAITING_FOR_END_OF_PHOTO_GENERATION,

    REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
    ROTATION_OBJECT_WAITING_FOR_PHOTO,
    ROTATION_OBJECT_WAITING_FOR_USER_PROMPT(
        ObjectRotation.Text.WAITING_FOR_USER_PROMPT,
        onlyBackKeyboard()
    ),
    REALISTIC_INTERIOR_BATCH_WAITING_FOR_PHOTO(
        RealisticInteriorBatch.Text.WAITING_FOR_BATCH_IMAGE,
        realisticInteriorBatchKeyboard()
    ),

    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT,
    EXTENDED_REALISTIC_INTERIOR_WAITING_ADDITIONAL_PHOTO,

    ROOM_UPGRADE_WAITING_FOR_PHOTO,
    ROOM_UPGRADE_WAITING_FOR_USER_OPTION,

    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PLAN,
    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_USER_OPTION,

    VIDEO_WAITING_FOR_PHOTO(Video.Text.WAITING_IMAGE, onlyBackKeyboard()),
    VIDEO_WAITING_FOR_FOR_USER_PROMPT(Video.Text.WAITING_USER_PROMPT, onlyBackKeyboard()),
    VIDEO_WAITING_FOR_FOR_USER_SELECTING_MODE(Video.Text.WAITING_USER_SELECTING_MODE, videoModes()),

    SELECTING_PAYMENT_OPTION(Payment.Text.SELECT_PAYMENT_OPTION, paymentKeyboard()),

    CONFIRMING_SUPPORT_MESSAGE(Support.Text.CONFIRM_SUPPORT_MESSAGE, onlyBackKeyboard())
}
