package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.papaya.design.platform.bot.image.bot.domain.UserState.*
import com.papaya.design.platform.bot.image.bot.message.ImageMessageService
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton.GENERATE_INTERIOR
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton.START
import com.papaya.design.platform.bot.image.bot.message.MessageService
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.REAL_IMAGE_CMD
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.START_CMD
import com.papaya.design.platform.bot.image.bot.message.removeKeyboard
import com.papaya.design.platform.bot.image.bot.static.IMAGE_STILL_GENERATING_TEXT
import com.papaya.design.platform.bot.image.bot.user.UserService
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    private val userService: UserService,
    private val messageService: MessageService,
    private val imageMessageService: ImageMessageService,
) : BotService {

    private val bot = bot {
        token = apiKey
        dispatch {
            command(START_CMD.text) {
                val chatId = message.chat.id
                messageService.sendFirstTimeWelcome(bot, chatId)
            }
            command(REAL_IMAGE_CMD.text) {
                val chatId = message.chat.id
                messageService.sendWaitingForPhotoFor3DRenderMessage(bot, chatId, REAL_IMAGE_CMD)
            }
            command(TelegramCommand.REAL_IMAGE_EXT_CMD.text) {
                val chatId = message.chat.id
            }

            message {
                val chatId = message.chat.id
                val messageText = message.text

                when (userService.getUser(chatId).userState) {
                    NEW_USER -> {
                        messageService.sendFirstTimeWelcome(bot, chatId)
                    }

                    WAITING_FOR_PHOTO -> {
                        if (message.photo != null) {
                            imageMessageService.handlePhotoMessage(bot, chatId, message.photo!!)
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Пожалуйста, пришлите изображение для генерации реалистичного интерьера."
                            )
                        }
                    }

                    WAITING_FOR_END_OF_PHOTO_GENERATION -> {
                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = IMAGE_STILL_GENERATING_TEXT,
                            replyMarkup = removeKeyboard()
                        )
                    }

                    READY_FOR_CMD -> {
                        // Keyboard input applying
                        when (messageText) {
                            START.text -> {
                                messageService.sendFirstTimeWelcome(bot, chatId)
                            }

                            GENERATE_INTERIOR.text -> {
                                messageService.sendWaitingForPhotoFor3DRenderMessage(bot, chatId, REAL_IMAGE_CMD)
                            }
                        }
                    }
                }
            }
        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }
}
