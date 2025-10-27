package com.papaya.design.platform.bot.image.bot.message

import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.readBytes

const val GEN_IMAGE_BOT_EXAMPLES = "~/gen-image-bot/examples/"

enum class LocalFile(val path: String) {
    REALISTIC_EXAMPLE_1("realistic_example.jpeg"),
    RULES_OF_USE("rules_of_use.pdf"),
    CONFIDENTIAL_POLICY("confidential_policy.pdf")
}

@Service
class ExamplesLocalFileLoader : LocalImageLoader(GEN_IMAGE_BOT_EXAMPLES)

open class LocalImageLoader(private val rootPath: String) {
    private val homeDirectory = System.getProperty("user.home")
    private val cache = ConcurrentHashMap<LocalFile, ByteArray>()

    fun loadFile(localFile: LocalFile): ByteArray =
        cache.getOrPut(localFile) {
            Path.of("$rootPath/${localFile.path}".replace("~", homeDirectory)).readBytes()
        }

}
