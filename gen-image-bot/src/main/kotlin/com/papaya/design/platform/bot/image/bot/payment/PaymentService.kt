package com.papaya.design.platform.bot.image.bot.payment

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.papaya.design.platform.bot.image.bot.message.TelegramId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Lazy
@Service
class PaymentService(
    private val bot: Bot
) {

    fun hasAvailableGenerations(id: TelegramId): Boolean {
        return true
    }

    fun sendInvoice(id: TelegramId) {
        TODO("Not yet implemented")
//        bot.sendInvoice(ChatId.fromId(id.chatId), )
    }
}
