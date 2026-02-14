package fr.mastersd.sime.unmixingproject.data

import android.net.Uri

data class Song(
    val id: String,
    val title: String,
    val uri: Uri,
    val duration: Long,
    val waveformData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (duration != other.duration) return false
        if (id != other.id) return false
        if (title != other.title) return false
        if (uri != other.uri) return false
        if (!waveformData.contentEquals(other.waveformData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = duration.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + (waveformData?.contentHashCode() ?: 0)
        return result
    }
}