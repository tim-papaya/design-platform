package com.papaya.design.platform.bot.image.bot.payment.robokassa

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.papaya.design.platform.bot.image.bot.message.TelegramId
import com.papaya.design.platform.bot.image.bot.payment.PaymentAmount
import com.papaya.design.platform.bot.image.bot.payment.PaymentInfo
import com.papaya.design.platform.bot.image.bot.payment.PaymentProvider
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.SortedMap
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicLong

private val log = KotlinLogging.logger { }

@Service
@Lazy
@Profile("robokassa")
class RobokassaPaymentProvider(
    private val bot: Bot,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.login}")
    private val merchantLogin: String,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.password1}")
    private val password1: String,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.payment-url:https://auth.robokassa.ru/Merchant/Index.aspx}")
    private val paymentUrl: String,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.is-test:0}")
    private val isTest: Int,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.result-url:}")
    private val resultUrl: String,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.success-url:}")
    private val successUrl: String,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.fail-url:}")
    private val failUrl: String,
) : PaymentProvider {

    private val invIdSequence = AtomicLong(System.currentTimeMillis())
    private val http = OkHttpClient()

    override fun sendInvoice(id: TelegramId, paymentAmount: PaymentAmount) {
        val invId = invIdSequence.incrementAndGet()
        val outSum = paymentAmount.priceAsSum()
        val shpParams = TreeMap<String, String>().apply {
            put("Shp_user", id.userId.toString())
            put("Shp_amount", paymentAmount.amount.toString())
        }
        val signature = RobokassaSignature.buildSignature(
            merchantLogin = merchantLogin,
            outSum = outSum,
            invId = invId,
            password = password1,
            shpParams = shpParams
        )

        val servicePaymentLink = UriComponentsBuilder.fromUriString(paymentUrl)
            .queryParam("MerchantLogin", merchantLogin)
            .queryParam("OutSum", outSum)
            .queryParam("InvId", invId)
            .queryParam("Description", paymentAmount.amountAsText)
            .queryParam("SignatureValue", signature)
            .queryParam("IsTest", isTest)
            .apply {
                if (resultUrl.isNotBlank()) queryParam("ResultUrl", resultUrl)
                if (successUrl.isNotBlank()) queryParam("SuccessUrl", successUrl)
                if (failUrl.isNotBlank()) queryParam("FailUrl", failUrl)
                shpParams.forEach { (k, v) -> queryParam(k, v) }
            }
            .build()
            .toUriString()
        log.info { "Prepared for ${id.userId} payment link $servicePaymentLink" }

        val servicePaymentRequest = Request.Builder()
            .url(servicePaymentLink)
            .get()
            .build()

        http.newCall(servicePaymentRequest).execute().use { resp ->
            val finalUrl = resp.request().url().toString()
            log.info {"Prepared for ${id.userId} redirect payment link $finalUrl"}
            bot.sendMessage(
                chatId = ChatId.fromId(id.chatId),
                text = "Оплата: ${paymentAmount.amountAsText}\nСсылка: $finalUrl"
            ).onError { e ->
                log.error { "Error sending Robokassa payment link: $e" }
            }
        }
    }

    override fun extractPaymentInfo(invoicePayload: String): PaymentInfo {
        throw UnsupportedOperationException("Telegram payload parsing is not supported in Robokassa profile")
    }
}

private fun PaymentAmount.priceAsSum(): String =
    BigDecimal(price).setScale(2, RoundingMode.UNNECESSARY).let {
        String.format(Locale.US, "%.2f", it)
    }

object RobokassaSignature {
    fun buildSignature(
        merchantLogin: String,
        outSum: String,
        invId: Long,
        password: String,
        shpParams: SortedMap<String, String> = sortedMapOf()
    ): String {
        val base = buildString {
            append(merchantLogin)
            append(':')
            append(outSum)
            append(':')
            append(invId)
            append(':')
            append(password)
            if (shpParams.isNotEmpty()) {
                shpParams.forEach { (k, v) ->
                    append(':')
                    append(k)
                    append('=')
                    append(v)
                }
            }
        }
        return RobokassaSignature.md5(base)
    }

    fun buildResultSignature(
        outSum: String,
        invId: Long,
        password: String,
        shpParams: SortedMap<String, String> = sortedMapOf()
    ): String {
        val base = buildString {
            append(outSum)
            append(':')
            append(invId)
            append(':')
            append(password)
            if (shpParams.isNotEmpty()) {
                shpParams.forEach { (k, v) ->
                    append(':')
                    append(k)
                    append('=')
                    append(v)
                }
            }
        }
        return md5(base)
    }

    fun md5(input: String): String =
        java.security.MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
