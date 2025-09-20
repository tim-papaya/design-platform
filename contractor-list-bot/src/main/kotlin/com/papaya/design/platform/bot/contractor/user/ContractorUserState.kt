package com.papaya.design.platform.bot.contractor.user

import com.papaya.design.platform.bot.contractor.General

object AddFieldUserState {
    val addFieldStates: List<ContractorUserState> = listOf(
        ContractorUserState.ADD_NAME,
        ContractorUserState.ADD_CATEGORY,
        ContractorUserState.ADD_PHONE,
        ContractorUserState.ADD_LINK,
        ContractorUserState.ADD_COMMENT,
        ContractorUserState.FINISH_ADDING_CONTRACTOR,
        ContractorUserState.CONFIRM_FINISH_ADDING_CONTRACTOR,
    )

    fun ContractorUserState.nextAddFieldState() =
        addFieldStates.indexOf(this).let {
            if (it == -1 || it + 1 >= addFieldStates.size) ContractorUserState.MAIN_MENU_READY_FOR_CMD
            else addFieldStates[it + 1]
        }

    fun ContractorUserState.previousAddFieldState() =
        addFieldStates.indexOf(this).let {
            if (it == -1 || it - 1 < 0) ContractorUserState.MAIN_MENU_READY_FOR_CMD
            else addFieldStates[it - 1]
        }
}


enum class ContractorUserState(val text: String, val isOptional: Boolean = true) {
    ADD_NAME(General.Text.ADD_NAME, false),
    ADD_CATEGORY(General.Text.ADD_CATEGORY, false),
    ADD_PHONE(General.Text.ADD_PHONE),
    ADD_LINK(General.Text.ADD_LINK),
    ADD_COMMENT(General.Text.ADD_COMMENT),
    FINISH_ADDING_CONTRACTOR(General.Text.FINISH_ADDING_CONTRACTOR, false),
    CONFIRM_FINISH_ADDING_CONTRACTOR(General.Text.FINISH_ADDING_CONTRACTOR, false),
    MAIN_MENU_READY_FOR_CMD(General.Text.MAIN_MENU_NEXT_STEP),

    CHOOSE_CATEGORY(General.Text.CHOOSE_CATEGORY),
    CHOOSE_CONTRACTOR(General.Text.CHOOSE_CONTRACTOR),

    CHOOSE_FIELD_TO_EDIT(General.Text.CHOOSE_FIELD_TO_EDIT),
}

fun String.containsContractorUserState() =
    ContractorUserState.entries.map { it.name }.contains(this)
