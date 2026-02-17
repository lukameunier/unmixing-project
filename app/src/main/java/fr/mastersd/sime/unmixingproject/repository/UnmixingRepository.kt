package fr.mastersd.sime.unmixingproject.repository

import android.net.Uri
import fr.mastersd.sime.unmixingproject.data.ProcessingState
import fr.mastersd.sime.unmixingproject.data.StemType
import fr.mastersd.sime.unmixingproject.data.UnmixedTrack
import fr.mastersd.sime.unmixingproject.tflite.TFLiteModelRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnmixingRepository @Inject constructor(
    private val modelRunner: TFLiteModelRunner
) {

    fun unmix(songId: String, inputData: FloatArray): Flow<ProcessingState> = flow {
        emit(ProcessingState.Loading(0f))

        modelRunner.initialize()

        emit(ProcessingState.Loading(0.5f))

        val outputData = modelRunner.run(inputData)

        // TODO: découper outputData en stems selon le format de ton modèle
        val stems = mapOutputToStems(outputData)

        emit(ProcessingState.Loading(1f))

        emit(
            ProcessingState.Success(
                UnmixedTrack(
                    songId = songId,
                    stems = stems,
                    processedAt = System.currentTimeMillis()
                )
            )
        )
    }.flowOn(Dispatchers.Default)
        .catch { e -> emit(ProcessingState.Error(e.message ?: "Unknown error")) }

    private fun mapOutputToStems(outputData: FloatArray): Map<StemType, Uri> {
        return mapOf(
            StemType.VOCALS to Uri.EMPTY,
            StemType.DRUMS  to Uri.EMPTY,
            StemType.BASS   to Uri.EMPTY,
            StemType.OTHER  to Uri.EMPTY
        )
    }
}