package fr.mastersd.sime.unmixingproject.data

data class SeparatedTrack(
    val id: String,
    val originalTitle: String,
    val vocalPath: String,
    val instrumentalPath: String,
    val originalPath: String = "",
    val sampleRate: Int = 44100,
    val processedAt: Long = System.currentTimeMillis()
)