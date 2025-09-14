package com.papaya.design.platform.bot.contractor.command

import com.papaya.design.platform.bot.tg.core.command.TelegramCommand

enum class ContractorTelegramCommand(
    override val cmdText: String,
    override val btnText: String
) : TelegramCommand {
    ADD_CONTRACTOR("add_contractor", "Добавить подрядчиков"),
    VIEW_CONTRACTORS("view_contractors", "Посмотреть подрядчиков"),
    EDIT_CONTRACTOR("edit_contractor", "Редактировать подрядчика"),
    REQUEST_ACCESS("request_access", "Запросить доступ")
}
