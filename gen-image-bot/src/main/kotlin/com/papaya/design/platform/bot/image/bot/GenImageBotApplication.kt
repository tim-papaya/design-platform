package com.papaya.design.platform.bot.image.bot

import com.papaya.design.platform.ai.AiConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@Import(AiConfig::class)
@PropertySource("file:\${user.home}/.app-config/design-platform/image-bot.props")
@EnableCaching
class GenImageBotApplication

fun main(args: Array<String>) {
    runApplication<GenImageBotApplication>(*args)
}
