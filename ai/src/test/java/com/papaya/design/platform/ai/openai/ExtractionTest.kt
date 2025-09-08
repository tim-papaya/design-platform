package com.papaya.design.platform.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.papaya.design.platform.ai.extractImageInB64
import org.junit.jupiter.api.Test
import kotlin.test.DefaultAsserter.assertTrue

class ExtractionTest {
    @Test
    fun `should extract image in b64 format`() {
        val responseFromAi = Thread.currentThread().contextClassLoader.getResource("result_b64.json").readText()

        val res = ObjectMapper().extractImageInB64(responseFromAi)

        assertTrue("First chars of b64 image", res.startsWith("iVBORw0KGgoAAAANSUh"))
    }
}
