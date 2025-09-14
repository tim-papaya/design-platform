package com.papaya.design.platform.bot.contractor

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton.CallbackData
import com.papaya.design.platform.bot.contractor.command.ContractorTelegramCommand
import com.papaya.design.platform.bot.contractor.tg.jpa.user.ContractorUserState
import com.papaya.design.platform.bot.tg.core.command.GeneralTelegramCommand

fun createMainMenuKeyboard() =
    InlineKeyboardMarkup.create(
        listOf(
            CallbackData(ContractorTelegramCommand.ADD_CONTRACTOR.btnText, ContractorUserState.ADD_NAME.name),
            CallbackData(
                ContractorTelegramCommand.EDIT_CONTRACTOR.btnText,
                ContractorUserState.CHOOSE_FIELD_TO_EDIT.name
            ),
            CallbackData(
                ContractorTelegramCommand.VIEW_CONTRACTORS.btnText,
                ContractorUserState.ADD_CATEGORY.name
            ),
        )
    )

fun createNextStepAndBackMenu(nextState: ContractorUserState, previousState: ContractorUserState) =
    InlineKeyboardMarkup.create(
        listOf(
            CallbackData(GeneralTelegramCommand.NEXT.btnText, nextState.name),
            CallbackData(GeneralTelegramCommand.BACK.btnText, previousState.name),
            CallbackData(GeneralTelegramCommand.MAIN_MENU.btnText, ContractorUserState.READY_FOR_CMD.name),
        )
    )

fun createListMarkup(categories: List<String>) =
    InlineKeyboardMarkup.create(
        categories.map { CallbackData(it, it) }
            .plus(
                listOf(
                    CallbackData(GeneralTelegramCommand.MAIN_MENU.btnText, ContractorUserState.READY_FOR_CMD.name)
                )
            )
    )
