package com.papaya.design.platform.bot.image.bot

import com.papaya.design.platform.ai.AiConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource

fun main(args: Array<String>) {
    runApplication<GenImageBotApplication>(*args)
}

@SpringBootApplication
@Import(AiConfig::class)
@PropertySource("file:\${user.home}/.app-config/design-platform/image-bot.props")
@PropertySource("file:\${user.home}/.app-config/design-platform/db.props")
@EnableCaching
class GenImageBotApplication


