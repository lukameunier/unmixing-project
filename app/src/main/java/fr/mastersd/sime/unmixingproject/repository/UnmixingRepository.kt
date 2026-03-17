package fr.mastersd.sime.unmixingproject.repository

import android.util.Log
import fr.mastersd.sime.unmixingproject.data.AudioBuffer
import fr.mastersd.sime.unmixingproject.data.ProcessingState
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
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

    fun unmix(audioBuffer: AudioBuffer): Flow<ProcessingState> = flow {
        emit(ProcessingState.Loading(0f))

        Log.d(TAG, "Starting unmixing for: ${audioBuffer.title}")
        Log.d(TAG, "Audio data size: ${audioBuffer.audioData.size}")

        pipeline.initialize()
        emit(ProcessingState.Loading(0.1f))

        // Collecte le Flow de UnmixingPipeline — pas de problème suspend
        pipeline.processFullAudio(audioBuffer.audioData).collect { update ->
            when (update) {
                is UnmixingProgress.Progress -> {
                    emit(ProcessingState.Loading(0.1f + update.value * 0.8f))
                }
                is UnmixingProgress.Done -> {
                    val result = update.result
                    Log.d(TAG, "Processing complete. Vocals: ${result.vocals.size}, Instrumental: ${result.instrumental.size}")

                    val separatedTrack = SeparatedTrack(
                        id = UUID.randomUUID().toString(),
                        originalTitle = audioBuffer.title,
                        vocalData = result.vocals,
                        instrumentalData = result.instrumental,
                        sampleRate = audioBuffer.sampleRate,
                        processedAt = System.currentTimeMillis()
                    )

                    separatedTrackRepository.saveTrack(separatedTrack)
                    emit(ProcessingState.Loading(1f))
                    Log.d(TAG, "Track saved: ${separatedTrack.id}")
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