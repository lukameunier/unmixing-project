package fr.mastersd.sime.unmixingproject.pytorch

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PyTorchModelRunner @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AutoCloseable {

    companion object {
        private const val TAG = "PyTorchModelRunner"
    }

    private var module: Module? = null

    val isInitialized: Boolean get() = module != null

    fun initialize(modelFileName: String = "model.ptl") {
        if (module != null) return
        module = LiteModuleLoader.load(assetFilePath(modelFileName))
        Log.d(TAG, "Model initialized: $modelFileName")
    }

    /**
     * Exécute le modèle sur un chunk au format (1, 2, SEGMENT_SAMPLES).
     * Retourne le FloatArray brut et la shape de sortie.
     */
    fun runInference(chunkData: FloatArray): Pair<FloatArray, LongArray> {
        val mod = module ?: error("Model not initialized. Call initialize() first.")

        val inputTensor = Tensor.fromBlob(
            chunkData,
            longArrayOf(
                1L,
                AudioPreprocessor.NB_CHANNELS.toLong(),
                AudioPreprocessor.SEGMENT_SAMPLES.toLong()
            )
        )

        val output = mod.forward(IValue.from(inputTensor)).toTensor()
        return Pair(output.dataAsFloatArray, output.shape())
    }

    private fun assetFilePath(assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    override fun close() {
        module?.destroy()
        module = null
    }
}