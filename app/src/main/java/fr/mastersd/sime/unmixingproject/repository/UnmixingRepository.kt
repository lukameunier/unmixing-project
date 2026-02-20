package fr.mastersd.sime.unmixingproject.repository

import android.util.Log
import fr.mastersd.sime.unmixingproject.data.AudioBuffer
import fr.mastersd.sime.unmixingproject.data.ProcessingState
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.tflite.TFLiteModelRunner
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
    private val modelRunner: TFLiteModelRunner,
    private val separatedTrackRepository: SeparatedTrackRepository
) {

    companion object {
        private const val TAG = "UnmixingRepository"
    }

    /**
     * Process audio buffer and separate vocals from instrumental.
     * The model outputs two tracks: vocals and instrumental.
     */
    fun unmix(audioBuffer: AudioBuffer): Flow<ProcessingState> = flow {
        emit(ProcessingState.Loading(0f))

        Log.d(TAG, "Starting unmixing for: ${audioBuffer.title}")
        Log.d(TAG, "Audio data size: ${audioBuffer.audioData.size}")

        // Initialize the model
        modelRunner.initialize()
        emit(ProcessingState.Loading(0.1f))

        // Process the full audio with chunking and get vocals/instrumental
        var lastProgress = 0.1f
        val result = modelRunner.processFullAudio(audioBuffer.audioData) { progress ->
            // Map model progress (0-1) to our progress range (0.1-0.9)
            lastProgress = 0.1f + progress * 0.8f
        }
        emit(ProcessingState.Loading(0.9f))

        Log.d(TAG, "Model processing complete. Vocals: ${result.vocals.size}, Instrumental: ${result.instrumental.size}")

        // Create the separated track object
        val separatedTrack = SeparatedTrack(
            id = UUID.randomUUID().toString(),
            originalTitle = audioBuffer.title,
            vocalData = result.vocals,
            instrumentalData = result.instrumental,
            sampleRate = audioBuffer.sampleRate,
            processedAt = System.currentTimeMillis()
        )

        // Save the separated tracks to the database
        separatedTrackRepository.saveTrack(separatedTrack)
        emit(ProcessingState.Loading(1f))

        Log.d(TAG, "Track saved to database: ${separatedTrack.id}")

        emit(ProcessingState.Success(separatedTrack))
    }.flowOn(Dispatchers.Default)
        .catch { e ->
            Log.e(TAG, "Error during unmixing", e)
            emit(ProcessingState.Error(e.message ?: "Unknown error"))
        }
}