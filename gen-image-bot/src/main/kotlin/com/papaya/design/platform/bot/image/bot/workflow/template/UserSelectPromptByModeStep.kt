package com.papaya.design.platform.bot.image.bot.workflow.template

import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.message.MessageService
import com.papaya.design.platform.bot.image.bot.workflow.GenImageUserState
import com.papaya.design.platform.bot.tg.core.command.input.CUSTOM_MODE
import com.papaya.design.platform.bot.tg.core.command.input.UserInputSelectingMode
import com.papaya.design.platform.bot.tg.core.command.workflow.Step
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

class UserSelectPromptByModeStep(
    private val messageService: MessageService,
    override val current: UserState,
    override val next: UserState,
    override val previous: UserState,
    private val modes: List<UserInputSelectingMode>,
    private val nextCustom: UserState? = null,
    private val actionOnFinish: (GenImageUserState) -> Unit = {}
) : Step<UserState, GenImageUserState> {
    override fun perform(state: GenImageUserState) {
        val mode = modes.find { it.textShowingToUser == state.messageText }

        if (mode == null) {
            messageService.sendMessage(state.id, current.messageText)
            return
        }

        if (nextCustom != null && mode.textShowingToUser == CUSTOM_MODE) {
            messageService.sendStateMessage(state.id, nextCustom)
            return
        }

        messageService.sendStateMessage(state.id, next) { u ->
            u.userPrompt = mode.data
        }
        log.info { "User ${state.id}: Selected user prompt ${mode.data} by ${mode.textShowingToUser}" }
        actionOnFinish.invoke(state)
    }
}
