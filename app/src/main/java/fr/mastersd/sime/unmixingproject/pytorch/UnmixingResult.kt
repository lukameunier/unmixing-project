package fr.mastersd.sime.unmixingproject.pytorch

data class UnmixingResult(
    val vocals: FloatArray,
    val instrumental: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UnmixingResult
        return vocals.contentEquals(other.vocals) && instrumental.contentEquals(other.instrumental)
    }
    override fun hashCode(): Int = 31 * vocals.contentHashCode() + instrumental.contentHashCode()
}