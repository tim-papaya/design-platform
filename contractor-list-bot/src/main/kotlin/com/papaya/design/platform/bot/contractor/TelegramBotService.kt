package com.papaya.design.platform.bot.contractor

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.papaya.design.platform.bot.contractor.command.ChangeFieldState
import com.papaya.design.platform.bot.contractor.contractor.ContractorEntity
import com.papaya.design.platform.bot.contractor.contractor.ContractorService
import com.papaya.design.platform.bot.contractor.user.ContractorUserState
import com.papaya.design.platform.bot.contractor.user.User
import com.papaya.design.platform.bot.contractor.user.UserService
import com.papaya.design.platform.bot.tg.core.command.GeneralTelegramCommand
import com.papaya.design.platform.bot.tg.core.command.message.TelegramId
import com.papaya.design.platform.bot.tg.core.command.message.telegramId
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
@Profile("prod")
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    @Value("\${support.admin.id}")
    private val supportId: Long,
    private val userService: UserService,
    private val contractorService: ContractorService
) : BotService {

    private val bot = bot {
        token = apiKey
        dispatch {
            command(GeneralTelegramCommand.START_CMD.cmdText) {
                val id = message.telegramId()
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Text.START,
                    replyMarkup = createMainMenuKeyboard()
                )
            }
            callbackQuery {
                val userId = callbackQuery.from.id
                try {
                    val messageText = callbackQuery.data ?: return@callbackQuery
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                    val id = TelegramId(userId, chatId)
                    val user = userService.getUserOrNull(id.userId) ?: userService.saveUser(id.userId)


                    checkInput(user, messageText, id)
                } catch (e: Exception) {
                    log.error(e) { "Error at callback thread" }
                    userService.saveUser(userId) { u ->
                        u.userState = ContractorUserState.READY_FOR_CMD
                    }
                }

            }
            message {
                val id = message.telegramId()
                try {
                    val messageText = message.text ?: return@message
                    val user = userService.getUserOrNull(id.userId) ?: userService.saveUser(id.userId)

                    if (messageText == GeneralTelegramCommand.MAIN_MENU.btnText) {
                        userService.saveUser(id.userId) { u ->
                            u.userState = ContractorUserState.READY_FOR_CMD
                        }

                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Text.NEXT_STEP,
                            replyMarkup = createMainMenuKeyboard()
                        )
                        return@message
                    }

                    if (messageText == "/${GeneralTelegramCommand.START_CMD.cmdText}") {
                        return@message
                    }

                    checkInput(user, messageText, id)

                } catch (e: Exception) {
                    log.error(e) { "Error at message thread" }
                    userService.saveUser(id.userId) { u ->
                        u.userState = ContractorUserState.READY_FOR_CMD
                    }
                }
            }
        }
    }

    private fun checkInput(
        user: User,
        messageText: String,
        id: TelegramId
    ) {
        when (user.userState) {
            ContractorUserState.READY_FOR_CMD -> {
                when (messageText) {
                    ContractorUserState.ADD_NAME.name -> {
                        userService.saveUser(id.userId) { u ->
                            u.userState = ContractorUserState.ADD_NAME
                        }

                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Text.ADD_NAME,
                            replyMarkup = createNextStepAndBackMenu(
                                ContractorUserState.READY_FOR_CMD
                            )
                        )
                    }

                    ContractorUserState.CHOOSE_CATEGORY.name -> {
                        userService.saveUser(id.userId) { u ->
                            u.userState = ContractorUserState.CHOOSE_CATEGORY
                        }

                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Text.CHOOSE_CATEGORY,
                            replyMarkup = createListMarkup(contractorService.getCategories())
                        )
                    }

                    ContractorUserState.CHOOSE_FIELD_TO_EDIT.name -> {
                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Text.CHOOSE_FIELD_TO_EDIT,
                            replyMarkup = createMainMenuKeyboard()
                        )
                    }

                    else -> bot.sendMessage(
                        chatId = ChatId.fromId(id.chatId),
                        text = General.Text.NEXT_STEP,
                        replyMarkup = createMainMenuKeyboard()
                    )
                }
            }

            ContractorUserState.ADD_NAME -> {
                checkNameField(messageText, id)
            }

            ContractorUserState.ADD_CATEGORY -> {
                checkField(
                    messageText, id, ChangeFieldState.ADD_CATEGORY,
                    { c: ContractorEntity, s: String -> c.category = s },
                    errorReplyMarkup = {
                        createListMarkup(contractorService.getCategories())
                    })
            }

            ContractorUserState.ADD_PHONE -> {
                checkField(
                    messageText, id, ChangeFieldState.ADD_PHONE,
                    { c: ContractorEntity, s: String -> c.phone = s })
            }

            ContractorUserState.ADD_LINK -> {
                checkField(
                    messageText, id, ChangeFieldState.ADD_LINK,
                    { c: ContractorEntity, s: String -> c.link = s })
                checkThatPhoneOrLinkIsFilled(id)
            }

            ContractorUserState.ADD_COMMENT -> {
                checkField(
                    messageText, id, ChangeFieldState.ADD_COMMENT,
                    { c: ContractorEntity, s: String -> c.comment = s },
                    nextReplyMarkup = {
                        createNextStepAndBackMenu(
                            ContractorUserState.ADD_COMMENT,
                            ContractorUserState.PREPARE_FOR_FINISH_ADDING_CONTRACTOR
                        )
                    }
                )
            }

            ContractorUserState.PREPARE_FOR_FINISH_ADDING_CONTRACTOR -> {

                if (messageText == ContractorUserState.FINISH_ADDING_CONTRACTOR.name) {
                    userService.saveUser(id.userId) { u ->
                        u.userState = ContractorUserState.READY_FOR_CMD
                    }

                    if (!contractorService.saveDraftIfExists(id.userId)) {
                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Error.ERROR_ON_SAVING_CONTRACTOR,
                            replyMarkup = createMainMenuKeyboard()
                        )
                    } else {
                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Text.ADDED_NEW_CONTRACTOR,
                            replyMarkup = createMainMenuKeyboard()
                        )
                    }

                } else {
                    val contractor = contractorService.getContractor(id.userId)

                    bot.sendMessage(
                        chatId = ChatId.fromId(id.chatId),
                        text = "Новый подрядчик - $contractor",
                        replyMarkup = createNextStepAndBackMenu(
                            ContractorUserState.ADD_COMMENT,
                            ContractorUserState.FINISH_ADDING_CONTRACTOR
                        )
                    )
                }
            }

            ContractorUserState.FINISH_ADDING_CONTRACTOR -> {
                // do nothing here
            }

            ContractorUserState.CHOOSE_CATEGORY -> {
                log.info {"Choosing category $messageText"}
                if (contractorService.getCategories().contains(messageText)) {

                    userService.saveUser(id.userId) { u ->
                        u.userState = ContractorUserState.CHOOSE_CONTRACTOR
                        u.category = messageText
                    }
                    bot.sendMessage(
                        chatId = ChatId.fromId(id.chatId),
                        text = General.Text.CHOOSE_CONTRACTOR,
                        replyMarkup = createListMarkup(
                            contractorService.getContractorNamesByCategory(
                                messageText
                            )
                        )
                    )
                } else {
                    userService.saveUser(id.userId) { u ->
                        u.userState = ContractorUserState.READY_FOR_CMD
                    }
                    bot.sendMessage(
                        chatId = ChatId.fromId(id.chatId),
                        text = General.Error.ERROR_ON_CHOOSING_CATEGORY,
                        replyMarkup = createMainMenuKeyboard()
                    )
                }
            }

            ContractorUserState.CHOOSE_CONTRACTOR -> {
                val category = user.category!!
                val contractorName =
                    contractorService.getContractorNamesByCategory(category).find { it == messageText }
                if (contractorName != null) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(id.chatId),
                        text = "Подрядчик - ${contractorService.getContractor(contractorName)}",
                        replyMarkup = createListMarkup(
                            contractorService.getContractorNamesByCategory(
                                category
                            )
                        )
                    )
                } else {
                    userService.saveUser(id.userId) { u ->
                        u.userState = ContractorUserState.READY_FOR_CMD
                    }
                    bot.sendMessage(
                        chatId = ChatId.fromId(id.chatId),
                        text = General.Error.ERROR_ON_CHOOSING_CONTRACTOR,
                        replyMarkup = createMainMenuKeyboard()
                    )
                }
            }

            ContractorUserState.CHOOSE_FIELD_TO_EDIT -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Text.CHOOSE_FIELD_TO_EDIT,
                    replyMarkup = createMainMenuKeyboard()
                )
            }
        }
    }

    private fun checkThatPhoneOrLinkIsFilled(id: TelegramId) {
        val draft = contractorService.getContractor(id.userId)
        if (draft != null && draft.link == null && draft.phone == null) {
            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = General.Error.ERROR_EMPTY_MAIN_FIELDS,
                replyMarkup = createNextStepAndBackMenu(
                    ContractorUserState.ADD_NAME
                )
            )
        }
    }

    private fun checkNameField(
        messageText: String?,
        id: TelegramId
    ) {
        val name = messageText?.trim()
        when {

            name == ContractorUserState.READY_FOR_CMD.name -> {
                userService.saveUser(id.userId) { u ->
                    u.userState = ContractorUserState.READY_FOR_CMD
                }
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Text.NEXT_STEP,
                    replyMarkup = createMainMenuKeyboard()
                )
            }

            name == null || ContractorUserState.entries.map { it.name }.contains(name) -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Error.ERROR_EMPTY_FIELD,
                    replyMarkup = createNextStepAndBackMenu(
                        ContractorUserState.READY_FOR_CMD
                    )
                )
            }

            else -> {
                userService.saveUser(id.userId) { u ->
                    u.userState = ContractorUserState.ADD_CATEGORY
                }

                contractorService.createContractor(name, id.userId)

                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Text.ADD_CATEGORY,
                    replyMarkup = createNextStepAndBackMenu(
                        ContractorUserState.ADD_CATEGORY
                    )
                )
            }
        }
    }

    private fun checkField(
        messageText: String?,
        id: TelegramId,
        changeFieldState: ChangeFieldState,
        changeMapper: (ContractorEntity, String) -> Unit,
        errorReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.previousState
            )
        },
        nextReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.currentState
            )
        },
        previousReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.previousPreviousState
            )
        },

        ) {
        val fieldValue = messageText?.trim()
        when {
            fieldValue == ContractorUserState.READY_FOR_CMD.name -> {
                userService.saveUser(id.userId) { u ->
                    u.userState = ContractorUserState.READY_FOR_CMD
                }
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Text.NEXT_STEP,
                    replyMarkup = createMainMenuKeyboard()
                )
            }

            fieldValue == changeFieldState.previousState.name -> {
                userService.saveUser(id.userId) { u ->
                    u.userState = changeFieldState.previousState
                }
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = changeFieldState.previousText,
                    replyMarkup = previousReplyMarkup()
                )
            }

            !changeFieldState.isOptional &&
                    (fieldValue == null || ContractorUserState.entries.map { it.name }.contains(fieldValue)) -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Error.ERROR_EMPTY_FIELD,
                    replyMarkup = errorReplyMarkup.invoke()
                )
            }

            else -> {
                log.info { "Will add for ${id.userId}, value $fieldValue" }
                userService.saveUser(id.userId) { u ->
                    u.userState = changeFieldState.nextState
                }

                val messageWithoutCommand = if (changeFieldState.nextState.name == fieldValue) {
                    fieldValue.removeSuffix(changeFieldState.nextState.name).trim()
                } else fieldValue ?: "NOT_FOUND_MESSAGE"

                contractorService.changeContractor(id.userId) { c ->
                    changeMapper.invoke(c, messageWithoutCommand)
                }
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = changeFieldState.nextStateText,
                    replyMarkup = nextReplyMarkup.invoke()
                )
            }
        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }
}
