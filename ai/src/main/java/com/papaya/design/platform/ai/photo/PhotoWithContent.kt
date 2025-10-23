package com.papaya.design.platform.ai.photo

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
}