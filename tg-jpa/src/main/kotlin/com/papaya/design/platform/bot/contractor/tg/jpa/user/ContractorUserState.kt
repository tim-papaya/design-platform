package com.papaya.design.platform.bot.contractor.tg.jpa.user

enum class ContractorUserState {
    ADD_NAME,
    ADD_PHONE,
    ADD_LINK,
    ADD_COMMENT,
    CHOOSE_FIELD_TO_EDIT,
    ADD_CATEGORY,
    CHOOSE_CATEGORY,
    CHOOSE_CONTRACTOR,
    PREPARE_FOR_FINISH_ADDING_CONTRACTOR,
    FINISH_ADDING_CONTRACTOR,
    READY_FOR_CMD,
}