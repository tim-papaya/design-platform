package com.papaya.design.platform.bot.image.bot.workflow

import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.bot.image.bot.message.TelegramId
import com.papaya.design.platform.bot.tg.core.command.workflow.StepUserState

class GenImageUserState(
    val id: TelegramId,
    val messageText: String?,
    val photos: List<Photo>?
) : StepUserState