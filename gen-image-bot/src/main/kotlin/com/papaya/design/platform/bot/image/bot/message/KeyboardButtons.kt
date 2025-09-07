package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton.*

enum class KeyboardInputButton(val text: String) {
    GENERATE_REALISTIC_INTERIOR("🖼️ 3D-визуализация по коллажу"),
    GENERATE_EXTENDED_REALISTIC_INTERIOR("🏡 Обновление по вашему фото или описанию"),
    ROOM_UPGRADE("🔼 Обновление с помощью ИИ-алгоритмов"),
    START("✨ Начать"),
    OPTION_FOR_SELF("🏠 Для себя"),
    OPTION_FOR_RENT("💲 Для аренды"),
    CANCEL("Назад"),
    EXTENDED_REALISTIC_INTERIOR_READY_FOR_GENERATION("✨ Фото отправлены")
}

fun createMainKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(GENERATE_REALISTIC_INTERIOR.text),
                KeyboardButton(GENERATE_EXTENDED_REALISTIC_INTERIOR.text),
                KeyboardButton(ROOM_UPGRADE.text),
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun roomUpgrade(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(OPTION_FOR_SELF.text),
                KeyboardButton(OPTION_FOR_RENT.text),
                KeyboardButton(CANCEL.text),
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun prepareForExtendedRealisticGeneration(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(EXTENDED_REALISTIC_INTERIOR_READY_FOR_GENERATION.text),
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun removeKeyboard(): ReplyKeyboardRemove {
    return ReplyKeyboardRemove(removeKeyboard = true)
}