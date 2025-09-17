package com.papaya.design.platform.bot.contractor.message

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.papaya.design.platform.bot.contractor.General
import com.papaya.design.platform.bot.contractor.command.ChangeFieldState
import com.papaya.design.platform.bot.contractor.contractor.ContractorDraftService
import com.papaya.design.platform.bot.contractor.contractor.ContractorService
import com.papaya.design.platform.bot.contractor.createMainMenuKeyboard
import com.papaya.design.platform.bot.contractor.user.ContractorUserState
import com.papaya.design.platform.bot.contractor.user.UserService
import com.papaya.design.platform.bot.tg.core.command.message.TelegramId
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val bot: Bot,
    private val userService: UserService,
    private val contractorService: ContractorService,
    private val contractorDraftService: ContractorDraftService
) {
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

    fun sendNextStateMessage(
        id: TelegramId,
        changeFieldState: ChangeFieldState,
        nextReplyMarkup: () -> InlineKeyboardMarkup
    ) {
        userService.saveUser(id.userId) { u ->
            u.userState = changeFieldState.nextState
        }
        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = changeFieldState.nextStateText,
            replyMarkup = nextReplyMarkup.invoke()
        )
    }

    fun sendMessage(
        id: TelegramId,
        text: String,
        replyMarkup: () -> InlineKeyboardMarkup
    ) {
        bot.sendMessage(
            chatId = ChatId.fromId(id.chatId),
            text = text,
            replyMarkup = replyMarkup.invoke()
        )
    }

    private fun clearStateForUser(id: TelegramId) {
        userService.saveUser(id.userId) { u ->
            u.userState = ContractorUserState.READY_FOR_CMD
        }
        contractorDraftService.removeDraft(id.userId)
    }
}
