package com.papaya.design.platform.ai

interface AudioAdditionService {
    fun addAudioToVideo(videoBytes: ByteArray): ByteArray
}
