package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.papaya.design.platform.ai.openai.OpenAiImageService.QualityPreset.Companion.AVERAGE
import com.papaya.design.platform.ai.openai.OpenAiImageService.QualityPreset.Companion.HIGH
import com.papaya.design.platform.ai.openai.OpenAiImageService.QualityPreset.Companion.LOW
import com.papaya.design.platform.bot.image.bot.domain.Photo
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState.*
import com.papaya.design.platform.bot.image.bot.message.*
import com.papaya.design.platform.bot.image.bot.message.StartGenerationOfImage.Companion.PLANED_REALISTIC_INTERIOR
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.REAL_IMAGE_CMD
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.START_CMD
import com.papaya.design.platform.bot.image.bot.message.WaitingPhotoState.Companion.PLANED_BEFORE_OPTIONS
import com.papaya.design.platform.bot.image.bot.message.WaitingPhotoState.Companion.PLANED_BEFORE_PLAN
import com.papaya.design.platform.bot.image.bot.static.ExtendedRealisticInterior
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.static.RealisticInterior
import com.papaya.design.platform.bot.image.bot.static.RoomUpgrade
import com.papaya.design.platform.bot.image.bot.user.UserService
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    private val userService: UserService,
    private val messageService: MessageService,
    private val imageMessageService: ImageMessageService,
) : BotService {

    private val bot = bot {
        token = apiKey
        dispatch {
            command(START_CMD.text) {
                messageService.sendFirstTimeWelcome(bot, message.userId())
            }
            command(REAL_IMAGE_CMD.text) {
                val id = message.telegramId()
                messageService.sendWaitingForPhotoMessage(
                    bot,
                    id,
                    StartWaitingForImageCommandState.START_REALISTIC_INTERIOR_GENERATION
                )
            }
            command(TelegramCommand.LOW_QUALITY.text) {
                val id = message.telegramId()
                messageService.sendQualityMessage(bot, id, "Выбрано низкое качество генерации", LOW)
            }
            command(TelegramCommand.AVERAGE_QUALITY.text) {
                val id = message.telegramId()
                messageService.sendQualityMessage(bot, id, "Выбрано среднее качество генерации", AVERAGE)
            }
            command(TelegramCommand.HIGH_QUALITY.text) {
                val id = message.telegramId()
                messageService.sendQualityMessage(bot, id, "Выбрано высокое качество генерации", HIGH)
            }
            message {
                val id = TelegramId(message.chat.id, message.userId())

                val user = getUserOrCreate(id.userId)
                val messageText = message.text
                val photos = extractPhotoFromMessage(user)

                if (messageText == KeyboardInputButton.CANCEL.text) {
                    // TODO VALIDATE PHOTOS
                    user.photos = listOf()
                    messageService.sendGenerationCompletionMessage(bot, id, "Return to main menu")
                }

                when (user.userState) {
                    READY_FOR_CMD -> {
                        when (messageText) {
                            KeyboardInputButton.START.text -> messageService.sendFirstTimeWelcome(bot, user.id)

                            KeyboardInputButton.GENERATE_REALISTIC_INTERIOR.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    id,
                                    StartWaitingForImageCommandState.START_REALISTIC_INTERIOR_GENERATION
                                )
                            }

                            KeyboardInputButton.ROOM_UPGRADE.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    id,
                                    StartWaitingForImageCommandState.START_ROOM_UPGRADE_GENERATION
                                )
                            }

                            KeyboardInputButton.GENERATE_EXTENDED_REALISTIC_INTERIOR.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    id,
                                    StartWaitingForImageCommandState.START_EXTENDED_REALISTIC_INTERIOR_GENERATION
                                )
                            }

                            KeyboardInputButton.PLANNED_REALISTIC_INTERIOR.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    id,
                                    StartWaitingForImageCommandState.START_PLANED_REALISTIC_INTERIOR_GENERATION
                                )
                            }
                        }
                    }

                    REALISTIC_INTERIOR_WAITING_FOR_PHOTO -> {
                        if (photos != null) {
                            imageMessageService.handlePhotoMessage(
                                bot,
                                id,
                                photos,
                                StartGenerationOfImage.REALISTIC_INTERIOR,
                            )
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = RealisticInterior.Text.WAITING_FOR_IMAGE,
                                replyMarkup = removeKeyboard(),
                            )
                        }
                    }

                    ROOM_UPGRADE_WAITING_FOR_PHOTO -> {
                        if (photos != null) {
                            user.photos = photos
                            user.userState = ROOM_UPGRADE_WAITING_FOR_USER_OPTION

                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = RoomUpgrade.Text.WAITING_FOR_UPGRADE_OPTION,
                                replyMarkup = roomUpgrade()
                            )

                            log.info("Added photo for interior upgrade")
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = RoomUpgrade.Text.WAITING_FOR_IMAGE,
                                replyMarkup = onlyBackKeyboard()
                            )
                        }
                    }

                    ROOM_UPGRADE_WAITING_FOR_USER_OPTION -> {
                        when (messageText) {
                            KeyboardInputButton.OPTION_FOR_RENT.text -> {
                                val photosFromUser = user.photos
                                imageMessageService.handlePhotoMessage(
                                    bot,
                                    id,
                                    photosFromUser,
                                    StartGenerationOfImage.ROOM_UPGRADE,
                                    RoomUpgrade.Prompt.FOR_RENT
                                )
                            }

                            KeyboardInputButton.OPTION_FOR_SELF.text -> {
                                val photosFromUser = user.photos
                                imageMessageService.handlePhotoMessage(
                                    bot,
                                    id,
                                    photosFromUser,
                                    StartGenerationOfImage.ROOM_UPGRADE,
                                    RoomUpgrade.Prompt.FOR_SELF
                                )
                            }

                            else -> {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(id.chatId),
                                    text = RoomUpgrade.Text.WAITING_FOR_UPGRADE_OPTION,
                                    replyMarkup = roomUpgrade(),
                                )
                            }
                        }
                    }

                    WAITING_FOR_END_OF_PHOTO_GENERATION -> {
                        bot.sendMessage(
                            chatId = ChatId.fromId(id.chatId),
                            text = General.Text.IMAGE_STILL_GENERATING,
                            replyMarkup = removeKeyboard(),
                        )
                    }

                    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO -> {
                        if (photos != null) {
                            user.photos = photos
                            user.userState = EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT

                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_USER_PROMPT,
                                replyMarkup = onlyBackKeyboard()
                            )
                            log.info("Added photo for extended interior upgrade")

                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_IMAGE,
                                replyMarkup = onlyBackKeyboard(),
                            )
                        }
                    }

                    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT -> {
                        if (messageText != null) {
                            user.userPrompt = messageText
                            user.userState = EXTENDED_REALISTIC_INTERIOR_WAITING_ADDITIONAL_PHOTO

                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_ADDITIONAL_IMAGES,
                                replyMarkup = prepareForExtendedRealisticGeneration()
                            )
                            log.info("Added user prompt for extended interior upgrade")
                        }
                    }

                    EXTENDED_REALISTIC_INTERIOR_WAITING_ADDITIONAL_PHOTO -> {
                        if (messageText == KeyboardInputButton.EXTENDED_REALISTIC_INTERIOR_READY_FOR_GENERATION.text) {
                            val savedPhotos = user.photos
                            val savedPrompt = user.userPrompt
                            imageMessageService.handlePhotoMessage(
                                bot,
                                id,
                                savedPhotos,
                                StartGenerationOfImage.EXTENDED_REALISTIC_INTERIOR,
                                savedPrompt
                            )
                        } else if (photos != null) {
                            // TODO Add validation of photos merge
                            user.photos = user.photos + photos

                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = ExtendedRealisticInterior.Text.ACCEPTED_ADDITIONAL_IMAGES,
                                replyMarkup = prepareForExtendedRealisticGeneration(),
                            )
                            log.info("Added additional photo for extended interior upgrade")
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(id.chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_IMAGE,
                                replyMarkup = onlyBackKeyboard(),
                            )
                        }
                    }

                    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO -> {
                        // TODO VALIDATE PHOTOS
                        user.photos = listOf()
                        messageService.sendMessageOnWaitingForPhoto(bot, id, photos, PLANED_BEFORE_PLAN)
                    }

                    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PLAN -> {
                        messageService.sendMessageOnWaitingForPhoto(bot, id, photos, PLANED_BEFORE_OPTIONS)
                    }

                    PLANNED_REALISTIC_INTERIOR_WAITING_FOR_USER_OPTION -> {
                        if (messageText != null) {
                            imageMessageService.handlePhotoMessage(bot, id, user.photos, PLANED_REALISTIC_INTERIOR, messageText)
                        }
                    }
                }
            }
        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
        log.info { "Telegram bot started" }
    }

    private fun MessageHandlerEnvironment.extractPhotoFromMessage(user: User): List<Photo>? = message.photo
        ?.sortedBy { it.fileSize }
        ?.map {
            //TODO Remove additional logging
            log.info { "Received file id:uid:size:WxH - ${it.fileId}:${it.fileUniqueId}:${it.fileSize}:${it.width}x${it.height}" }
            it
        }?.let { photoSizeList ->
            if (user.qualityPreset != HIGH) {
                photoSizeList.first { it.height >= 300 && it.width >= 300 }
            } else {
                photoSizeList.last()
            }
        }?.let { Photo().apply { fileId = it.fileId; fileUniqueId = it.fileUniqueId } }
        ?.let { listOf(it) }

    private fun MessageHandlerEnvironment.getUserOrCreate(userId: Long): User = (userService.getUserOrNull(userId)
        ?: messageService.sendFirstTimeWelcome(bot, userId))
}
