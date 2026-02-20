package fr.mastersd.sime.unmixingproject.data

/**
 * Data class representing audio data loaded into memory buffer.
 * This is used as an intermediate step before processing with the model.
 */
data class AudioBuffer(
    val title: String,
    val audioData: FloatArray,
    val sampleRate: Int = 44100,
    val duration: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioBuffer

        if (title != other.title) return false
        if (!audioData.contentEquals(other.audioData)) return false
        if (sampleRate != other.sampleRate) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + audioData.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + duration.hashCode()
        return result
    }
}

