package com.papaya.design.platform.bot.tg.core.command.workflow

abstract class StepProcessor<E : StepEnum, D : StepUserState>(
    stepsList: List<Step<E, D>>
) {
    val steps: Map<E, Step<E, D>> = stepsList.associateBy { it.current }

    fun process(state: E, data: D) {
        val step = steps[state] ?: throw IllegalStateException("State $state not implemented")
        step.perform(data)
    }
}
