package com.papaya.design.platform.bot.image.bot.payment.robokassa

import com.papaya.design.platform.bot.image.bot.user.UserService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.SortedMap
import java.util.TreeMap

private val log = KotlinLogging.logger { }

@RestController
@Profile("robokassa")
class RobokassaController(
    private val userService: UserService,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.password1}")
    private val password1: String,
    @Value("\${com.papaya.design.platform.bot.image.robokassa.password2}")
    private val password2: String,
) {

    @RequestMapping(
        value = ["/robokassa/result"],
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun result(
        @RequestParam("OutSum") outSum: String,
        @RequestParam("InvId") invId: Long,
        @RequestParam("SignatureValue") signatureValue: String,
        @RequestParam params: Map<String, String>,
    ): ResponseEntity<String> {
        val shpParams = extractShpParams(params)
        val expected = RobokassaSignature.buildResultSignature(outSum, invId, password2, shpParams)
        if (!signatureValue.equals(expected, ignoreCase = true)) {
            log.warn { "Robokassa result signature mismatch invId=$invId outSum=$outSum" }
            return ResponseEntity.ok("bad sign")
        }

        val userId = shpParams["Shp_user"]?.toLongOrNull()
        val amount = shpParams["Shp_amount"]?.toIntOrNull()
        if (userId == null || amount == null) {
            log.warn { "Robokassa result missing Shp_user or Shp_amount invId=$invId" }
            return ResponseEntity.ok("bad sign")
        }

        userService.saveUser(userId) { u ->
            u.generations += amount
        }
        log.info { "Robokassa payment confirmed invId=$invId userId=$userId amount=$amount" }
        return ResponseEntity.ok("OK$invId")
    }

    @GetMapping("/robokassa/success", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun success(
        @RequestParam("OutSum") outSum: String,
        @RequestParam("InvId") invId: Long,
        @RequestParam("SignatureValue") signatureValue: String,
        @RequestParam params: Map<String, String>,
    ): ResponseEntity<String> {
        val shpParams = extractShpParams(params)
        val expected = RobokassaSignature.buildResultSignature(outSum, invId, password1, shpParams)
        if (!signatureValue.equals(expected, ignoreCase = true)) {
            log.warn { "Robokassa success signature mismatch invId=$invId outSum=$outSum" }
            return ResponseEntity.ok("bad sign")
        }
        return ResponseEntity.ok("Спасибо! Оплата подтверждена.")
    }

    private fun extractShpParams(params: Map<String, String>): SortedMap<String, String> =
        TreeMap<String, String>().apply {
            params.filterKeys { it.startsWith("Shp_") }
                .forEach { (k, v) -> put(k, v) }
        }
}
