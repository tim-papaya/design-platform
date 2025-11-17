package com.papaya.design.platform.bot.image.bot.message

import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.UserState.*
import com.papaya.design.platform.bot.image.bot.static.ExtendedRealisticInterior
import com.papaya.design.platform.bot.image.bot.static.PlannedRealisticInterior
import com.papaya.design.platform.bot.image.bot.static.RealisticInterior
import com.papaya.design.platform.bot.image.bot.static.RealisticInteriorBatch
import com.papaya.design.platform.bot.image.bot.static.RoomUpgrade

open class CommandState

class ImageGenerationStrategy(
    val newState: UserState,
    val textToShow: String,
    val stateToReturn: UserState,
    val exampleImages: List<LocalFile> = listOf(),
    val replyMarkup: ReplyMarkup = onlyBackKeyboard()
) : CommandState() {
    companion object {
        val START_REALISTIC_INTERIOR_GENERATION = ImageGenerationStrategy(
            newState = REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
            textToShow = RealisticInterior.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalFile.REALISTIC_EXAMPLE),
        )
        val START_REALISTIC_INTERIOR_BATCH_GENERATION = ImageGenerationStrategy(
            newState = REALISTIC_INTERIOR_BATCH_WAITING_FOR_PHOTO,
            textToShow = RealisticInteriorBatch.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalFile.REALISTIC_EXAMPLE),
            replyMarkup = realisticInteriorBatchKeyboard()
        )
        val START_ROOM_UPGRADE_GENERATION = ImageGenerationStrategy(
            newState = ROOM_UPGRADE_WAITING_FOR_PHOTO,
            textToShow = RoomUpgrade.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalFile.AI_UPDATE),
        )
        val START_EXTENDED_REALISTIC_INTERIOR_GENERATION = ImageGenerationStrategy(
            newState = EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
            textToShow = ExtendedRealisticInterior.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalFile.ROOM_UPDATE),
        )
        val START_PLANED_REALISTIC_INTERIOR_GENERATION = ImageGenerationStrategy(
            newState = PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO,
            textToShow = PlannedRealisticInterior.Text.START_GENERATION,
            stateToReturn = READY_FOR_CMD,
            exampleImages = listOf(LocalFile.MOOD_BOARD_GENERATE),
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
        val PLANED_REALISTIC_INTERIOR = StartGenerationOfImage(
            systemPrompt = PlannedRealisticInterior.Prompt.SYSTEM_PROMPT
        )
    }
}
