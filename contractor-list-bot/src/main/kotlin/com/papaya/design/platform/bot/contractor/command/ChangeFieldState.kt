package com.papaya.design.platform.bot.contractor.command

import com.papaya.design.platform.bot.contractor.General
import com.papaya.design.platform.bot.contractor.user.ContractorUserState

enum class ChangeFieldState(
    val currentState: ContractorUserState,
    val previousState: ContractorUserState,
    val previousPreviousState: ContractorUserState,
    val nextState: ContractorUserState,
    val nextNextState: ContractorUserState,
    val nextStateText: String,
    val isOptional: Boolean,
    val previousText: String,
) {
    ADD_CATEGORY(
        ContractorUserState.ADD_CATEGORY,
        ContractorUserState.ADD_NAME,
        ContractorUserState.READY_FOR_CMD,
        ContractorUserState.ADD_PHONE,
        ContractorUserState.ADD_LINK,
        General.Text.ADD_PHONE,
        false,
        General.Text.NEXT_STEP,
    ),
    ADD_PHONE(
        ContractorUserState.ADD_PHONE,
        ContractorUserState.ADD_CATEGORY,
        ContractorUserState.ADD_NAME,
        ContractorUserState.ADD_LINK,
        ContractorUserState.ADD_COMMENT,
        General.Text.ADD_LINK,
        true,
        General.Text.ADD_CATEGORY,
    ),
    ADD_LINK(
        ContractorUserState.ADD_LINK,
        ContractorUserState.ADD_PHONE,
        ContractorUserState.ADD_CATEGORY,
        ContractorUserState.ADD_COMMENT,
        ContractorUserState.PREPARE_FOR_FINISH_ADDING_CONTRACTOR,
        General.Text.ADD_COMMENT,
        true,
        General.Text.ADD_PHONE,
    ),
    ADD_COMMENT(
        ContractorUserState.ADD_COMMENT,
        ContractorUserState.ADD_LINK,
        ContractorUserState.ADD_PHONE,
        ContractorUserState.PREPARE_FOR_FINISH_ADDING_CONTRACTOR,
        ContractorUserState.FINISH_ADDING_CONTRACTOR,
        General.Text.FINISH_ADDING_CONTRACTOR,
        true,
        General.Text.ADD_LINK,
    ),
}