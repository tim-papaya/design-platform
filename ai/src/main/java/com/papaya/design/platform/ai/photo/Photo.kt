package com.papaya.design.platform.ai.photo

data class Photo(
    val fileId: String,
    val fileUniqueId: String,
    val width: Int = 0,
    val height: Int = 0,
) {
    enum class Orientation {
        HORIZONTAL, VERTICAL, SQUARE
    }

    val imageOrientation: Orientation
        get() = when {
            width.toDouble() / height.toDouble() >= 1.29 -> Orientation.HORIZONTAL
            height.toDouble() / width.toDouble() >= 1.29 -> Orientation.VERTICAL
            else -> Orientation.SQUARE
        }
}
