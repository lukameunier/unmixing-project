package fr.mastersd.sime.unmixingproject.pytorch

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnmixingPipeline @Inject constructor(
    private val modelRunner: PyTorchModelRunner,
    private val context: Context
) : AutoCloseable {

    companion object {
        private const val TAG = "UnmixingPipeline"
    }

    val isInitialized: Boolean get() = modelRunner.isInitialized

    fun initialize(modelFileName: String = "model.ptl") {
        modelRunner.initialize(modelFileName)
    }

    fun processChunkedAudio(
        chunks: Flow<PcmChunk>
    ): Flow<UnmixingProgress> = flow {
        check(modelRunner.isInitialized) { "Model not initialized" }

        val trackId = UUID.randomUUID().toString()
        val vocalsFile = File(context.filesDir, "vocals_$trackId.wav")
        val instrumentalFile = File(context.filesDir, "instrumental_$trackId.wav")

        var sampleRate = AudioPreprocessor.SAMPLE_RATE
        var chunkIndex = 0

        WavWriter(vocalsFile, sampleRate).use { vocalsWriter ->
            WavWriter(instrumentalFile, sampleRate).use { instrumentalWriter ->
                chunks.collect { chunk ->
                    sampleRate = chunk.sampleRate

                    val stereo = AudioPreprocessor.toStereoChannels(chunk.floats, interleaved = true)
                    val modelChunks = AudioPreprocessor.buildChunks(stereo)

                    modelChunks.forEach { (chunkData, actualLen) ->
                        val (outputData, outputShape) = modelRunner.runInference(chunkData)
                        val samplesToTake = minOf(actualLen, outputShape[3].toInt())
                        val (vocals, instrumental) = StemPostprocessor.extractStems(
                            outputData, outputShape, samplesToTake
                        )
                        vocalsWriter.write(vocals)
                        instrumentalWriter.write(instrumental)
                    }

                    chunkIndex++
                    emit(UnmixingProgress.Progress(chunkIndex.toFloat() / (chunkIndex + 1).toFloat()))
                    Log.d(TAG, "Chunk $chunkIndex done, isFinal=${chunk.isFinal}")
                }
            }
        }

        emit(
            UnmixingProgress.Done(
                UnmixingResult(
                    vocalsPath = vocalsFile.absolutePath,
                    instrumentalPath = instrumentalFile.absolutePath,
                    sampleRate = sampleRate
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