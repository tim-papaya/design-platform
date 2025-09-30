package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.network.ResponseError
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.openai.OpenAiImageService
import com.papaya.design.platform.bot.image.bot.domain.Photo
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.UserState.ROOM_UPGRADE_WAITING_FOR_USER_OPTION
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.user.UserService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class MessageService(
    private val userService: UserService,
    private val imageLoader: ExamplesLocalImageLoader
) {

    fun sendFirstTimeWelcome(
        bot: Bot,
        userId: Long,
    ): User {
        val user = userService.saveUser(userId)

        bot.sendMessage(
            chatId = ChatId.Companion.fromId(userId),
            text = General.Text.WELCOME_MESSAGE,
            replyMarkup = createMainKeyboard()
        )
        return user
    }

    fun sendWaitingForPhotoMessage(
        bot: Bot,
        id: TelegramId,
        commandState: StartWaitingForImageCommandState,
    ) {
        userService.saveUser(id.userId) { u ->
            u.userState = commandState.newState
        }

        val result = bot.sendPhoto(
            chatId = ChatId.fromId(id.chatId),
            caption = commandState.textToShow,
            replyMarkup = onlyBackKeyboard(),
            photo = TelegramFile.ByByteArray(
                fileBytes = imageLoader.loadImage(commandState.exampleImages.first()),
                filename = "example_interior_${System.currentTimeMillis()}.jpeg"
            ),
        )
        result.fold({
            log.info("User $id is now waiting for image")
        }, { e ->
            logErrorInCommand(commandState.cmd, e)
            userService.saveUser(id.userId) { u ->
                u.userState = commandState.stateToReturn
            }
        })
    }


    private fun logErrorInCommand(
        cmd: TelegramCommand,
        e: ResponseError
    ) {
        log.error("Error in ${cmd.text} command: $e")
    }

    fun sendGenerationCompletionMessage(bot: Bot, id: TelegramId, successMessage: String) {
        userService.saveUser(id.userId) { u ->
            u.userState = UserState.READY_FOR_CMD
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = General.Text.NEXT_STEP,
            replyMarkup = createMainKeyboard()
        )
        log.info("$successMessage in $id")
    }

    fun sendErrorMessage(bot: Bot, id: TelegramId, errorMessage: String, e: Exception) {
        sendError(id, bot)
        log.error("$errorMessage: ${e.message}", e)
    }

    fun sendErrorMessage(bot: Bot, id: TelegramId, errorMessage: String) {
        sendError(id, bot)
        log.error(errorMessage)
    }

    private fun sendError(id: TelegramId, bot: Bot) {
        userService.saveUser(id.userId) { u ->
            u.userState = UserState.READY_FOR_CMD
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = Error.Text.ERROR_ON_PROCESSING_IMAGE,
            replyMarkup = createMainKeyboard()
        )
    }

    fun sendMessageOnWaitingForPhoto(
        bot: Bot,
        id: TelegramId,
        photos: List<Photo>?,
        waitingPhotoState: WaitingPhotoState
    ) {
        if (photos != null) {
            // TODO Add validation
            userService.saveUser(id.userId) { u ->
                u.photos += photos.map { it.toEntity() }
                u.userState = waitingPhotoState.newState
            }

            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = waitingPhotoState.messageText,
                replyMarkup = waitingPhotoState.nextKeyboardMarkup
            )

            log.info("Added photo for interior generation")
        } else {
            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = waitingPhotoState.errorText,
                replyMarkup = onlyBackKeyboard()
            )
        }
    }
}