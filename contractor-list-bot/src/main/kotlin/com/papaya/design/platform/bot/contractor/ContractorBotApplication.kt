package com.papaya.design.platform.bot.contractor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.PropertySource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

fun main(args: Array<String>) {
    runApplication<ContractorBotApplication>(*args)
}

@SpringBootApplication
@PropertySource("file:\${user.home}/.app-config/design-platform/contractor-bot/general.props")
@EnableCaching
@EnableJpaRepositories
class ContractorBotApplication


