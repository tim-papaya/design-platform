package com.papaya.design.platform.ai

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("file:\${user.home}/.app-config/design-platform/ai.props")
@ComponentScan()
class AiConfig {

    @Bean
    fun objectMapper() =
        ObjectMapper()
}