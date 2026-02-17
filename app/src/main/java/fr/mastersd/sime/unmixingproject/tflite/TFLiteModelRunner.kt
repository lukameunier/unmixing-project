package fr.mastersd.sime.unmixingproject.tflite

import android.content.Context
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteModelRunner @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AutoCloseable {

    private var compiledModel: CompiledModel? = null

    val isInitialized: Boolean get() = compiledModel != null

    fun initialize(modelFileName: String = "model_quantized.tflite") {
        if (compiledModel != null) return

        compiledModel = CompiledModel.create(
            assetManager = context.assets,
            assetName = modelFileName,
            options = CompiledModel.Options(accelerators = setOf(Accelerator.CPU))
        )
    }

    fun run(inputData: FloatArray): FloatArray {
        val model = compiledModel
            ?: error("Model not initialized. Call initialize() first.")

        val inputBuffers = model.createInputBuffers()
        val outputBuffers = model.createOutputBuffers()

        inputBuffers[0].writeFloat(inputData)
        model.run(inputBuffers, outputBuffers)

        return outputBuffers[0].readFloat()
    }

    override fun close() {
        compiledModel?.close()
        compiledModel = null
    }
}