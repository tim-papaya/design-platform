package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaDocument
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.network.fold
import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserEntity
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.static.Rules.POLICY_FILE_NAME
import com.papaya.design.platform.bot.image.bot.static.Rules.RULES_FILE_NAME
import com.papaya.design.platform.bot.image.bot.user.UserService
import mu.KotlinLogging
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Lazy
@Service
class MessageService(
    private val userService: UserService,
    private val fileLoader: ExamplesLocalFileLoader,
    private val bot: Bot
) {

    fun sendFirstTimeWelcome(
        userId: TelegramId,
    ): User {
        val user = userService.saveUser(userId)

        bot.sendMessage(
            chatId = ChatId.Companion.fromId(userId.userId),
            text = General.Text.WELCOME_MESSAGE,
            replyMarkup = createMainKeyboard(user)
        )
        return user
    }

    fun sendWaitingForPhotoMessage(
        id: TelegramId,
        commandState: ImageGenerationStrategy,
    ) {
        userService.saveUser(id) { u ->
            u.userState = commandState.newState
        }

        val result = bot.sendPhoto(
            chatId = ChatId.fromId(id.chatId),
            caption = commandState.textToShow,
            replyMarkup = commandState.replyMarkup,
            photo = TelegramFile.ByByteArray(
                fileBytes = fileLoader.loadFile(commandState.exampleImages.first()),
                filename = "example_interior_${System.currentTimeMillis()}.jpeg"
            ),
        )
        result.fold({
            log.info("User $id is now waiting for image")
        }, { e ->
            log.error(e.exception) { "Error in $commandState, error: ${e.errorBody}" }
            userService.saveUser(id) { u ->
                u.userState = commandState.stateToReturn
            }
        })
    }

    fun sendGenerationCompletionMessage(
        id: TelegramId,
        successMessage: String,
        generationCount: Int = 1
    ) {
        val user = userService.saveUser(id) { u ->
            u.userState = UserState.READY_FOR_CMD
            u.generations -= generationCount
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = General.Text.NEXT_STEP,
            replyMarkup = createMainKeyboard(user)
        )
        log.info("$successMessage in $id")
    }

    fun sendErrorMessage(
        id: TelegramId,
        internalErrorMessage: String,
        e: Exception,
        textToUser: String = Error.Text.ERROR_ON_PROCESSING_IMAGE
    ) {
        sendMessageAndReturnToMainMenu(id, textToUser)
        log.error("$internalErrorMessage: ${e.message}", e)
    }

    fun sendErrorMessage(
        id: TelegramId,
        internalErrorMessage: String,
        textToUser: String = Error.Text.ERROR_ON_PROCESSING_IMAGE
    ) {
        sendMessageAndReturnToMainMenu(id, textToUser)
        log.error(internalErrorMessage)
    }

    fun sendWarningMessage(id: TelegramId, warningMessage: String) {
        sendMessageAndReturnToMainMenu(id, warningMessage)
    }

    fun sendMessageAndReturnToMainMenu(id: TelegramId, message: String) {
        val user = userService.saveUser(id) { u ->
            u.userState = UserState.READY_FOR_CMD
            u.photos = listOf()
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = message,
            replyMarkup = createMainKeyboard(user)
        )
    }

    fun sendMessageOnWaitingForPhoto(
        id: TelegramId,
        photos: List<Photo>?,
        waitingPhotoState: WaitingPhotoState
    ) {
        if (photos != null) {
            // TODO Add validation
            userService.saveUser(id) { u ->
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

    fun sendStateMessage(id: TelegramId, userState: UserState, userAction: (UserEntity) -> Unit = {}) {
        userService.saveUser(id) { u ->
            u.userState = userState
            userAction.invoke(u)
        }

        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = userState.messageText,
            replyMarkup = userState.replyMarkup
        )
    }

    fun sendMessage(id: TelegramId, message: String) {
        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = message,
        )
    }

    fun sendDocument(id: TelegramId, localFile: LocalFile, showedFileName: String) {
        val doc = fileLoader.loadFile(localFile)
        bot.sendDocument(ChatId.fromId(id.chatId), TelegramFile.ByByteArray(doc, showedFileName))
    }

    fun sendRules(id: TelegramId) {
        sendMessageAndReturnToMainMenu(id, General.Text.RULES_TEXTS)

        sendDocument(id, LocalFile.RULES_OF_USE, RULES_FILE_NAME)
        sendDocument(id, LocalFile.CONFIDENTIAL_POLICY, POLICY_FILE_NAME)

        log.info { "User $id accepted rules and policy" }

        userService.saveUser(id) { u ->
            u.isAcceptedRules = true
        }
    }
}
