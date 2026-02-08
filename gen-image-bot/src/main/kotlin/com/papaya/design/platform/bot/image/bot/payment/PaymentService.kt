package com.papaya.design.platform.bot.image.bot.payment

import com.papaya.design.platform.bot.image.bot.message.TelegramId
import com.papaya.design.platform.bot.image.bot.user.UserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

data class PaymentInfo(val id: Long, val amount: Int)

interface PaymentProvider {
    fun sendInvoice(id: TelegramId, paymentAmount: PaymentAmount)
    fun extractPaymentInfo(invoicePayload: String): PaymentInfo
}

@Lazy
@Service
class PaymentService(
    private val userService: UserService,
    private val paymentProvider: PaymentProvider,
) {

    fun hasAvailableGenerations(id: TelegramId, min: Int = 1): Boolean =
        userService.getUser(id.userId).generationsNumber >= min

    fun sendInvoice(id: TelegramId, paymentAmount: PaymentAmount) =
        paymentProvider.sendInvoice(id, paymentAmount)

    fun extractPaymentInfo(invoicePayload: String) =
        paymentProvider.extractPaymentInfo(invoicePayload)
}
