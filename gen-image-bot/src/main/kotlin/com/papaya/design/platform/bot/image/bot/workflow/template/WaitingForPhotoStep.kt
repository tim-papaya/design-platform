package com.papaya.design.platform.bot.image.bot.workflow.template

import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.message.MessageService
import com.papaya.design.platform.bot.image.bot.workflow.GenImageUserState
import com.papaya.design.platform.bot.tg.core.command.workflow.Step
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

open class WaitingForPhotoStep(
    private val messageService: MessageService,
    override val current: UserState,
    override val next: UserState,
    override val previous: UserState,
) : Step<UserState, GenImageUserState> {
    override fun perform(state: GenImageUserState) {
        val photos = state.photos
        val id = state.id

        if (photos != null) {
            messageService.sendStateMessage(id, next) { u ->
                u.photos = photos.map { it.toEntity() }
            }
            log.info("User $id: Added photo for generation")
        } else {
            messageService.sendMessage(id, current.messageText)
        }
    }
}
