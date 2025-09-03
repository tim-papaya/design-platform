package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

private const val REAL_IMAGE_CMD = "real-image"
private const val HELLO_CMD = "hello"

@Service
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String
) : BotService {
    private val bot = bot {
        token = apiKey
        dispatch {
            command(HELLO_CMD) {
                val result = bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Привет!")
                result.fold({
                    // do nothing
                }, { e ->
                    log.info("Error in $HELLO_CMD command")
                })
            }
            command(REAL_IMAGE_CMD) {
                val result = bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Начнем генерацию реалистичного интерьера, пришли фото, чтобы начать."
                )
                result.fold({
                    it.text
                }, { e ->
                    log.info("Error in $REAL_IMAGE_CMD command")
                })
            }

        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }
}