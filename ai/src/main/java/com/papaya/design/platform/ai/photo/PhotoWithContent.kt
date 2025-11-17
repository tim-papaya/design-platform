package com.papaya.design.platform.ai.photo

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.MemoryCacheImageInputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

data class PhotoWithContent(
    val
    currentPhoto: Photo,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoWithContent

        if (currentPhoto != other.currentPhoto) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentPhoto.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

    fun cropToCenter(size1: Int, size2: Int): PhotoWithContent {
        val src = ImageIO.read(ByteArrayInputStream(bytes)) ?: return this
        val (targetW, targetH) = if (src.width >= src.height) size1 to size2 else size2 to size1
        val targetRatio = targetW.toDouble() / targetH
        val srcRatio = src.width.toDouble() / src.height

        val cropW: Int
        val cropH: Int
        if (srcRatio > targetRatio) {
            cropH = src.height
            cropW = (src.height * targetRatio).roundToInt()
        } else {
            cropW = src.width
            cropH = (src.width / targetRatio).roundToInt()
        }

        val x = (src.width - cropW) / 2
        val y = (src.height - cropH) / 2
        val cropped = src.getSubimage(x, y, cropW, cropH)

        val type = if (cropped.colorModel.hasAlpha()) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        val outImg = BufferedImage(targetW, targetH, type)
        val g = outImg.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(cropped, 0, 0, targetW, targetH, null)
        g.dispose()

        val format = detectFormat(bytes) ?: if (type == BufferedImage.TYPE_INT_ARGB) "png" else "jpeg"
        val baos = ByteArrayOutputStream()
        ImageIO.write(outImg, format, baos)
        return PhotoWithContent(
            Photo(this.currentPhoto.fileId, this.currentPhoto.fileUniqueId, targetW, targetH),
            bytes = baos.toByteArray()
        )
    }

    private fun detectFormat(input: ByteArray): String? {
        MemoryCacheImageInputStream(ByteArrayInputStream(input)).use { iis ->
            val readers: Iterator<ImageReader> = ImageIO.getImageReaders(iis)
            if (readers.hasNext()) {
                val r = readers.next()
                r.input = iis
                return r.formatName.lowercase()
            }
        }
        return null
    }

    fun upscaleIfSmaller(size1: Int, size2: Int): PhotoWithContent {
        val src = ImageIO.read(ByteArrayInputStream(bytes)) ?: return this
        val (minW, minH) = if (src.width >= src.height) size1 to size2 else size2 to size1

        val needUpscale = src.width < minW || src.height < minH
        if (!needUpscale) return this

        val scale = max(minW.toDouble() / src.width, minH.toDouble() / src.height)
        val newW = ceil(src.width * scale).toInt()
        val newH = ceil(src.height * scale).toInt()

        val type = if (src.colorModel.hasAlpha()) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        val dst = BufferedImage(newW, newH, type)
        val g = dst.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(src, 0, 0, newW, newH, null)
        g.dispose()

        val format = detectFormat(bytes) ?: if (type == BufferedImage.TYPE_INT_ARGB) "png" else "jpeg"
        val baos = ByteArrayOutputStream()
        ImageIO.write(dst, format, baos)
        return copy(bytes = baos.toByteArray())
    }
}