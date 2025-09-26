package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
@Profile("mock")
class MockBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
) : BotService {
    private val bot = bot {
        token = apiKey
        dispatch {
            message {
                bot.sendMessage(ChatId.fromId(message.chat.id), "Привет! У меня сегодня выходной, я постараюсь как можно быстрее снова вернуться к творчеству!🙂")
            }
        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }
}