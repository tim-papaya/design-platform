package com.papaya.design.platform.ai.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("file:\${user.home}/.app-config/design-platform/ai.props")
class AiConfig {
}