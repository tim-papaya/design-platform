package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.papaya.design.platform.ai.openai.OpenAiImageService
import com.papaya.design.platform.bot.image.bot.domain.Photo
import com.papaya.design.platform.bot.image.bot.domain.UserState.*
import com.papaya.design.platform.bot.image.bot.message.*
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.REAL_IMAGE_CMD
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.START_CMD
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
                val chatId = message.chat.id
                messageService.sendFirstTimeWelcome(bot, chatId)
            }
            command(REAL_IMAGE_CMD.text) {
                val chatId = message.chat.id
                messageService.sendWaitingForPhotoMessage(
                    bot,
                    chatId,
                    StartWaitingForImageCommandState.START_REALISTIC_INTERIOR_GENERATION
                )
            }

            command(TelegramCommand.LOW_QUALITY.text) {
                val chatId = message.chat.id
                userService.getUser(chatId).qualityPreset = OpenAiImageService.QualityPreset.LOW

                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Выбрано низкое качество генерации",
                )
                log.info { "Low quality selected by $chatId" }
            }
            command(TelegramCommand.AVERAGE_QUALITY.text) {
                val chatId = message.chat.id
                userService.getUser(chatId).qualityPreset = OpenAiImageService.QualityPreset.AVERAGE

                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Выбрано среднее качество генерации",
                )
                log.info { "Average quality selected by $chatId" }
            }

            command(TelegramCommand.HIGH_QUALITY.text) {
                val chatId = message.chat.id
                userService.getUser(chatId).qualityPreset = OpenAiImageService.QualityPreset.HIGH

                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Выбрано высокое качество генерации",
                )
                log.info { "High quality selected by $chatId" }
            }


            message {
                val chatId = message.chat.id
                val user = userService.getUserOrNull(chatId)
                    ?: messageService.sendFirstTimeWelcome(bot, chatId)

                val messageText = message.text
                val photos = message.photo
                    ?.sortedBy { it.fileSize }
                    ?.map {
                        //TODO Remove additional logging
                        log.info { "Received file id:uid:size:WxH - ${it.fileId}:${it.fileUniqueId}:${it.fileSize}:${it.width}x${it.height}" }
                        it
                    }?.let { photoSizeList ->
                        if (user.qualityPreset != OpenAiImageService.QualityPreset.HIGH) {
                            photoSizeList.first { it.height >= 300 && it.width >= 300 }
                        } else {
                            photoSizeList.last()
                        }
                    }?.let { Photo().apply { fileId = it.fileId; fileUniqueId = it.fileUniqueId } }
                    ?.let { listOf(it) }

                if (messageText == KeyboardInputButton.CANCEL.text) {
                    messageService.sendGenerationCompletionMessage(bot, chatId, "Return to main menu")
                }

                when (user.userState) {
                    READY_FOR_CMD -> {
                        when (messageText) {
                            KeyboardInputButton.START.text -> {
                                messageService.sendFirstTimeWelcome(bot, chatId)
                            }

                            KeyboardInputButton.GENERATE_REALISTIC_INTERIOR.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    chatId,
                                    StartWaitingForImageCommandState.START_REALISTIC_INTERIOR_GENERATION
                                )
                            }

                            KeyboardInputButton.ROOM_UPGRADE.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    chatId,
                                    StartWaitingForImageCommandState.START_ROOM_UPGRADE_GENERATION
                                )
                            }

                            KeyboardInputButton.GENERATE_EXTENDED_REALISTIC_INTERIOR.text -> {
                                messageService.sendWaitingForPhotoMessage(
                                    bot,
                                    chatId,
                                    StartWaitingForImageCommandState.START_EXTENDED_REALISTIC_INTERIOR_GENERATION
                                )
                            }
                        }
                    }

                    REALISTIC_INTERIOR_WAITING_FOR_PHOTO -> {
                        if (photos != null) {
                            imageMessageService.handlePhotoMessage(
                                bot,
                                chatId,
                                photos,
                                StartGenerationOfImage.REALISTIC_INTERIOR,
                            )
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
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
                                chatId = ChatId.fromId(chatId),
                                text = RoomUpgrade.Text.WAITING_FOR_UPGRADE_OPTION,
                                replyMarkup = roomUpgrade()
                            )

                            log.info("Added photo for interior upgrade")
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = RoomUpgrade.Text.WAITING_FOR_IMAGE
                            )
                        }
                    }

                    ROOM_UPGRADE_WAITING_FOR_USER_OPTION -> {
                        when (messageText) {
                            KeyboardInputButton.OPTION_FOR_RENT.text -> {
                                val photosFromUser = user.photos
                                imageMessageService.handlePhotoMessage(
                                    bot,
                                    chatId,
                                    photosFromUser,
                                    StartGenerationOfImage.ROOM_UPGRADE,
                                    RoomUpgrade.Prompt.FOR_RENT
                                )
                            }

                            KeyboardInputButton.OPTION_FOR_SELF.text -> {
                                val photosFromUser = user.photos
                                imageMessageService.handlePhotoMessage(
                                    bot,
                                    chatId,
                                    photosFromUser,
                                    StartGenerationOfImage.ROOM_UPGRADE,
                                    RoomUpgrade.Prompt.FOR_SELF
                                )
                            }

                            else -> {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(chatId),
                                    text = RoomUpgrade.Text.WAITING_FOR_UPGRADE_OPTION,
                                    replyMarkup = roomUpgrade(),
                                )
                            }
                        }
                    }

                    WAITING_FOR_END_OF_PHOTO_GENERATION -> {
                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = General.Text.IMAGE_STILL_GENERATING,
                            replyMarkup = removeKeyboard(),
                        )
                    }

                    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_PHOTO -> {
                        if (photos != null) {
                            user.photos = photos
                            user.userState = EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_USER_PROMPT,
                                replyMarkup = removeKeyboard()
                            )
                            log.info("Added photo for extended interior upgrade")

                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_IMAGE,
                                replyMarkup = removeKeyboard(),
                            )
                        }
                    }

                    EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT -> {
                        if (messageText != null) {
                            user.userPrompt = messageText
                            user.userState = EXTENDED_REALISTIC_INTERIOR_WAITING_ADDITIONAL_PHOTO

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
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
                                chatId,
                                savedPhotos,
                                StartGenerationOfImage.EXTENDED_REALISTIC_INTERIOR,
                                savedPrompt
                            )
                        } else if (photos != null) {
                            // TODO Add validation of photos merge
                            user.photos = user.photos + photos

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = ExtendedRealisticInterior.Text.ACCEPTED_ADDITIONAL_IMAGES,
                                replyMarkup = prepareForExtendedRealisticGeneration()
                            )
                            log.info("Added additional photo for extended interior upgrade")
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = ExtendedRealisticInterior.Text.WAITING_FOR_IMAGE
                            )
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
}
