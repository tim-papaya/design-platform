package com.papaya.design.platform.ai.config

import com.papaya.design.platform.ai.OpenAiImageService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("file:\${user.home}/.app-config/design-platform/ai.props")
class AiConfig {

    @Bean
    @Qualifier("openAiImageService")
    fun openAiImageService() =
        OpenAiImageService()
}