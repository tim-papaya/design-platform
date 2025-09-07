package com.papaya.design.platform.bot.image.bot.log

interface TracingService {
    fun logPrompt(chatId: Long, prompt: String)
    fun logImage(chatId: Long, image: ByteArray, extension: String)
    fun logResultImage(chatId: Long, image: ByteArray, extension: String)
}