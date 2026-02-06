package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.input.VideoUserInputSelectingMode
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton.*
import com.papaya.design.platform.bot.image.bot.payment.PaymentAmount

enum class KeyboardInputButton(val text: String) {
    GENERATE_REALISTIC_INTERIOR("🖼️ 3D-визуализация по коллажу"),
    ROTATE_OBJECT("🔄 Поворот объекта"),
    GENERATE_REALISTIC_INTERIOR_BATCH("📦(Дизайнер) 3D-визуализация пачкой"),
    GENERATE_EXTENDED_REALISTIC_INTERIOR("🏡 Обновление по вашему фото или описанию"),
    ROOM_UPGRADE("🔼 Декорирование с помощью ИИ-алгоритмов"),
    PLANNED_REALISTIC_INTERIOR("📋 3D-визуализация по мудборду"),
    START("✨ Начать"),
    OPTION_FOR_SELF("🏠 Для себя"),
    OPTION_FOR_RENT("💲 Для аренды"),
    CANCEL("⬆ В главное меню"),
    EXTENDED_REALISTIC_INTERIOR_READY_FOR_GENERATION("✨ Готово"),
    KITCHEN("Кухня"),
    BEDROOM("Спальня"),
    GUESTROOM("Гостиная"),
    SUPPORT("🩹 Сообщить о проблеме"),
    PAYMENT("💲 Купить генерации"),
    CHECK_STATUS("💸 Проверить баланс"),
    GENERATE_VIDEO("🎬 Видео по вашему фото"),
}

fun createMainKeyboard(user: User): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard =
            listOf(
                listOf(KeyboardButton(GENERATE_REALISTIC_INTERIOR.text)),
                listOf(KeyboardButton(ROTATE_OBJECT.text)),
                listOf(KeyboardButton(GENERATE_EXTENDED_REALISTIC_INTERIOR.text)),
                listOf(KeyboardButton(ROOM_UPGRADE.text)),
                listOf(KeyboardButton(PLANNED_REALISTIC_INTERIOR.text)),
                listOf(KeyboardButton(GENERATE_VIDEO.text)),
                listOf(KeyboardButton(PAYMENT.text)),
                listOf(KeyboardButton(CHECK_STATUS.text)),
                listOf(KeyboardButton(SUPPORT.text)),
            ).let {
                if (!user.isDesigner) it else {
                    listOf(listOf(KeyboardButton(GENERATE_REALISTIC_INTERIOR_BATCH.text))) + it
                }
            },
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

fun realisticInteriorBatchKeyboard(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(KeyboardButton(START.text)),
            listOf(KeyboardButton(CANCEL.text))
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

fun videoModes(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = VideoUserInputSelectingMode.entries
            .map { listOf(KeyboardButton(it.textShowingToUser)) }
            .plus(listOf(listOf(KeyboardButton(CANCEL.text)))),
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

fun paymentKeyboard(): KeyboardReplyMarkup {
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
