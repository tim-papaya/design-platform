package com.papaya.design.platform.bot.contractor

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton.CallbackData
import com.papaya.design.platform.bot.contractor.command.ContractorFields
import com.papaya.design.platform.bot.contractor.command.ContractorTelegramCommand
import com.papaya.design.platform.bot.contractor.contractor.ContractorService
import com.papaya.design.platform.bot.contractor.user.ContractorUserState
import com.papaya.design.platform.bot.tg.core.command.GeneralTelegramCommand

fun createMainMenuKeyboard() =
    InlineKeyboardMarkup.create(
        listOf(
            listOf(
                CallbackData(
                    ContractorTelegramCommand.VIEW_CONTRACTORS.btnText,
                    ContractorUserState.CHOOSE_CATEGORY.name
                )
            ),
            listOf(CallbackData(ContractorTelegramCommand.ADD_CONTRACTOR.btnText, ContractorUserState.ADD_NAME.name)),
        )
    )

fun createNextStepAndBackMenu(
    previousState: ContractorUserState,
    nextState: ContractorUserState? = null
): InlineKeyboardMarkup {
    return if (nextState != null) {
        InlineKeyboardMarkup.create(
            listOf(
                listOf(CallbackData(GeneralTelegramCommand.NEXT.btnText, nextState.name)),
                listOf(CallbackData(GeneralTelegramCommand.BACK.btnText, previousState.name)),
                listOf(
                    CallbackData(
                        GeneralTelegramCommand.MAIN_MENU.btnText,
                        ContractorUserState.MAIN_MENU_READY_FOR_CMD.name
                    )
                ),
            )
        )
    } else InlineKeyboardMarkup.create(
        listOf(
            listOf(CallbackData(GeneralTelegramCommand.BACK.btnText, previousState.name)),
            listOf(
                CallbackData(
                    GeneralTelegramCommand.MAIN_MENU.btnText,
                    ContractorUserState.MAIN_MENU_READY_FOR_CMD.name
                )
            ),
        )
    )
}

fun createListMarkup(
    buttonTexts: List<String>,
    before: ContractorUserState? = null,
    beforeText: String? = null
) =
    InlineKeyboardMarkup.create(
        createEmptyMarkupIfTextIsNull(before, beforeText) +
                buttonTexts.map { listOf(CallbackData(it, it)) } +
                listOf(
                    listOf(
                        CallbackData(
                            GeneralTelegramCommand.MAIN_MENU.btnText,
                            ContractorUserState.MAIN_MENU_READY_FOR_CMD.name
                        )
                    )
                )
    )

fun createContractorEditMarkup(contractorService: ContractorService, category: String, contractorName: String): InlineKeyboardMarkup =
    createListMarkup(
        contractorService.getContractorNamesByCategory(category).filter { it != contractorName },
        before = ContractorUserState.EDIT,
        beforeText = General.Text.EDIT_BTN
    )

fun createFieldsToEditMarkup(): InlineKeyboardMarkup =
    createListMarkup(ContractorFields.entries.map { it.text })

private fun createEmptyMarkupIfTextIsNull(before: ContractorUserState?, beforeText: String?): List<List<CallbackData>> =
    before?.let { listOf(listOf(CallbackData(beforeText ?: before.text, before.name))) } ?: listOf(listOf())
