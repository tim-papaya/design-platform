package com.papaya.design.platform.bot.image.bot.payment

import java.math.BigInteger

const val GENERATION_TOKENS_FOR_FULL_IMAGE_GENERATION = 8
const val GENERATION_TOKENS_FOR_ROTATION = 2

enum class PaymentAmount(val price: Int, val amount: Long) {
    LOWEST_GENERATION_PACKET(149, 3),
    LOW_GENERATION_PACKET(449, 10),
    AVERAGE_GENERATION_PACKET(999, 25),
    ABOVE_AVERAGE_PACKET(1849, 50),
    LARGE_PACKET(3499, 100);

    val label: String
        get() = "$amountAsText - ${price}р"

    val amountAsText: String
        get() = "Пакет из $amount генераций"

    val priceWithCents: BigInteger
        get() = price.toBigInteger() * BigInteger.valueOf(100)
}

fun Int.toFullImageGenerationAmount() : Double =
    this.toDouble() / GENERATION_TOKENS_FOR_FULL_IMAGE_GENERATION

fun Int.toGenerationTokens(): Int =
    this * GENERATION_TOKENS_FOR_FULL_IMAGE_GENERATION
