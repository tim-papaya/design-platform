package com.papaya.design.platform.bot.image.bot.payment

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.payments.InvoiceUserDetail
import com.github.kotlintelegrambot.entities.payments.LabeledPrice
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.papaya.design.platform.bot.image.bot.message.TelegramId
import com.papaya.design.platform.bot.image.bot.user.UserService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import kotlin.math.log

private const val PAYLOAD_SEPARATOR = ":-:"
private val log = KotlinLogging.logger { }

@Lazy
@Service
class PaymentService(
    private val bot: Bot,
    @Value("\${com.papaya.design.platform.bot.image.shop.token}")
    private val shopToken: String,
    private val userService: UserService
) {

    fun hasAvailableGenerations(id: TelegramId): Boolean =
        userService.getUser(id.userId).generationsNumber > 0

    fun sendInvoice(id: TelegramId, paymentAmount: PaymentAmount) {
        bot.sendInvoice(
            ChatId.fromId(id.chatId),
            PaymentInvoiceInfo(
                title = "Покупка генераций изображений",
                description = paymentAmount.amountAsText,
                payload = "${id.userId}$PAYLOAD_SEPARATOR${paymentAmount.amount}",
                providerToken = shopToken,
                startParameter = id.userId.toString(),
                currency = "RUB",
                providerData = """
                    {
                            "receipt" : {
                                "items" : [
                                    {
                                        "description" : "${paymentAmount.amountAsText}",
                                        "quantity" : 1,
                                        "amount" : {
                                            "value" : ${paymentAmount.price.toDouble()},
                                            "currency" : "RUB"
                                        },
                                        "vat_code" : 1,
                                        "payment_mode" : "full_payment",
                                        "payment_subject" : "commodity"
                                    }
                                ]
                            }
                    }
                """.trimIndent(),
                prices = listOf(
                    LabeledPrice(
                        label = "К оплате",
                        amount = paymentAmount.priceWithCents
                    )
                ),
                invoiceUserDetail = InvoiceUserDetail(
                    needEmail = true,
                    sendEmailToProvider = true
                )
            )
        ).onError { e ->
            log.error { "Where is error at invoice preparement $e" }
        }.onSuccess { log.info { "Invoice successfully sent" } }
    }

    data class PaymentInfo(val id: Long, val Amount: Int)

    fun extractPaymentInfo(invoicePayload: String) =
        invoicePayload.split(PAYLOAD_SEPARATOR).let {
            PaymentInfo(it.first().toLong(), it.last().toInt())
        }
}
