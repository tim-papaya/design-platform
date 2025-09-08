package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.Message
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

fun Message.userId(): Long {
    return this.from?.id.let {
        if (it == null) {
            val s = "User id is null in chat - ${this.chat.id}"
            log.error { s }
            throw IllegalStateException(s)
        } else it
    }
}

fun Message.telegramId() : TelegramId =
    TelegramId(this.chat.id, this.userId())