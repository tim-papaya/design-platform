package com.papaya.design.platform.bot.image.bot

import com.papaya.design.platform.bot.image.bot.workflow.GenImageStepProcessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test", "mock")
class WorkflowTest {

    @Autowired
    lateinit var genImageStepProcessor: GenImageStepProcessor

    @Test
    fun `should register all steps`() {
        assertEquals(3, genImageStepProcessor.steps.size)
    }
}