package com.papaya.design.platform.bot.contractor.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.papaya.design.platform.bot.contractor.General
import com.papaya.design.platform.bot.contractor.contractor.ContractorDraftService
import com.papaya.design.platform.bot.contractor.contractor.ContractorService
import com.papaya.design.platform.bot.contractor.createMainMenuKeyboard
import com.papaya.design.platform.bot.contractor.user.ContractorUserState
import com.papaya.design.platform.bot.contractor.user.UserService
import com.papaya.design.platform.bot.tg.core.command.message.TelegramId
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
@Lazy
class MessageService(
    private val userService: UserService,
    private val contractorService: ContractorService,
    private val contractorDraftService: ContractorDraftService
) {
    lateinit var bot: Bot

    fun sendMainMenuMessage(id: TelegramId) {
        sendMainMenuMessage(id, General.Text.MAIN_MENU_NEXT_STEP)
    }

    fun sendMainMenuMessage(id: TelegramId, text: String) {
        clearStateForUser(id)
        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = text,
            replyMarkup = createMainMenuKeyboard()
        )
    }

    fun sendStateMessage(
        id: TelegramId,
        newState: ContractorUserState,
        newReplyMarkup: () -> InlineKeyboardMarkup,
        text: String? = null
    ) {
        userService.saveUser(id.userId) { u ->
            u.userState = newState
        }
        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = text ?: newState.text,
            replyMarkup = newReplyMarkup.invoke()
        )
    }

    fun sendMessage(
        id: TelegramId,
        text: String,
        replyMarkup: () -> InlineKeyboardMarkup? = { null }
    ) {
        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = text,
            replyMarkup = replyMarkup.invoke()
        )
    }

    private fun clearStateForUser(id: TelegramId) {
        userService.saveUser(id.userId) { u ->
            u.userState = ContractorUserState.MAIN_MENU_READY_FOR_CMD
            u.contractorName = null
            u.category = null
        }
        contractorDraftService.removeDraft(id.userId)
    }
}
