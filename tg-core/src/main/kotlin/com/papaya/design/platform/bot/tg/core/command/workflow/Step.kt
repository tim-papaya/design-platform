package com.papaya.design.platform.bot.tg.core.command.workflow

interface Step<E: StepEnum, T : StepUserState> {
    val current: E
    val next: E
    val previous: E

    fun perform(state: T)
}
