package com.papaya.design.platform.bot.image.bot.workflow.template

import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.input.VideoUserInputSelectingMode
import com.papaya.design.platform.bot.image.bot.message.MessageService
import com.papaya.design.platform.bot.image.bot.workflow.GenImageUserState
import com.papaya.design.platform.bot.tg.core.command.input.CUSTOM_MODE
import com.papaya.design.platform.bot.tg.core.command.workflow.Step
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

class UserSelectPromptStep(
    private val messageService: MessageService,
    override val current: UserState,
    override val next: UserState,
    override val previous: UserState,
    private val actionOnFinish: (GenImageUserState) -> Unit = {}
) : Step<UserState, GenImageUserState> {
    override fun perform(state: GenImageUserState) {
        if (state.messageText == null) {
            messageService.sendMessage(state.id, current.messageText)
            return
        }

        messageService.sendStateMessage(state.id, next) { u ->
            u.userPrompt = state.messageText
        }

        log.info { "User ${state.id}: Selected user prompt ${state.messageText} }" }
        actionOnFinish.invoke(state)
    }
}
