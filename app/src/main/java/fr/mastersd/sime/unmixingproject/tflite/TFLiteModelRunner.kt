package fr.mastersd.sime.unmixingproject.tflite

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of the unmixing model containing vocals and instrumental tracks.
 */
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

@Singleton
class TFLiteModelRunner @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AutoCloseable {

    companion object {
        private const val TAG = "TFLiteModelRunner"
        // These values should match your model's expected input/output dimensions
        // Adjust based on your specific model
        private const val INPUT_SIZE = 150  // 600 bytes / 4 bytes per float = 150 floats
        private const val OUTPUT_SIZE = 300 // Model outputs 2x input (vocals + instrumental interleaved or concatenated)
    }

    private var compiledModel: CompiledModel? = null

    val isInitialized: Boolean get() = compiledModel != null

    fun initialize(modelFileName: String = "model_quantized.tflite") {
        if (compiledModel != null) return

        compiledModel = CompiledModel.create(
            assetManager = context.assets,
            assetName = modelFileName,
            options = CompiledModel.Options(accelerators = setOf(Accelerator.CPU))
        )
        Log.d(TAG, "Model initialized successfully")
    }

    /**
     * Run the model on a single chunk of audio data.
     * Input must be exactly INPUT_SIZE floats.
     */
    fun runChunk(inputData: FloatArray): FloatArray {
        val model = compiledModel
            ?: error("Model not initialized. Call initialize() first.")

        require(inputData.size == INPUT_SIZE) {
            "Input data size must be $INPUT_SIZE, got ${inputData.size}"
        }

        val inputBuffers = model.createInputBuffers()
        val outputBuffers = model.createOutputBuffers()

        inputBuffers[0].writeFloat(inputData)
        model.run(inputBuffers, outputBuffers)

        return outputBuffers[0].readFloat()
    }

    /**
     * Process full audio and return separated vocals and instrumental.
     * This handles chunking the audio into segments the model can process.
     */
    fun processFullAudio(audioData: FloatArray, onProgress: (Float) -> Unit = {}): UnmixingResult {
        val model = compiledModel
            ?: error("Model not initialized. Call initialize() first.")

        Log.d(TAG, "Processing audio of size: ${audioData.size}")

        // Calculate number of chunks
        val numChunks = (audioData.size + INPUT_SIZE - 1) / INPUT_SIZE
        Log.d(TAG, "Will process $numChunks chunks")

        val vocalsResult = mutableListOf<Float>()
        val instrumentalResult = mutableListOf<Float>()

        for (i in 0 until numChunks) {
            val startIdx = i * INPUT_SIZE
            val endIdx = minOf(startIdx + INPUT_SIZE, audioData.size)

            // Create padded input chunk
            val chunk = FloatArray(INPUT_SIZE)
            for (j in startIdx until endIdx) {
                chunk[j - startIdx] = audioData[j]
            }
            // Remaining values are already 0 (zero-padding)

            // Process chunk
            val inputBuffers = model.createInputBuffers()
            val outputBuffers = model.createOutputBuffers()

            inputBuffers[0].writeFloat(chunk)
            model.run(inputBuffers, outputBuffers)

            val output = outputBuffers[0].readFloat()

            // Parse output: assuming first half is vocals, second half is instrumental
            val halfSize = output.size / 2
            val actualSamplesToTake = minOf(halfSize, endIdx - startIdx)

            for (j in 0 until actualSamplesToTake) {
                vocalsResult.add(output[j])
                instrumentalResult.add(output[halfSize + j])
            }

            // Report progress
            onProgress((i + 1).toFloat() / numChunks)
        }

        Log.d(TAG, "Processing complete. Vocals: ${vocalsResult.size}, Instrumental: ${instrumentalResult.size}")

        return UnmixingResult(
            vocals = vocalsResult.toFloatArray(),
            instrumental = instrumentalResult.toFloatArray()
        )
    }

    override fun close() {
        compiledModel?.close()
        compiledModel = null
    }
}