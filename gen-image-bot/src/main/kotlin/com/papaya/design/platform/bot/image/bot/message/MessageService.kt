package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.types.TelegramBotResult
import com.papaya.design.platform.bot.image.bot.user.UserService
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.static.START_INTERIOR_3D_RENDER_TEXT
import com.papaya.design.platform.bot.image.bot.static.WELCOME_MESSAGE_TEXT
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class MessageService(
    private val userService: UserService,
) {
    fun sendFirstTimeWelcome(
        bot: Bot,
        chatId: Long,
    ) {
        userService.getUser(chatId).userState = UserState.READY_FOR_CMD

        val welcomeResult = bot.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = WELCOME_MESSAGE_TEXT,
            replyMarkup = createMainKeyboard()
        )
    }

    fun sendWaitingForPhotoFor3DRenderMessage(
        bot: Bot,
        chatId: Long,
        cmd: TelegramCommand,
    ) {
        userService.getUser(chatId).userState = UserState.WAITING_FOR_PHOTO

        val result = bot.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = START_INTERIOR_3D_RENDER_TEXT,
            replyMarkup = removeKeyboard()
        )
        result.fold({
            log.info("User $chatId is now waiting for image")
        }, { e ->
            logErrorInCommand(cmd, e)
            userService.getUser(chatId).userState = UserState.READY_FOR_CMD
        })
    }

    private fun logErrorInCommand(
        cmd: TelegramCommand,
        e: TelegramBotResult.Error
    ) {
        log.error("Error in ${cmd.text} command: $e")
    }
}