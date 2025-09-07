package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.types.TelegramBotResult
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.user.UserService
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
    ): User {
        val user = userService.addUser(chatId)
        bot.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = General.Text.WELCOME_MESSAGE,
            replyMarkup = createMainKeyboard()
        )
        return user
    }

    fun sendWaitingForPhotoMessage(
        bot: Bot,
        chatId: Long,
        commandState: StartWaitingForImageCommandState,
    ) {
        userService.getUser(chatId).userState = commandState.newState

        val result = bot.sendMessage(
            chatId = ChatId.Companion.fromId(chatId),
            text = commandState.textToShow,
            replyMarkup = removeKeyboard()
        )
        result.fold({
            log.info("User $chatId is now waiting for image")
        }, { e ->
            logErrorInCommand(commandState.cmd, e)
            userService.getUser(chatId).userState = commandState.stateToReturn
        })
    }


    private fun logErrorInCommand(
        cmd: TelegramCommand,
        e: TelegramBotResult.Error
    ) {
        log.error("Error in ${cmd.text} command: $e")
    }

    fun sendGenerationCompletionMessage(bot: Bot, chatId: Long, successMessage: String, ) {
        userService.getUser(chatId).userState = UserState.READY_FOR_CMD
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = General.Text.NEXT_STEP,
            replyMarkup = createMainKeyboard()
        )
        log.info("$successMessage in $chatId")
    }

    fun sendErrorMessage(bot: Bot, chatId: Long, errorMessage: String, e: Exception) {
        sendError(chatId, bot)
        log.error("$errorMessage: ${e.message}", e)
    }

    fun sendErrorMessage(bot: Bot, chatId: Long, errorMessage: String) {
        sendError(chatId, bot)
        log.error(errorMessage)
    }

    private fun sendError(chatId: Long, bot: Bot) {
        userService.getUser(chatId).userState = UserState.READY_FOR_CMD

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = Error.Text.ERROR_ON_PROCESSING_IMAGE,
            replyMarkup = createMainKeyboard()
        )
    }
}