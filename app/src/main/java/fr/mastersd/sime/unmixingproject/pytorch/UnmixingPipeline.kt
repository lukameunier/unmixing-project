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
    fun processChunkedAudio(
        chunks: Flow<PcmChunk>
    ): Flow<UnmixingProgress> = flow {
        check(modelRunner.isInitialized) { "Model not initialized" }

        val vocalsResult = mutableListOf<Float>()
        val instrumentalResult = mutableListOf<Float>()
        var chunkIndex = 0

        chunks.collect { chunk ->
            val stereo = AudioPreprocessor.toStereoChannels(chunk.floats, interleaved = true)
            val modelChunks = AudioPreprocessor.buildChunks(stereo)

            modelChunks.forEach { (chunkData, actualLen) ->
                val (outputData, outputShape) = modelRunner.runInference(chunkData)
                val samplesToTake = minOf(actualLen, outputShape[3].toInt())
                val (vocals, instrumental) = StemPostprocessor.extractStems(
                    outputData, outputShape, samplesToTake
                )
                vocalsResult.addAll(vocals)
                instrumentalResult.addAll(instrumental)
            }

            chunkIndex++
            emit(UnmixingProgress.Progress(chunkIndex.toFloat() / (chunkIndex + 1).toFloat()))
            Log.d(TAG, "Chunk $chunkIndex processed, isFinal=${chunk.isFinal}")
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