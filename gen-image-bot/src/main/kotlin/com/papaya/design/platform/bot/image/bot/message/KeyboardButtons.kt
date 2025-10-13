package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton.*
import com.papaya.design.platform.bot.image.bot.payment.PaymentAmount

enum class KeyboardInputButton(val text: String) {
    GENERATE_REALISTIC_INTERIOR("🖼️ 3D-визуализация по коллажу"),
    GENERATE_EXTENDED_REALISTIC_INTERIOR("🏡 Обновление по вашему фото или описанию"),
    ROOM_UPGRADE("🔼 Обновление с помощью ИИ-алгоритмов"),
    PLANNED_REALISTIC_INTERIOR("📋 3D-визуализация по мудборду"),
    START("✨ Начать"),
    OPTION_FOR_SELF("🏠 Для себя"),
    OPTION_FOR_RENT("💲 Для аренды"),
    CANCEL("⬆ В главное меню"),
    EXTENDED_REALISTIC_INTERIOR_READY_FOR_GENERATION("✨ Готово"),
    KITCHEN("Столовая"),
    BEDROOM("Спальня"),
    GUESTROOM("Гостиная"),
    SUPPORT("🩹 Сообщить о проблеме"),
    PAYMENT("💲 Купить"),
    CHECK_STATUS("Проверить баланс")
}

fun createMainKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(KeyboardButton(GENERATE_REALISTIC_INTERIOR.text)),
            listOf(KeyboardButton(GENERATE_EXTENDED_REALISTIC_INTERIOR.text)),
            listOf(KeyboardButton(ROOM_UPGRADE.text)),
            listOf(KeyboardButton(PLANNED_REALISTIC_INTERIOR.text)),
            listOf(KeyboardButton(PAYMENT.text)),
            listOf(KeyboardButton(SUPPORT.text)),
            listOf(KeyboardButton(CHECK_STATUS.text)),
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = true
    )
}

fun roomUpgrade(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(KeyboardButton(OPTION_FOR_SELF.text)),
            listOf(
                KeyboardButton(OPTION_FOR_RENT.text),
                KeyboardButton(CANCEL.text)
            ),
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = true
    )
}

fun prepareForExtendedRealisticGeneration(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(EXTENDED_REALISTIC_INTERIOR_READY_FOR_GENERATION.text),
                KeyboardButton(CANCEL.text),
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun onlyBackKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton(CANCEL.text),
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = true
    )
}

fun removeKeyboard(): ReplyKeyboardRemove {
    return ReplyKeyboardRemove(removeKeyboard = true)
}

fun planedKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(KeyboardButton(KITCHEN.text)),
            listOf(KeyboardButton(BEDROOM.text)),
            listOf(KeyboardButton(GUESTROOM.text)),
            listOf(KeyboardButton(CANCEL.text)),
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = true
    )
}

fun paymentKeyboard() : KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(KeyboardButton(PaymentAmount.LOWEST_GENERATION_PACKET.label)),
            listOf(KeyboardButton(PaymentAmount.LOW_GENERATION_PACKET.label)),
            listOf(KeyboardButton(PaymentAmount.AVERAGE_GENERATION_PACKET.label)),
            listOf(KeyboardButton(PaymentAmount.ABOVE_AVERAGE_PACKET.label)),
            listOf(KeyboardButton(PaymentAmount.LARGE_PACKET.label)),
            listOf(KeyboardButton(CANCEL.text)),
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = true
    )
}