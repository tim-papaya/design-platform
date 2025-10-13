package com.papaya.design.platform.bot.image.bot.payment

import java.math.BigInteger

enum class PaymentAmount(val text: String, val price: Int, val amount: Long) {
    LOWEST_GENERATION_PACKET("генерации", 149, 3),
    LOW_GENERATION_PACKET("генераций", 449, 10);

    val label: String
        get() = "$amount $text - ${price}р"

    val amountAsText: String
        get() = "$amount $text"

    val priceWithCents: BigInteger
        get() = price.toBigInteger() * BigInteger.valueOf(100)

    // 3 10 25 50 100
}