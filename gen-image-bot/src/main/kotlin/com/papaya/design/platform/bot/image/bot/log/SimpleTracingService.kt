package com.papaya.design.platform.bot.image.bot.log

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

@Service
class DummyTracingService : TracingService {
    override fun logPrompt(chatId: Long, prompt: String) {
    }

    override fun logImage(chatId: Long, image: ByteArray, extension: String) {
    }

    override fun logResultImage(chatId: Long, image: ByteArray, extension: String) {
    }
}

class SimpleTracingService(
    @Value("\${com.papaya.design.platform.bot.image.trace-log.path:}")
    private val logPath: String
) : TracingService {
    private val homeDirectory = System.getProperty("user.home")
    private val modifiedLogPath = logPath.replace("~", homeDirectory)

    override fun logPrompt(chatId: Long, prompt: String) {
        Path.of(modifiedLogPath, "$chatId:${LocalDateTime.now()}_prompt.txt")
            .writeText(prompt, Charsets.UTF_8, StandardOpenOption.CREATE_NEW)
    }

    override fun logImage(chatId: Long, image: ByteArray, extension: String) {
        logImage(chatId, extension, image, "image")
    }

    override fun logResultImage(chatId: Long, image: ByteArray, extension: String) {
        logImage(chatId, extension, image, "result_image")
    }

    private fun logImage(chatId: Long, extension: String, image: ByteArray, imageName: String) {
        Path.of(modifiedLogPath, "$chatId:${LocalDateTime.now()}_$imageName.$extension")
            .writeBytes(image, StandardOpenOption.CREATE_NEW)
    }
}