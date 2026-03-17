package fr.mastersd.sime.unmixingproject.pytorch

object StemPostprocessor {

    /**
     * Extrait vocals et instrumental depuis le FloatArray de sortie du modèle.
     * Shape attendue : (1, 4, 2, nbSamples) — 4 stems stéréo.
     * Stem 0 = vocals, stems 1-3 = drums/bass/other → mélangés en instrumental.
     */
    fun extractStems(
        outputData: FloatArray,
        outputShape: LongArray,
        samplesToTake: Int
    ): Pair<List<Float>, List<Float>> {
        val nbStems = outputShape[1].toInt()
        val nbCh = outputShape[2].toInt()
        val nbSamples = outputShape[3].toInt()

        val vocals = mutableListOf<Float>()
        val instrumental = mutableListOf<Float>()

        for (s in 0 until samplesToTake) {
            val vocalL = outputData[offset(0, 0, s, nbCh, nbSamples)]
            val vocalR = outputData[offset(0, 1, s, nbCh, nbSamples)]
            vocals.add((vocalL + vocalR) / 2f)

            var instrSum = 0f
            for (stem in 1 until nbStems) {
                val l = outputData[offset(stem, 0, s, nbCh, nbSamples)]
                val r = outputData[offset(stem, 1, s, nbCh, nbSamples)]
                instrSum += (l + r) / 2f
            }
            instrumental.add(instrSum / (nbStems - 1))
        }

        return Pair(vocals, instrumental)
    }

    private fun offset(stem: Int, ch: Int, sample: Int, nbCh: Int, nbSamples: Int): Int {
        return stem * nbCh * nbSamples + ch * nbSamples + sample
    }
}