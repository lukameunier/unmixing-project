package fr.mastersd.sime.unmixingproject.data

/**
 * Data class representing separated audio tracks from the unmixing model.
 * Contains the vocal and instrumental tracks as FloatArrays.
 */
data class SeparatedTrack(
    val id: String,
    val originalTitle: String,
    val vocalData: FloatArray,
    val instrumentalData: FloatArray,
    val sampleRate: Int = 44100,
    val processedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeparatedTrack

        if (id != other.id) return false
        if (originalTitle != other.originalTitle) return false
        if (!vocalData.contentEquals(other.vocalData)) return false
        if (!instrumentalData.contentEquals(other.instrumentalData)) return false
        if (sampleRate != other.sampleRate) return false
        if (processedAt != other.processedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + originalTitle.hashCode()
        result = 31 * result + vocalData.contentHashCode()
        result = 31 * result + instrumentalData.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + processedAt.hashCode()
        return result
    }
}

