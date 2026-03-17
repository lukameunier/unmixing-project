package fr.mastersd.sime.unmixingproject.pytorch

object AudioPreprocessor {

    const val SAMPLE_RATE = 44100
    const val SEGMENT_SECONDS = 5
    const val SEGMENT_SAMPLES = SAMPLE_RATE * SEGMENT_SECONDS // 220500
    const val NB_CHANNELS = 2

    /**
     * Convertit un FloatArray mono ou stéréo entrelacé en [left[], right[]].
     * Si mono (taille impaire), duplique le canal.
     */
    fun toStereoChannels(audio: FloatArray, interleaved: Boolean): Array<FloatArray> {
        return if (!interleaved) {
            val half = audio.size / 2
            arrayOf(audio.copyOfRange(0, half), audio.copyOfRange(half, audio.size))
        } else if (audio.size % 2 == 0) {
            val n = audio.size / 2
            val left = FloatArray(n)
            val right = FloatArray(n)
            for (i in 0 until n) {
                left[i] = audio[i * 2]
                right[i] = audio[i * 2 + 1]
            }
            arrayOf(left, right)
        } else {
            arrayOf(audio.copyOf(), audio.copyOf())
        }
    }

    /**
     * Découpe le signal stéréo en chunks de SEGMENT_SAMPLES avec zero-padding.
     * Retourne une liste de FloatArray au format [1, 2, SEGMENT_SAMPLES] row-major.
     */
    fun buildChunks(stereo: Array<FloatArray>): List<Pair<FloatArray, Int>> {
        val totalSamples = stereo[0].size
        val numChunks = (totalSamples + SEGMENT_SAMPLES - 1) / SEGMENT_SAMPLES
        val chunks = mutableListOf<Pair<FloatArray, Int>>()

        for (i in 0 until numChunks) {
            val start = i * SEGMENT_SAMPLES
            val end = minOf(start + SEGMENT_SAMPLES, totalSamples)
            val actualLen = end - start

            val chunkData = FloatArray(NB_CHANNELS * SEGMENT_SAMPLES)
            for (ch in 0 until NB_CHANNELS) {
                val chOffset = ch * SEGMENT_SAMPLES
                for (s in 0 until actualLen) {
                    chunkData[chOffset + s] = stereo[ch][start + s]
                }
            }
            chunks.add(Pair(chunkData, actualLen))
        }

        return chunks
    }
}