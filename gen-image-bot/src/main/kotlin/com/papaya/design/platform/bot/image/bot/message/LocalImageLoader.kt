package com.papaya.design.platform.bot.image.bot.message

import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.readBytes

const val GEN_IMAGE_BOT_EXAMPLES = "~/gen-image-bot/examples/"

enum class LocalImage(val path: String) {
        REALISTIC_EXAMPLE_1("realistic_example.jpeg")
}

@Service
class ExamplesLocalImageLoader : LocalImageLoader(GEN_IMAGE_BOT_EXAMPLES)

open class LocalImageLoader(private val rootPath: String) {
    private val homeDirectory = System.getProperty("user.home")
    private val cache = ConcurrentHashMap<LocalImage, ByteArray>()

    fun loadImage(localImage: LocalImage): ByteArray =
        cache.getOrPut(localImage) {
            Path.of("$rootPath/${localImage.path}".replace("~", homeDirectory)).readBytes()
        }

}
