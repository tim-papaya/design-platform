package com.papaya.design.platform.bot.contractor

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.papaya.design.platform.bot.contractor.General.Text.toText
import com.papaya.design.platform.bot.contractor.command.ContractorFields
import com.papaya.design.platform.bot.contractor.contractor.ContractorDraftService
import com.papaya.design.platform.bot.contractor.contractor.ContractorEntity
import com.papaya.design.platform.bot.contractor.contractor.ContractorService
import com.papaya.design.platform.bot.contractor.contractor.toModel
import com.papaya.design.platform.bot.contractor.message.MessageService
import com.papaya.design.platform.bot.contractor.user.AddFieldUserState.nextAddFieldState
import com.papaya.design.platform.bot.contractor.user.AddFieldUserState.previousAddFieldState
import com.papaya.design.platform.bot.contractor.user.ContractorUserState
import com.papaya.design.platform.bot.contractor.user.User
import com.papaya.design.platform.bot.contractor.user.UserService
import com.papaya.design.platform.bot.contractor.user.containsContractorUserState
import com.papaya.design.platform.bot.tg.core.command.GeneralTelegramCommand
import com.papaya.design.platform.bot.tg.core.command.message.TelegramId
import com.papaya.design.platform.bot.tg.core.command.message.telegramId
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.hibernate.Length
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
@Profile("prod")
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    @Value("#{'\${support.admin.ids}'.split(',')}")
    private val rawSupportIds: List<String>,
    private val userService: UserService,
    private val contractorService: ContractorService,
    private val contractorDraftService: ContractorDraftService,
    private val messageService: MessageService,
) : BotService {

    private val supportIds = rawSupportIds.map { it.toLong() }.map { TelegramId(it, it) }

    private val bot = bot {
        token = apiKey
        dispatch {
            command(GeneralTelegramCommand.GET_ACCESS.cmdText) {
                val id = message.telegramId()
                val newUserName = messageWithoutCommand(message.text)

                when {
                    newUserName == null ->
                        messageService.sendMessage(id, General.Error.ERROR_MISING_NAME_IN_ACCESS_REQUEST)

                    else -> {
                        supportIds.forEach {
                            messageService.sendMessage(
                                it,
                                "${General.Text.USER_REQUEST_ACCESS}@${message.from?.username} - $newUserName"
                            )
                            messageService.sendMessage(it, "/${GeneralTelegramCommand.ADD_ACCESS.cmdText} ${id.userId}")
                        }
                        messageService.sendMessage(id, General.Text.ACCESS_AWAITING)
                    }
                }
            }
            command(GeneralTelegramCommand.ADD_ACCESS.cmdText) {
                val id = message.telegramId()
                if (!supportIds.contains(id)) return@command

                val newUserId = messageWithoutCommand(message.text)?.toLongOrNull()
                when {
                    newUserId == null ->
                        messageService.sendMessage(id, General.Error.ERROR_ON_GIVING_ACCESS)

                    else -> {
                        userService.saveUser(newUserId)
                        messageService.sendMessage(id, General.Text.ACCESS_GIVEN, { createMainMenuKeyboard() })
                        messageService.sendMessage(
                            TelegramId(newUserId, newUserId),
                            General.Text.ACCESS_GIVEN,
                            { createMainMenuKeyboard() })
                    }
                }
            }

            callbackQuery {
                val userId = callbackQuery.from.id
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                val id = TelegramId(userId, chatId)

                try {
                    val messageText = callbackQuery.data ?: return@callbackQuery
                    val user = getUserAndUpdateUsernameIfRequired(id, callbackQuery.from.username)
                    when {
                        messageText.contains(GeneralTelegramCommand.ADD_ACCESS.cmdText)
                                || messageText.contains(GeneralTelegramCommand.GET_ACCESS.cmdText) ->
                            return@callbackQuery

                        user == null ->
                            messageService.sendMessage(id, General.Error.NOT_AUTHORIZED)

                        else ->
                            checkInput(user, messageText, id)
                    }
                } catch (e: Exception) {
                    log.error(e) { "Error at callback thread" }
                    messageService.sendMainMenuMessage(id, General.Error.ERROR_GENERAL)
                }

            }
            message {
                val id = message.telegramId()
                try {
                    val messageText = message.text ?: return@message
                    val user = getUserAndUpdateUsernameIfRequired(id, message.from?.username)

                    when {
                        messageText.contains(GeneralTelegramCommand.ADD_ACCESS.cmdText)
                                || messageText.contains(GeneralTelegramCommand.GET_ACCESS.cmdText) ->
                            return@message

                        user == null ->
                            messageService.sendMessage(id, General.Error.NOT_AUTHORIZED)

                        messageText == GeneralTelegramCommand.MAIN_MENU.btnText ->
                            messageService.sendMainMenuMessage(id)

                        else -> checkInput(user, messageText, id)
                    }
                } catch (e: Exception) {
                    log.error(e) { "Error at message thread" }
                    messageService.sendMainMenuMessage(id, General.Error.ERROR_GENERAL)
                }
            }
        }
    }.also { messageService.bot = it }


    private fun getUserAndUpdateUsernameIfRequired(id: TelegramId, currentUsername: String?): User? =
        userService.getUserOrNull(id.userId)?.also {
            if (!currentUsername.isNullOrBlank() && it.name != currentUsername)
                userService.saveUser(id.userId) { u -> u.name = currentUsername }
        }

    private fun checkInput(
        user: User,
        messageText: String,
        id: TelegramId
    ) {
        when (user.userState) {
            ContractorUserState.MAIN_MENU_READY_FOR_CMD -> {
                when (messageText) {
                    ContractorUserState.ADD_NAME.name ->
                        messageService.sendStateMessage(id, ContractorUserState.ADD_NAME, {
                            createNextStepAndBackMenu(
                                ContractorUserState.MAIN_MENU_READY_FOR_CMD
                            )
                        })

                    ContractorUserState.CHOOSE_CATEGORY.name ->
                        messageService.sendStateMessage(id, ContractorUserState.CHOOSE_CATEGORY, {
                            createListMarkup(contractorService.getCategories())
                        })

                    else -> messageService.sendMainMenuMessage(id)
                }
            }

            ContractorUserState.ADD_NAME -> {
                val name = messageText.trim()
                if (contractorService.getContractor(name) != null || contractorDraftService.getContractorDraft(id.userId) != null) {
                    messageService.sendMainMenuMessage(id, General.Error.ERROR_NAME_NOT_UNIQUE)
                    return
                }

                checkField(
                    messageText, id, ContractorUserState.ADD_NAME,
                    isLengthLimited = true,
                    { _: ContractorEntity, _: String -> },
                    nextReplyMarkup = { createListMarkup(contractorService.getCategories()) },
                    invokeBeforeChangeMapper = { s: String ->
                        contractorDraftService.createContractorDraft(
                            s,
                            id.userId
                        )
                    }
                )
            }

            ContractorUserState.ADD_CATEGORY ->
                checkField(
                    messageText, id, ContractorUserState.ADD_CATEGORY,
                    isLengthLimited = true,
                    { c: ContractorEntity, s: String -> c.category = s },
                ) {
                    createListMarkup(contractorService.getCategories())
                }

            ContractorUserState.ADD_PHONE ->
                checkField(
                    messageText, id, ContractorUserState.ADD_PHONE,
                    isLengthLimited = true,
                    { c: ContractorEntity, s: String -> c.phone = s })


            ContractorUserState.ADD_LINK -> {
                checkField(
                    messageText, id, ContractorUserState.ADD_LINK,
                    isLengthLimited = false,
                    { c: ContractorEntity, s: String -> c.link = s })
                checkThatPhoneOrLinkIsFilled(id)
            }

            ContractorUserState.ADD_COMMENT -> {
                checkField(
                    messageText, id, ContractorUserState.ADD_COMMENT,
                    isLengthLimited = false,
                    { c: ContractorEntity, s: String -> c.comment = s },
                    nextReplyMarkup = { InlineKeyboardMarkup.create() },
                    sendAdditionalMessageOnNext = {
                        val contractorDraft = contractorDraftService.getContractorDraft(id.userId)
                        messageService.sendMessage(
                            id,
                            contractorDraft?.toModel()?.toText(user) ?: "Contractor draft unknown",
                            {
                                createNextStepAndBackMenu(
                                    ContractorUserState.ADD_COMMENT,
                                    ContractorUserState.CONFIRM_FINISH_ADDING_CONTRACTOR
                                )
                            }
                        )
                    })
            }

            ContractorUserState.FINISH_ADDING_CONTRACTOR -> {
                when (messageText) {
                    ContractorUserState.MAIN_MENU_READY_FOR_CMD.name -> messageService.sendMainMenuMessage(id)

                    ContractorUserState.ADD_COMMENT.name ->
                        messageService.sendStateMessage(id, ContractorUserState.ADD_COMMENT, {
                            createNextStepAndBackMenu(
                                ContractorUserState.ADD_LINK,
                                ContractorUserState.FINISH_ADDING_CONTRACTOR
                            )
                        })

                    ContractorUserState.CONFIRM_FINISH_ADDING_CONTRACTOR.name -> {
                        if (contractorDraftService.saveDraftIfExists(id.userId)) {
                            messageService.sendMainMenuMessage(id, General.Text.CONFIRM_FINISH_ADDING_CONTRACTOR)
                        } else {
                            messageService.sendMainMenuMessage(id, General.Error.ERROR_ON_SAVING_CONTRACTOR)
                        }
                    }
                }
            }

            ContractorUserState.CONFIRM_FINISH_ADDING_CONTRACTOR -> {
                log.error { "Should not go to this section" }
                messageService.sendMainMenuMessage(id)
            }

            ContractorUserState.CHOOSE_CATEGORY -> {
                log.info { "Choosing category $messageText" }
                val inputCategory = messageText.trim()
                when {
                    inputCategory == ContractorUserState.MAIN_MENU_READY_FOR_CMD.name ->
                        messageService.sendMainMenuMessage(id)

                    contractorService.getCategories().contains(messageText) -> {

                        userService.saveUser(id.userId) { u ->
                            u.userState = ContractorUserState.CHOOSE_CONTRACTOR
                            u.category = inputCategory
                        }
                        messageService.sendStateMessage(id, ContractorUserState.CHOOSE_CONTRACTOR, {
                            createListMarkup(contractorService.getContractorNamesByCategory(messageText))
                        })
                    }

                    else -> {
                        messageService.sendMainMenuMessage(id, General.Error.ERROR_ON_CHOOSING_CATEGORY)
                    }
                }
            }

            ContractorUserState.CHOOSE_CONTRACTOR -> {
                val category = user.category!!
                val contractorName =
                    contractorService.getContractorNamesByCategory(category).find { it == messageText }
                when {
                    messageText == ContractorUserState.MAIN_MENU_READY_FOR_CMD.name ->
                        messageService.sendMainMenuMessage(id)

                    messageText == ContractorUserState.EDIT.name -> {
                        when {
                            contractorName != null && contractorService.getContractor(contractorName)?.addedByUserId != user.userId ->
                                messageService.sendMainMenuMessage(id, General.Error.ERROR_NOT_YOUR_CONTRACTOR)

                            else -> {
                                messageService.sendStateMessage(
                                    id, ContractorUserState.EDIT,
                                    { createFieldsToEditMarkup() },
                                )
                            }
                        }
                    }

                    contractorName != null -> {
                        val contractor = contractorService.getContractor(contractorName)!!
                        userService.saveUser(id.userId) { u ->
                            u.contractorName = contractorName
                        }
                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text =
                                """|Подрядчик: ${contractor.name}
                                           |Категория: ${contractor.category}
                                           |Телефон: ${contractor.phone}
                                           |Ссылка: ${contractor.link}
                                           |Кто добавил(а): ${userService.getUserOrNull(contractor.addedByUserId)?.name ?: contractor.addedByUserId} 
                                           |Комментарий: ${contractor.comment}""".trimMargin(),
                            replyMarkup = createContractorEditMarkup(contractorService, category, contractor.name)
                        )
                    }

                    else -> messageService.sendMainMenuMessage(id, General.Error.ERROR_ON_CHOOSING_CONTRACTOR)

                }
            }

            ContractorUserState.EDIT -> {
                val contractorName = user.contractorName!!
                val fieldToEdit = messageText.trim()
                when {
                    fieldToEdit == ContractorUserState.MAIN_MENU_READY_FOR_CMD.name ->
                        messageService.sendMainMenuMessage(id)

                    contractorService.getContractor(contractorName)?.addedByUserId != user.userId ->
                        messageService.sendMainMenuMessage(id, General.Error.ERROR_NOT_YOUR_CONTRACTOR)

                    else -> {
                        val fieldToEditSelected = ContractorFields.entries.find { it.text == fieldToEdit }
                        val contractor = contractorService.getContractor(contractorName)!!
                        when (fieldToEditSelected) {
                            ContractorFields.NAME ->
                                messageService.sendStateMessage(
                                    id, ContractorUserState.EDIT_NAME,
                                    { createNextStepAndBackMenu(ContractorUserState.EDIT) },
                                    "${ContractorUserState.EDIT_NAME.text}\nТекущее: ${contractor.name}"
                                )

                            ContractorFields.PHONE ->
                                messageService.sendStateMessage(
                                    id, ContractorUserState.EDIT_PHONE,
                                    { createNextStepAndBackMenu(ContractorUserState.EDIT) },
                                    "${ContractorUserState.EDIT_PHONE.text}\nТекущее: ${contractor.phone}"
                                )

                            ContractorFields.LINK ->
                                messageService.sendStateMessage(
                                    id, ContractorUserState.EDIT_LINK,
                                    { createNextStepAndBackMenu(ContractorUserState.EDIT) },
                                    "${ContractorUserState.EDIT_LINK.text}\nТекущее: ${contractor.link}"
                                )

                            ContractorFields.CATEGORY ->
                                messageService.sendStateMessage(
                                    id, ContractorUserState.EDIT_CATEGORY,
                                    {
                                        createListMarkup(
                                            contractorService.getCategories(),
                                            before = ContractorUserState.EDIT,
                                            beforeText = General.Text.EDIT_BTN
                                        )
                                    },
                                    "${ContractorUserState.EDIT_CATEGORY.text}\nТекущее: ${contractor.category}"
                                )

                            ContractorFields.COMMENT ->
                                messageService.sendStateMessage(
                                    id, ContractorUserState.EDIT_COMMENT,
                                    { createNextStepAndBackMenu(ContractorUserState.EDIT) },
                                    "${ContractorUserState.EDIT_COMMENT.text}\nТекущее: ${contractor.comment}"
                                )

                            else -> {
                                messageService.sendMainMenuMessage(id, General.Error.ERROR_GENERAL)
                            }
                        }
                    }
                }
            }

            ContractorUserState.EDIT_NAME ->
                checkFieldEdit(messageText, id, user, ContractorUserState.EDIT_NAME, isLengthLimited = true) { c, s ->
                    c.name = s
                }

            ContractorUserState.EDIT_CATEGORY ->
                checkFieldEdit(messageText, id, user, ContractorUserState.EDIT_CATEGORY, isLengthLimited = true) { c, s ->
                    c.category = s
                }

            ContractorUserState.EDIT_PHONE ->
                checkFieldEdit(messageText, id, user, ContractorUserState.EDIT_PHONE, isLengthLimited = true) { c, s ->
                    c.phone = s
                }

            ContractorUserState.EDIT_LINK ->
                checkFieldEdit(messageText, id, user, ContractorUserState.EDIT_LINK) { c, s ->
                    c.link = s
                }

            ContractorUserState.EDIT_COMMENT ->
                checkFieldEdit(messageText, id, user, ContractorUserState.EDIT_COMMENT) { c, s ->
                    c.comment = s
                }
        }
    }

    private fun checkThatPhoneOrLinkIsFilled(id: TelegramId) {
        val draft = contractorDraftService.getContractorDraft(id.userId)
        if (draft != null && draft.link == null && draft.phone == null) {
            messageService.sendStateMessage(id, ContractorUserState.ADD_NAME, {
                createNextStepAndBackMenu(
                    ContractorUserState.ADD_NAME
                )
            }, General.Error.ERROR_EMPTY_MAIN_FIELDS)
        }
    }

    private fun checkFieldEdit(
        messageText: String,
        id: TelegramId,
        user: User,
        currentState: ContractorUserState,
        isLengthLimited: Boolean = false,
        changeMapper: (ContractorEntity, String) -> Unit,
    ) {
        val fieldValue = messageText.trim()
        val previousState: ContractorUserState = ContractorUserState.EDIT

        val category = user.category!!
        val contractorReplyMarkup = { createContractorEditMarkup(contractorService, category, user.contractorName!!) }
        val editFieldReplyMarkup = { createFieldsToEditMarkup() }
        val errorReplyMarkup = createNextStepAndBackMenu(previousState)

        when {
            fieldValue == ContractorUserState.MAIN_MENU_READY_FOR_CMD.name ->
                messageService.sendMainMenuMessage(id)

            fieldValue == previousState.name ->
                messageService.sendStateMessage(id, previousState, editFieldReplyMarkup)

            !currentState.isOptional && (fieldValue.containsContractorUserState()) ->
                messageService.sendMessage(
                    id, General.Error.ERROR_EMPTY_FIELD, { errorReplyMarkup }
                )

            isLengthLimited && fieldValue.length >= 32 ->
                messageService.sendMessage(
                    id, General.Error.ERROR_FIELD_SIZE_TOO_LARGE, { errorReplyMarkup }
                )

            else -> {
                contractorService.changeContractor(user.contractorName!!) { c ->
                    changeMapper.invoke(c, fieldValue)
                }
                messageService.sendStateMessage(
                    id, ContractorUserState.CHOOSE_CONTRACTOR, contractorReplyMarkup, General.Text.EDIT_SUCCESSFUL
                )
            }
        }
    }

    private fun checkField(
        messageText: String?,
        id: TelegramId,
        changeFieldState: ContractorUserState,
        isLengthLimited : Boolean = false,
        changeMapper: (ContractorEntity, String) -> Unit,
        errorReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.previousAddFieldState()
            )
        },
        nextReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState
            )
        },
        previousReplyMarkup: () -> InlineKeyboardMarkup = {
            createNextStepAndBackMenu(
                changeFieldState.previousAddFieldState().previousAddFieldState()
            )
        },
        sendAdditionalMessageOnNext: () -> Unit = {},
        invokeBeforeChangeMapper: (String) -> Unit = {},
    ) {
        val fieldValue = messageText?.trim()
        val previousState = changeFieldState.previousAddFieldState()
        when {
            fieldValue == ContractorUserState.MAIN_MENU_READY_FOR_CMD.name ->
                messageService.sendMainMenuMessage(id)

            fieldValue == previousState.name ->
                messageService.sendStateMessage(id, previousState, previousReplyMarkup)

            !changeFieldState.isOptional && (fieldValue == null || fieldValue.containsContractorUserState()) ->
                messageService.sendMessage(id, General.Error.ERROR_EMPTY_FIELD, errorReplyMarkup)

            isLengthLimited && fieldValue != null && fieldValue.length >= 32 ->
                messageService.sendMessage(id, General.Error.ERROR_FIELD_SIZE_TOO_LARGE, errorReplyMarkup)

            else -> {
                val fieldValueWithDefault = fieldValue ?: General.FieldDefault.NO_FIELD_VALUE
                log.info { "Will add for ${id.userId}, value $fieldValueWithDefault" }

                invokeBeforeChangeMapper.invoke(fieldValueWithDefault)
                contractorDraftService.changeContractorDraft(id.userId) { c ->
                    changeMapper.invoke(c, fieldValueWithDefault)
                }

                messageService.sendStateMessage(id, changeFieldState.nextAddFieldState(), nextReplyMarkup)
                sendAdditionalMessageOnNext.invoke()
            }
        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }
}

private fun messageWithoutCommand(text: String?): String? =
    text?.trim()?.split(Regex("\\s+"), limit = 2)?.drop(1)?.firstOrNull()
