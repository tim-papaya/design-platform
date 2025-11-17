package com.papaya.design.platform.bot.image.bot.workflow.realistic

import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.UserState.READY_FOR_CMD
import com.papaya.design.platform.bot.image.bot.domain.UserState.REALISTIC_INTERIOR_BATCH_WAITING_FOR_PHOTO
import com.papaya.design.platform.bot.image.bot.domain.UserState.WAITING_FOR_END_OF_PHOTO_GENERATION
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.message.ImageMessageService
import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton
import com.papaya.design.platform.bot.image.bot.message.MessageService
import com.papaya.design.platform.bot.image.bot.message.StartGenerationOfImage
import com.papaya.design.platform.bot.image.bot.message.TelegramId
import com.papaya.design.platform.bot.image.bot.payment.PaymentService
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.static.RealisticInteriorBatch
import com.papaya.design.platform.bot.image.bot.user.UserService
import com.papaya.design.platform.bot.image.bot.workflow.GenImageUserState
import com.papaya.design.platform.bot.tg.core.command.workflow.Step

class RealisticInteriorBatchStep(
    private val userService: UserService,
    private val messageService: MessageService,
    private val imageMessageService: ImageMessageService,
    private val paymentService: PaymentService,
) : Step<UserState, GenImageUserState> {

    override val current: UserState = REALISTIC_INTERIOR_BATCH_WAITING_FOR_PHOTO
    override val next: UserState = WAITING_FOR_END_OF_PHOTO_GENERATION
    override val previous: UserState = READY_FOR_CMD

    override fun perform(state: GenImageUserState) {
        val photos = state.photos
        val messageText = state.messageText
        val id = state.id

        when {
            photos != null -> handleIncomingPhotos(id, photos)
            messageText == KeyboardInputButton.START.text -> startBatchGeneration(id)
            else -> messageService.sendMessage(id, RealisticInteriorBatch.Text.WAITING_FOR_BATCH_IMAGE)
        }
    }

    private fun handleIncomingPhotos(id: TelegramId, newPhotos: List<Photo>) {
        var addedPhotos = 0
        var newTotal = 0
        val maxPhotos = RealisticInteriorBatch.MAX_BATCH_SIZE

        userService.saveUser(id) { entity ->
            val currentCount = entity.photos.size
            if (currentCount >= maxPhotos) {
                addedPhotos = -1
                return@saveUser
            }

            val allowedPhotos = newPhotos.take(maxPhotos - currentCount)
            if (allowedPhotos.isEmpty()) {
                addedPhotos = -1
                return@saveUser
            }

            entity.photos = entity.photos + allowedPhotos.map { it.toEntity() }
            addedPhotos = allowedPhotos.size
            newTotal = currentCount + addedPhotos
        }

        when {
            addedPhotos == -1 -> messageService.sendMessage(
                id,
                RealisticInteriorBatch.Text.batchLimitReached(maxPhotos)
            )

            addedPhotos == 0 -> messageService.sendMessage(
                id,
                RealisticInteriorBatch.Text.batchPhotoAdded(newTotal, maxPhotos)
            )

            else -> messageService.sendMessage(
                id,
                RealisticInteriorBatch.Text.batchPhotoAdded(newTotal, maxPhotos)
            )
        }
    }

    private fun startBatchGeneration(id: TelegramId) {
        val user = userService.getUser(id.userId)
        val photos = user.photos
        val photosCount = photos.size

        if (photosCount == 0) {
            messageService.sendMessage(id, RealisticInteriorBatch.Text.BATCH_NEED_AT_LEAST_ONE)
            return
        }

        if (photosCount > RealisticInteriorBatch.MAX_BATCH_SIZE) {
            messageService.sendMessage(
                id,
                RealisticInteriorBatch.Text.batchLimitReached(RealisticInteriorBatch.MAX_BATCH_SIZE)
            )
            return
        }

        if (!paymentService.hasAvailableGenerations(id, photosCount)) {
            messageService.sendMessage(id, Error.Text.ERROR_HAS_NO_GENERATIONS)
            return
        }

        photos.forEach {
            imageMessageService.handlePhotoMessage(
                id = id,
                photos = listOf(it),
                commandState = StartGenerationOfImage.REALISTIC_INTERIOR
            )
        }
    }
}
