package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.static.PlannedRealisticInterior

data class WaitingPhotoState(
    val newState: UserState,
    val messageText: String,
    val nextKeyboardMarkup: ReplyMarkup,
    val errorText: String
) {
    companion object {
        val PLANED_BEFORE_PLAN = WaitingPhotoState(
            UserState.PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PLAN,
            PlannedRealisticInterior.Text.WAITING_FOR_PLAN,
            planedKeyboard(),
            PlannedRealisticInterior.Text.WAITING_FOR_IMAGE
        )
        val PLANED_BEFORE_OPTIONS = WaitingPhotoState(
            UserState.PLANNED_REALISTIC_INTERIOR_WAITING_FOR_USER_OPTION,
            PlannedRealisticInterior.Text.WAITING_FOR_OPTION,
            onlyBackKeyboard(),
            PlannedRealisticInterior.Text.WAITING_FOR_PLAN
        )
    }
}