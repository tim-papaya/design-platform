package com.papaya.design.platform.bot.contractor

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.papaya.design.platform.bot.contractor.command.ChangeFieldState
import com.papaya.design.platform.bot.contractor.contractor.ContractorService
import com.papaya.design.platform.bot.contractor.tg.jpa.user.ContractorUserState
import com.papaya.design.platform.bot.contractor.tg.jpa.user.UserService
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
            message {
                val messageText = message.text
                val id = message.telegramId()
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
                                        ContractorUserState.ADD_PHONE,
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
                            errorReplyMarkup = {
                                createListMarkup(contractorService.getCategories())
                            })
                    }

                    ContractorUserState.ADD_PHONE -> {
                        checkField(messageText, id, ChangeFieldState.ADD_PHONE)
                    }

                    ContractorUserState.ADD_LINK -> {
                        checkField(messageText, id, ChangeFieldState.ADD_LINK)
                        checkThatPhoneOrLinkIsFilled(id)
                    }

                    ContractorUserState.ADD_COMMENT -> {
                        checkField(messageText, id, ChangeFieldState.ADD_COMMENT)
                    }

                    ContractorUserState.PREPARE_FOR_FINISH_ADDING_CONTRACTOR -> {
                        val contractor = contractorService.getContractor(id.userId)

                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = "Новый подрядчик - $contractor",
                            replyMarkup = createNextStepAndBackMenu(
                                ContractorUserState.FINISH_ADDING_CONTRACTOR,
                                ContractorUserState.ADD_COMMENT
                            )
                        )
                    }

                    ContractorUserState.FINISH_ADDING_CONTRACTOR -> {
                        if (!contractorService.saveDraftIfExists(id.userId)) {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = General.Error.ERROR_ON_SAVING_CONTRACTOR,
                                replyMarkup = createMainMenuKeyboard()
                            )
                        }
                    }

                    ContractorUserState.CHOOSE_CATEGORY -> {
                        if (contractorService.getCategories().contains(messageText)) {
                            userService.saveUser(id.userId) { u ->
                                u.userState = ContractorUserState.CHOOSE_CONTRACTOR
                                u.category = messageText
                            }
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = General.Error.ERROR_ON_CHOOSING_CATEGORY,
                                replyMarkup = createListMarkup(contractorService.getCategories())
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
                                replyMarkup = createListMarkup(contractorService.getContractorNamesByCategory(category))
                            )
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = General.Error.ERROR_ON_CHOOSING_CATEGORY,
                                replyMarkup = createListMarkup(contractorService.getContractorNamesByCategory(category))
                            )
                        }
                    }

                    ContractorUserState.CHOOSE_FIELD_TO_EDIT -> TODO()
                }
            }
        }
    }

    private fun MessageHandlerEnvironment.checkThatPhoneOrLinkIsFilled(id: TelegramId) {
        val draft = contractorService.getContractor(id.userId)
        if (draft != null && draft.link == null && draft.phone == null) {
            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = General.Error.ERROR_EMPTY_MAIN_FIELDS,
                replyMarkup = createNextStepAndBackMenu(
                    ContractorUserState.ADD_PHONE,
                    ContractorUserState.ADD_NAME
                )
            )
        }
    }

    private fun MessageHandlerEnvironment.checkNameField(
        messageText: String?,
        id: TelegramId
    ) {
        when (val name = messageText?.trim()) {
            null -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Error.ERROR_EMPTY_FIELD,
                    replyMarkup = createNextStepAndBackMenu(
                        ContractorUserState.ADD_PHONE,
                        ContractorUserState.READY_FOR_CMD
                    )
                )
            }

            else -> {
                userService.saveUser(id.userId) { u ->
                    u.userState = ContractorUserState.ADD_PHONE
                }

                contractorService.addContractor(name, id.userId)
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Text.ADD_PHONE,
                    replyMarkup = createNextStepAndBackMenu(
                        ContractorUserState.ADD_LINK,
                        ContractorUserState.ADD_PHONE
                    )
                )
            }
        }
    }

    private fun MessageHandlerEnvironment.checkField(
        messageText: String?,
        id: TelegramId,
        changeFieldState: ChangeFieldState,
        errorReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.currentState,
                changeFieldState.previousState
            )
        },
        nextReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.nextNextState,
                changeFieldState.currentState
            )
        }
    ) {
        val fieldValue = messageText?.trim()
        when {
            !changeFieldState.isOptional && fieldValue == null -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(id.chatId),
                    text = General.Error.ERROR_EMPTY_FIELD,
                    replyMarkup = errorReplyMarkup.invoke()
                )
            }

            else -> {
                userService.saveUser(id.userId) { u ->
                    u.userState = changeFieldState.nextState
                }

                contractorService.changeContractor(id.userId) {
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
