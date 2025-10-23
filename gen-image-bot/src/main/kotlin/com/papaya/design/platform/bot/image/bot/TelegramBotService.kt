package com.papaya.design.platform.bot.image.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.preCheckoutQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.bot.image.bot.domain.User
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.UserState.*
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.message.*
import com.papaya.design.platform.bot.image.bot.message.StartGenerationOfImage.Companion.PLANED_REALISTIC_INTERIOR
import com.papaya.design.platform.bot.image.bot.message.TelegramCommand.START_CMD
import com.papaya.design.platform.bot.image.bot.message.WaitingPhotoState.Companion.PLANED_BEFORE_OPTIONS
import com.papaya.design.platform.bot.image.bot.message.WaitingPhotoState.Companion.PLANED_BEFORE_PLAN
import com.papaya.design.platform.bot.image.bot.payment.PaymentAmount
import com.papaya.design.platform.bot.image.bot.payment.PaymentService
import com.papaya.design.platform.bot.image.bot.static.Error
import com.papaya.design.platform.bot.image.bot.static.ExtendedRealisticInterior
import com.papaya.design.platform.bot.image.bot.static.General
import com.papaya.design.platform.bot.image.bot.static.Payment
import com.papaya.design.platform.bot.image.bot.static.RealisticInterior
import com.papaya.design.platform.bot.image.bot.static.RoomUpgrade
import com.papaya.design.platform.bot.image.bot.static.Support
import com.papaya.design.platform.bot.image.bot.user.UserService
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

private val log = KotlinLogging.logger { }

@Configuration
@Profile("prod")
class TelegramBotService(
    @Value("\${telegram.api-key}")
    private val apiKey: String,
    private val userService: UserService,
    @Value("\${support.admin.id}")
    private val supportId: Long,
) : BotService {
    @Autowired
    lateinit var messageService: MessageService

    @Autowired
    lateinit var paymentService: PaymentService

    @Autowired
    lateinit var imageMessageService: ImageMessageService

    @Bean
    fun bot(): Bot = bot {
        token = apiKey
        dispatch {
            command(START_CMD.text) {
                messageService.sendFirstTimeWelcome(message.telegramId())
            }
            command(TelegramCommand.SUPPORT.text) {
                sendMessageToSupport(bot, message.telegramId(), message)
            }
            command(TelegramCommand.SEND_ALL.text) {
                val id = message.telegramId()
                val text = messageWithoutCommand(message.text)
                if (id.userId != supportId) return@command

                if (text == null) {
                    messageService.sendWarningMessage(id, "Where is no text to send")
                    return@command
                }

                userService.getAllUserIds().forEach {
                    bot.sendMessage(ChatId.fromId(it), text)
                }
            }
            preCheckoutQuery {
                log.info { "Confirming payment ${preCheckoutQuery.id} ${preCheckoutQuery.totalAmount}" }
                bot.answerPreCheckoutQuery(preCheckoutQuery.id, true)
                    .onSuccess { log.info { "Confirmed payment for  ${preCheckoutQuery.id}" } }
                    .onError { e -> log.info { "Error on confirming message $e" } }
            }
            message(Filter.Custom { successfulPayment != null }) {
                val paymentInfo = paymentService.extractPaymentInfo(message.successfulPayment!!.invoicePayload)
                userService.saveUser(paymentInfo.id) { u ->
                    u.generations += paymentInfo.Amount
                }
                log.info { "User ${paymentInfo.id} bought ${paymentInfo.Amount} generations" }
                messageService.sendMessageAndReturnToMainMenu(message.telegramId(), Payment.Text.SUCCSESFUL_PAYMENT)
            }
            message {
                val id = message.telegramId()

                val user = userService.getUserOrNull(id.userId)
                    ?: messageService.sendFirstTimeWelcome(id)

                try {
                    val messageText = message.text
                    val photos = extractPhotoFromMessage()

                    if (messageText == KeyboardInputButton.CANCEL.text) {
                        // TODO VALIDATE PHOTOS
                        userService.saveUser(id) { u ->
                            u.photos = listOf()
                        }
                        messageService.sendMessageAndReturnToMainMenu(id, General.Text.NEXT_STEP)
                        return@message
                    }

                    when (user.userState) {
                        READY_FOR_CMD -> {
                            when (messageText) {
                                KeyboardInputButton.START.text ->
                                    messageService.sendFirstTimeWelcome(id)

                                KeyboardInputButton.GENERATE_REALISTIC_INTERIOR.text -> {
                                    if (!paymentService.hasAvailableGenerations(id)) {
                                        messageService.sendWarningMessage(id, Error.Text.ERROR_HAS_NO_GENERATIONS)
                                        return@message
                                    }
                                    messageService.sendWaitingForPhotoMessage(
                                        id, ImageGenerationStrategy.START_REALISTIC_INTERIOR_GENERATION
                                    )
                                }

                                KeyboardInputButton.ROOM_UPGRADE.text -> {
                                    if (!paymentService.hasAvailableGenerations(id)) {
                                        messageService.sendWarningMessage(id, Error.Text.ERROR_HAS_NO_GENERATIONS)
                                        return@message
                                    }
                                    messageService.sendWaitingForPhotoMessage(
                                        id, ImageGenerationStrategy.START_ROOM_UPGRADE_GENERATION
                                    )
                                }

                                KeyboardInputButton.GENERATE_EXTENDED_REALISTIC_INTERIOR.text -> {
                                    if (!paymentService.hasAvailableGenerations(id)) {
                                        messageService.sendWarningMessage(id, Error.Text.ERROR_HAS_NO_GENERATIONS)
                                        return@message
                                    }
                                    messageService.sendWaitingForPhotoMessage(
                                        id, ImageGenerationStrategy.START_EXTENDED_REALISTIC_INTERIOR_GENERATION
                                    )
                                }

                                KeyboardInputButton.PLANNED_REALISTIC_INTERIOR.text -> {
                                    if (!paymentService.hasAvailableGenerations(id)) {
                                        messageService.sendWarningMessage(id, Error.Text.ERROR_HAS_NO_GENERATIONS)
                                        return@message
                                    }
                                    messageService.sendWaitingForPhotoMessage(
                                        id, ImageGenerationStrategy.START_PLANED_REALISTIC_INTERIOR_GENERATION
                                    )
                                }

                                KeyboardInputButton.PAYMENT.text -> {
                                    messageService.sendStateMessage(id, UserState.SELECTING_PAYMENT_OPTION)
                                }

                                KeyboardInputButton.SUPPORT.text -> {
                                    messageService.sendStateMessage(id, CONFIRMING_SUPPORT_MESSAGE)
                                }

                                KeyboardInputButton.CHECK_STATUS.text -> {
                                    messageService.sendMessageAndReturnToMainMenu(
                                        id,
                                        "${General.Text.GENERATIONS_AMOUNT} ${user.generationsNumber}"
                                    )
                                }

                            }
                        }

                        REALISTIC_INTERIOR_WAITING_FOR_PHOTO -> {
                            if (photos != null) {
                                imageMessageService.handlePhotoMessage(
                                    id, photos, StartGenerationOfImage.REALISTIC_INTERIOR,
                                )
                            } else {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(id.chatId),
                                    text = RealisticInterior.Text.WAITING_FOR_IMAGE,
                                    replyMarkup = onlyBackKeyboard(),
                                )
                            }
                        }

                        ROOM_UPGRADE_WAITING_FOR_PHOTO -> {
                            if (photos != null) {
                                userService.saveUser(id) { u ->
                                    u.photos = photos.map { it.toEntity() }
                                    u.userState = ROOM_UPGRADE_WAITING_FOR_USER_OPTION
                                }

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
                                        id,
                                        photosFromUser,
                                        StartGenerationOfImage.ROOM_UPGRADE,
                                        RoomUpgrade.Prompt.FOR_RENT
                                    )
                                }

                                KeyboardInputButton.OPTION_FOR_SELF.text -> {
                                    val photosFromUser = user.photos
                                    imageMessageService.handlePhotoMessage(
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
                                userService.saveUser(id) { u ->
                                    u.photos = photos.map { it.toEntity() }
                                    u.userState = EXTENDED_REALISTIC_INTERIOR_WAITING_FOR_USER_PROMPT
                                }

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
                                userService.saveUser(id) { u ->
                                    u.userPrompt = messageText
                                    u.userState = EXTENDED_REALISTIC_INTERIOR_WAITING_ADDITIONAL_PHOTO
                                }

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
                                    id,
                                    savedPhotos,
                                    StartGenerationOfImage.EXTENDED_REALISTIC_INTERIOR,
                                    savedPrompt
                                )
                            } else if (photos != null) {
                                // TODO Add validation of photos merge
                                userService.saveUser(id) { u ->
                                    u.photos = (user.photos + photos).map { it.toEntity() }
                                }

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
                            userService.saveUser(id) { u ->
                                u.photos = listOf()
                            }

                            messageService.sendMessageOnWaitingForPhoto(id, photos, PLANED_BEFORE_PLAN)
                        }

                        PLANNED_REALISTIC_INTERIOR_WAITING_FOR_PLAN -> {
                            messageService.sendMessageOnWaitingForPhoto(id, photos, PLANED_BEFORE_OPTIONS)
                        }

                        PLANNED_REALISTIC_INTERIOR_WAITING_FOR_USER_OPTION -> {
                            if (messageText != null) {
                                imageMessageService.handlePhotoMessage(
                                    id,
                                    user.photos,
                                    PLANED_REALISTIC_INTERIOR,
                                    messageText
                                )
                            }
                        }

                        UserState.SELECTING_PAYMENT_OPTION -> {
                            when (messageText) {
                                PaymentAmount.LOWEST_GENERATION_PACKET.label -> {
                                    paymentService.sendInvoice(id, PaymentAmount.LOWEST_GENERATION_PACKET)
                                }

                                PaymentAmount.LOW_GENERATION_PACKET.label -> {
                                    paymentService.sendInvoice(id, PaymentAmount.LOW_GENERATION_PACKET)
                                }

                                PaymentAmount.AVERAGE_GENERATION_PACKET.label -> {
                                    paymentService.sendInvoice(id, PaymentAmount.AVERAGE_GENERATION_PACKET)
                                }

                                PaymentAmount.ABOVE_AVERAGE_PACKET.label -> {
                                    paymentService.sendInvoice(id, PaymentAmount.ABOVE_AVERAGE_PACKET)
                                }

                                PaymentAmount.LARGE_PACKET.label -> {
                                    paymentService.sendInvoice(id, PaymentAmount.LARGE_PACKET)
                                }

                                else -> {
                                    messageService.sendMessage(id, Payment.Text.SELECT_PAYMENT_OPTION)
                                    return@message
                                }
                            }
                            messageService.sendMessageAndReturnToMainMenu(id, Payment.Text.PAYMENT_PREPARED)
                        }

                        UserState.CONFIRMING_SUPPORT_MESSAGE -> {
                            if (message.text.isNullOrBlank())
                                messageService.sendMessage(id, Support.Error.ERROR_EMPTY_MESSAGE)
                            sendMessageToSupport(bot, message.telegramId(), message)
                            messageService.sendMessageAndReturnToMainMenu(id, Support.Text.CONFIRMING_SUPPORT_MESSAGE)

                        }
                    }
                } catch (e: Exception) {
                    log.error(e) { "Unknown error" }
                }
            }
        }
    }

    private fun sendMessageToSupport(bot: Bot, id: TelegramId, message: Message) {
        bot.sendMessage(
            ChatId.fromId(supportId),
            "User ${message.chat.username}:${id.userId} send: ${message.text}"
        )
    }

    @PostConstruct
    fun init() {
        bot().startPolling()
        log.info { "Telegram bot started" }
    }

    private fun MessageHandlerEnvironment.extractPhotoFromMessage(): List<Photo>? =
        message.photo
            ?.sortedBy { it.fileSize }
            ?.map {
                log.info { "Received file id:uid:size:WxH - ${it.fileId}:${it.fileUniqueId}:${it.fileSize}:${it.width}x${it.height}" }
                it
//        }?.let { photoSizeList ->
//            photoSizeList.first { it.height >= 300 && it.width >= 300 }
            }?.let {
                it.last()
            }?.let { Photo(it.fileId, it.fileUniqueId, it.width, it.height) }
            ?.let { listOf(it) }
}

private fun messageWithoutCommand(text: String?): String? =
    text?.trim()?.split(Regex("\\s+"), limit = 2)?.drop(1)?.firstOrNull()