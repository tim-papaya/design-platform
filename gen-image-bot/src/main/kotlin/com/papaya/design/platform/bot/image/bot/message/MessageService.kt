package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.network.ResponseError
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.bot.image.bot.domain.Photo
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.user.UserService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Lazy
@Service
class MessageService(
    private val userService: UserService,
    private val imageLoader: ExamplesLocalImageLoader,
    private val bot: Bot
    ) {

    fun sendFirstTimeWelcome(
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
        id: TelegramId,
        commandState: ImageGenerationStrategy,
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
            log.error(e.exception) { "Error in $commandState, error: ${e.errorBody}" }
            userService.saveUser(id.userId) { u ->
                u.userState = commandState.stateToReturn
            }
        })
    }

    fun sendGenerationCompletionMessage(
        id: TelegramId,
        successMessage: String
    ) {
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

    fun sendErrorMessage(id: TelegramId, internalErrorMessage: String, e: Exception) {
        sendMessageAndReturnToMainMenu(id, Error.Text.ERROR_ON_PROCESSING_IMAGE)
        log.error("$internalErrorMessage: ${e.message}", e)
    }

    fun sendErrorMessage(id: TelegramId, internalErrorMessage: String) {
        sendMessageAndReturnToMainMenu(id, Error.Text.ERROR_ON_PROCESSING_IMAGE)
        log.error(internalErrorMessage)
    }

    fun sendWarningMessage(id: TelegramId, warningMessage: String) {
        sendMessageAndReturnToMainMenu(id, warningMessage)
    }

    private fun sendMessageAndReturnToMainMenu(id: TelegramId, message: String) {
        userService.saveUser(id.userId) { u ->
            u.userState = UserState.READY_FOR_CMD
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = message,
            replyMarkup = createMainKeyboard()
        )
    }

    fun sendMessageOnWaitingForPhoto(
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

    fun sendStateMessage(id: TelegramId, userState: UserState) {
        userService.saveUser(id.userId) { u ->
            u.userState = userState
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = userState.messageText,
            replyMarkup = userState.replyMarkup
        )
    }
}