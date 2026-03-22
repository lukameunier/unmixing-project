package fr.mastersd.sime.unmixingproject.pytorch

data class PcmChunk(
    val floats: FloatArray,
    val sampleRate: Int,
    val isFinal: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PcmChunk

        if (sampleRate != other.sampleRate) return false
        if (isFinal != other.isFinal) return false
        if (!floats.contentEquals(other.floats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sampleRate
        result = 31 * result + isFinal.hashCode()
        result = 31 * result + floats.contentHashCode()
        return result
    }
}