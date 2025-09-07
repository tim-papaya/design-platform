package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton.*

enum class KeyboardInputButton(val text: String) {
    GENERATE_INTERIOR("🖼️ Генерация интерьера"),
    START("✨ Начать"),
}

fun createMainKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(GENERATE_INTERIOR.text)
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun welcomeKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(START.text),
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun removeKeyboard(): ReplyKeyboardRemove {
    return ReplyKeyboardRemove(removeKeyboard = true)
}