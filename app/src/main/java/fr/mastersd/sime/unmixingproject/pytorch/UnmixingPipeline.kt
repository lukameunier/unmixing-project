package fr.mastersd.sime.unmixingproject.pytorch

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnmixingPipeline @Inject constructor(
    private val modelRunner: PyTorchModelRunner
) : AutoCloseable {

    companion object {
        private const val TAG = "UnmixingPipeline"
    }

    val isInitialized: Boolean get() = modelRunner.isInitialized

    fun initialize(modelFileName: String = "model.ptl") {
        modelRunner.initialize(modelFileName)
    }

    /**
     * Traite l'audio et émet le progress (0f..1f) puis le résultat final.
     * Utiliser un Flow permet d'émettre depuis UnmixingRepository sans problème suspend.
     */
    fun processFullAudio(
        audioData: FloatArray,
        isInterleaved: Boolean = true
    ): Flow<UnmixingProgress> = flow {
        check(modelRunner.isInitialized) { "Model not initialized. Call initialize() first." }

        val stereo = AudioPreprocessor.toStereoChannels(audioData, isInterleaved)
        val chunks = AudioPreprocessor.buildChunks(stereo)

        val vocalsResult = mutableListOf<Float>()
        val instrumentalResult = mutableListOf<Float>()

        chunks.forEachIndexed { index, (chunkData, actualLen) ->
            val (outputData, outputShape) = modelRunner.runInference(chunkData)
            val samplesToTake = minOf(actualLen, outputShape[3].toInt())
            val (vocals, instrumental) = StemPostprocessor.extractStems(
                outputData, outputShape, samplesToTake
            )

            vocalsResult.addAll(vocals)
            instrumentalResult.addAll(instrumental)

            val progress = (index + 1).toFloat() / chunks.size
            emit(UnmixingProgress.Progress(progress))
            Log.d(TAG, "Chunk ${index + 1}/${chunks.size} processed")
        }

        emit(
            UnmixingProgress.Done(
                UnmixingResult(
                    vocals = vocalsResult.toFloatArray(),
                    instrumental = instrumentalResult.toFloatArray()
                )
            )
        )
    }

    override fun close() = modelRunner.close()
}

sealed class UnmixingProgress {
    data class Progress(val value: Float) : UnmixingProgress()
    data class Done(val result: UnmixingResult) : UnmixingProgress()
}