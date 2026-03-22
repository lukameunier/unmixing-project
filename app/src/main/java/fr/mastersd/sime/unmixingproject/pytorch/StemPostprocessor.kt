package fr.mastersd.sime.unmixingproject.pytorch

object StemPostprocessor {

    fun extractStems(
        outputData: FloatArray,
        outputShape: LongArray,
        samplesToTake: Int
    ): Pair<FloatArray, FloatArray> {
        val nbStems = outputShape[1].toInt()
        val nbCh = outputShape[2].toInt()
        val nbSamples = outputShape[3].toInt()

        val vocals = FloatArray(samplesToTake)
        val instrumental = FloatArray(samplesToTake)

        for (s in 0 until samplesToTake) {
            val vocalL = outputData[offset(0, 0, s, nbCh, nbSamples)]
            val vocalR = outputData[offset(0, 1, s, nbCh, nbSamples)]
            vocals[s] = (vocalL + vocalR) / 2f

            var instrSum = 0f
            for (stem in 1 until nbStems) {
                val l = outputData[offset(stem, 0, s, nbCh, nbSamples)]
                val r = outputData[offset(stem, 1, s, nbCh, nbSamples)]
                instrSum += (l + r) / 2f
            }
            instrumental[s] = instrSum / (nbStems - 1)
        }

        return Pair(vocals, instrumental)
    }

    private fun offset(stem: Int, ch: Int, sample: Int, nbCh: Int, nbSamples: Int): Int {
        return stem * nbCh * nbSamples + ch * nbSamples + sample
    }
}