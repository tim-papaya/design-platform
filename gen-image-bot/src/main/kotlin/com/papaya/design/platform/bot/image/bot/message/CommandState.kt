package com.papaya.design.platform.bot.image.bot.message

import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.UserState.*
import com.papaya.design.platform.bot.image.bot.static.ExtendedRealisticInterior
import com.papaya.design.platform.bot.image.bot.static.PlannedRealisticInterior
import com.papaya.design.platform.bot.image.bot.static.RealisticInterior
import com.papaya.design.platform.bot.image.bot.static.RoomUpgrade
import java.nio.file.Path
import kotlin.io.path.readBytes

open class CommandState

class StartWaitingForImageCommandState(
    val cmd: TelegramCommand,
    val newState: UserState,
    val textToShow: String,
    val stateToReturn: UserState,
    val exampleImages: List<LocalImage> = listOf()
) : CommandState() {
    companion object {
        val START_REALISTIC_INTERIOR_GENERATION = StartWaitingForImageCommandState(
            cmd = TelegramCommand.REAL_IMAGE_CMD,
            newState = REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
            textToShow = RealisticInterior.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalImage.REALISTIC_EXAMPLE_1),
        )
        val START_ROOM_UPGRADE_GENERATION = StartWaitingForImageCommandState(
            cmd = TelegramCommand.ROOM_UPGRADE_CMD,
            newState = ROOM_UPGRADE_WAITING_FOR_PHOTO,
            textToShow = RoomUpgrade.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalImage.REALISTIC_EXAMPLE_1),
        )
        val START_EXTENDED_REALISTIC_INTERIOR_GENERATION = StartWaitingForImageCommandState(
            cmd = TelegramCommand.EXT_REAL_IMAGE_CMD,
            newState = EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
            textToShow = ExtendedRealisticInterior.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalImage.REALISTIC_EXAMPLE_1),
        )
        val START_PLANED_REALISTIC_INTERIOR_GENERATION = StartWaitingForImageCommandState(
            cmd = TelegramCommand.PLANED_REAL_IMAGE_CMD,
            newState = PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
            textToShow = PlannedRealisticInterior.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalImage.REALISTIC_EXAMPLE_1),
        )
    }
}

class StartGenerationOfImage(
    val systemPrompt: String
) : CommandState() {
    companion object {
        val ROOM_UPGRADE = StartGenerationOfImage(
            systemPrompt = RoomUpgrade.Prompt.SYSTEM_PROMPT
        )
        val REALISTIC_INTERIOR = StartGenerationOfImage(
            systemPrompt = RealisticInterior.Prompt.SYSTEM_PROMPT
        )
        val EXTENDED_REALISTIC_INTERIOR = StartGenerationOfImage(
            systemPrompt = ExtendedRealisticInterior.Prompt.SYSTEM_PROMPT
        )
    }
}
