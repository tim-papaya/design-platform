package com.papaya.design.platform.bot.image.bot.image

import java.net.URL

fun downloadImageAsBytes(url: String): ByteArray {
    return URL(url).readBytes()
}
