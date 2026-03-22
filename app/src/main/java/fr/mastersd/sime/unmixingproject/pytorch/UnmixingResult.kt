package fr.mastersd.sime.unmixingproject.pytorch

data class UnmixingResult(
    val vocalsPath: String,
    val instrumentalPath: String,
    val sampleRate: Int
)