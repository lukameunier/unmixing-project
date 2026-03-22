package fr.mastersd.sime.unmixingproject.data

/**
 * Data class representing separated audio tracks from the unmixing model.
 * Contains the vocal and instrumental tracks as FloatArrays.
 */
data class SeparatedTrack(
    val id: String,
    val originalTitle: String,
    val vocalPath: String,
    val instrumentalPath: String,
    val sampleRate: Int = 44100,
    val processedAt: Long = System.currentTimeMillis()
)
