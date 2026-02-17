package fr.mastersd.sime.unmixingproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val uri: String,
    val duration: Long,
    val waveformData: ByteArray? = null,
    val cachedFilePath: String? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (uri != other.uri) return false
        if (duration != other.duration) return false
        if (!waveformData.contentEquals(other.waveformData)) return false
        if (cachedFilePath != other.cachedFilePath) return false
        if (addedAt != other.addedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (waveformData?.contentHashCode() ?: 0)
        result = 31 * result + (cachedFilePath?.hashCode() ?: 0)
        result = 31 * result + addedAt.hashCode()
        return result
    }
}

