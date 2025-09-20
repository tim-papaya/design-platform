package com.papaya.design.platform.bot.contractor

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton.CallbackData
import com.papaya.design.platform.bot.contractor.command.ContractorTelegramCommand
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
            listOf(
                CallbackData(
                    ContractorTelegramCommand.EDIT_CONTRACTOR.btnText,
                    ContractorUserState.CHOOSE_FIELD_TO_EDIT.name
                )
            ),
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
                listOf(CallbackData(GeneralTelegramCommand.MAIN_MENU.btnText, ContractorUserState.MAIN_MENU_READY_FOR_CMD.name)),
            )
        )
    } else InlineKeyboardMarkup.create(
        listOf(
            listOf(CallbackData(GeneralTelegramCommand.BACK.btnText, previousState.name)),
            listOf(CallbackData(GeneralTelegramCommand.MAIN_MENU.btnText, ContractorUserState.MAIN_MENU_READY_FOR_CMD.name)),
        )
    )
}

fun createListMarkup(categories: List<String>) =
    InlineKeyboardMarkup.create(
        categories.map { listOf(CallbackData(it, it)) } +
                listOf(
                    listOf(
                        CallbackData(
                            GeneralTelegramCommand.MAIN_MENU.btnText,
                            ContractorUserState.MAIN_MENU_READY_FOR_CMD.name
                        )
                    )
                )
    )