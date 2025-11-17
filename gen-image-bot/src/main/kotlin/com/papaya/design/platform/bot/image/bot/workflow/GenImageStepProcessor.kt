package com.papaya.design.platform.bot.image.bot.workflow

import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.tg.core.command.workflow.Step
import com.papaya.design.platform.bot.tg.core.command.workflow.StepProcessor
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
@Lazy
@Import(WorkflowConfig::class)
class GenImageStepProcessor(
    stepsList: List<Step<UserState, GenImageUserState>>
) : StepProcessor<UserState, GenImageUserState>(stepsList) {

    @PostConstruct
    fun logAfterCreate() {
        log.info { "Registered ${steps.size} steps in workflow" }
    }
}
