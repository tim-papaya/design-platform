package com.papaya.design.platform.ai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@Import(AiConfig::class)
class AiApplication

fun main(args: Array<String>) {
    runApplication<AiApplication>(*args)
}
