package com.papaya.design.platform.bot.image.bot.video

import org.mp4parser.IsoFile
import org.mp4parser.boxes.iso14496.part12.TrackBox
import java.io.ByteArrayInputStream
import java.nio.channels.Channels

data class VideoDimensions(val width: Int, val height: Int)

object VideoMetadataExtractor {
    fun extractDimensions(videoBytes: ByteArray): VideoDimensions? =
        runCatching {
            ByteArrayInputStream(videoBytes).use { input ->
                Channels.newChannel(input).use { channel ->
                    IsoFile(channel).use { isoFile ->
                        val videoTrack = isoFile.movieBox
                            ?.boxes
                            ?.filterIsInstance<TrackBox>()
                            ?.firstOrNull { it.mediaBox?.handlerBox?.handlerType == "vide" }
                            ?: return null

                        val trackHeader = videoTrack.trackHeaderBox
                        VideoDimensions(
                            width = trackHeader.width.toInt(),
                            height = trackHeader.height.toInt(),
                        )
                    }
                }
            }
        }.getOrNull()
}
