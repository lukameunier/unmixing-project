package fr.mastersd.sime.unmixingproject.repository

import android.util.Log
import fr.mastersd.sime.unmixingproject.data.ProcessingState
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.pytorch.PcmChunk
import fr.mastersd.sime.unmixingproject.pytorch.UnmixingPipeline
import fr.mastersd.sime.unmixingproject.pytorch.UnmixingProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnmixingRepository @Inject constructor(
    private val pipeline: UnmixingPipeline,
    private val separatedTrackRepository: SeparatedTrackRepository
) {

    companion object {
        private const val TAG = "UnmixingRepository"
    }

    fun unmixChunked(
        title: String,
        originalPath: String,
        duration: Long,
        chunks: Flow<PcmChunk>
    ): Flow<ProcessingState> = flow {
        emit(ProcessingState.Loading(0f))
        pipeline.initialize()
        emit(ProcessingState.Loading(0.1f))

        pipeline.processChunkedAudio(chunks).collect { update ->
            when (update) {
                is UnmixingProgress.Progress -> {
                    emit(ProcessingState.Loading(0.1f + update.value * 0.8f))
                }
                is UnmixingProgress.Done -> {
                    val separatedTrack = SeparatedTrack(
                        id = UUID.randomUUID().toString(),
                        originalTitle = title,
                        vocalPath = update.result.vocalsPath,
                        instrumentalPath = update.result.instrumentalPath,
                        originalPath = originalPath,
                        sampleRate = update.result.sampleRate,
                        processedAt = System.currentTimeMillis()
                    )
                    separatedTrackRepository.saveTrack(separatedTrack)
                    emit(ProcessingState.Success(separatedTrack))
                }
            }
        }
    }.flowOn(Dispatchers.Default)
        .catch { e ->
            Log.e(TAG, "Error during unmixing", e)
            emit(ProcessingState.Error(e.message ?: "Unknown error"))
        }
}