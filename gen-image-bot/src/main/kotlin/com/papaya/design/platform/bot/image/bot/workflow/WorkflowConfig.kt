package com.papaya.design.platform.bot.image.bot.workflow

import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.UserState.READY_FOR_CMD
import com.papaya.design.platform.bot.image.bot.domain.UserState.VIDEO_WAITING_FOR_FOR_USER_PROMPT
import com.papaya.design.platform.bot.image.bot.domain.UserState.VIDEO_WAITING_FOR_FOR_USER_SELECTING_MODE
import com.papaya.design.platform.bot.image.bot.domain.UserState.VIDEO_WAITING_FOR_PHOTO
import com.papaya.design.platform.bot.image.bot.domain.UserState.WAITING_FOR_END_OF_PHOTO_GENERATION
import com.papaya.design.platform.bot.image.bot.input.VideoUserInputSelectingMode
import com.papaya.design.platform.bot.image.bot.message.ImageMessageService
import com.papaya.design.platform.bot.image.bot.message.MessageService
import com.papaya.design.platform.bot.image.bot.message.VideoMessageService
import com.papaya.design.platform.bot.image.bot.static.Video
import com.papaya.design.platform.bot.image.bot.workflow.realistic.RealisticInteriorBatchStep
import com.papaya.design.platform.bot.image.bot.user.UserService
import com.papaya.design.platform.bot.image.bot.workflow.template.UserSelectPromptByModeStep
import com.papaya.design.platform.bot.image.bot.workflow.template.UserSelectPromptStep
import com.papaya.design.platform.bot.image.bot.workflow.template.WaitingForPhotoStep
import com.papaya.design.platform.bot.image.bot.payment.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class WorkflowConfig(
    private val userService: UserService,
) {
    @Lazy
    @Autowired
    lateinit var messageService: MessageService

    @Lazy
    @Autowired
    lateinit var videoMessageService: VideoMessageService

    @Lazy
    @Autowired
    lateinit var imageMessageService: ImageMessageService

    @Lazy
    @Autowired
    lateinit var paymentService: PaymentService

    @Bean
    fun realisticInteriorBatchStep() = RealisticInteriorBatchStep(
        userService = userService,
        messageService = messageService,
        imageMessageService = imageMessageService,
        paymentService = paymentService,
    )

    @Bean
    fun videoWaitingForPhoto() =
        WaitingForPhotoStep(
            messageService,
            current = VIDEO_WAITING_FOR_PHOTO,
            next = VIDEO_WAITING_FOR_FOR_USER_SELECTING_MODE,
            previous = READY_FOR_CMD
        )

    @Bean
    fun videoWaitingForUserSelectMode() =
        UserSelectPromptByModeStep(
            messageService,
            current = VIDEO_WAITING_FOR_FOR_USER_SELECTING_MODE,
            next = WAITING_FOR_END_OF_PHOTO_GENERATION,
            previous = VIDEO_WAITING_FOR_PHOTO,
            modes = VideoUserInputSelectingMode.entries.map { it },
            nextCustom = VIDEO_WAITING_FOR_FOR_USER_PROMPT,
            actionOnFinish = startVideoGeneration()
        )

    @Bean
    fun videoWaitingForUserCustomPrompt() =
        UserSelectPromptStep(
            messageService = messageService,
            current = VIDEO_WAITING_FOR_FOR_USER_PROMPT,
            previous = VIDEO_WAITING_FOR_FOR_USER_SELECTING_MODE,
            next = WAITING_FOR_END_OF_PHOTO_GENERATION,
            actionOnFinish = startVideoGeneration()
        )

    private fun startVideoGeneration(): (GenImageUserState) -> Unit = { s ->
        val user = userService.getUser(s.id.userId)
        videoMessageService.handleVideoMessage(
            id = s.id,
            userPrompt = user.userPrompt
                ?: throw IllegalArgumentException("Not  found user prompt for video generation"),
            systemPrompt = Video.Prompt.SYSTEM_PROMPT,
            // most quality
            inputReferenceJpeg = user.photos.last(),
        )
    }
}
