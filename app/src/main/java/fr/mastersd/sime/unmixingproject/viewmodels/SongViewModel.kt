package fr.mastersd.sime.unmixingproject.viewmodels

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.mastersd.sime.unmixingproject.data.AudioBuffer
import fr.mastersd.sime.unmixingproject.data.ProcessingState
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.repository.SeparatedTrackRepository
import fr.mastersd.sime.unmixingproject.repository.UnmixingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

data class SongUiState(
    val separatedTracks: List<SeparatedTrack> = emptyList(),
    val isLoading: Boolean = false,
    val processingProgress: Float = 0f,
    val error: String? = null,
    val processSuccess: Boolean = false,
    val currentAudioBuffer: AudioBuffer? = null
)

@HiltViewModel
class SongViewModel @Inject constructor(
    private val separatedTrackRepository: SeparatedTrackRepository,
    private val unmixingRepository: UnmixingRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongUiState())
    val uiState: StateFlow<SongUiState> = _uiState.asStateFlow()

    val separatedTracks: StateFlow<List<SeparatedTrack>> = separatedTrackRepository.getAllTracks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadTracks()
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val tracks = separatedTrackRepository.getAllTracksSuspend()
                _uiState.value = _uiState.value.copy(
                    separatedTracks = tracks,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Load audio file from URI into buffer and immediately process with the model.
     * The audio is NOT stored in a database - only the separated outputs are saved.
     */
    fun processAudioFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                processSuccess = false,
                processingProgress = 0f
            )
            try {
                // Step 1: Load audio into buffer
                val audioBuffer = loadAudioToBuffer(uri)
                _uiState.value = _uiState.value.copy(
                    currentAudioBuffer = audioBuffer,
                    processingProgress = 0.1f
                )

                // Step 2: Immediately process with the unmixing model
                unmixingRepository.unmix(audioBuffer).collect { state ->
                    when (state) {
                        is ProcessingState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                processingProgress = 0.1f + state.progress * 0.9f
                            )
                        }
                        is ProcessingState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                processSuccess = true,
                                processingProgress = 1f,
                                error = null,
                                currentAudioBuffer = null
                            )
                            loadTracks()
                        }
                        is ProcessingState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Erreur lors du traitement: ${state.message}",
                                processSuccess = false,
                                currentAudioBuffer = null
                            )
                        }
                        is ProcessingState.Idle -> { /* Ignore */ }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement: ${e.message}",
                    processSuccess = false,
                    currentAudioBuffer = null
                )
            }
        }
    }

    /**
     * Load audio file into memory buffer as FloatArray.
     */
    private suspend fun loadAudioToBuffer(uri: Uri): AudioBuffer = withContext(Dispatchers.IO) {
        val title = extractTitle(uri)
        val duration = extractDuration(uri)

        // Read audio data and convert to FloatArray
        val audioData = readAudioData(uri)

        AudioBuffer(
            title = title,
            audioData = audioData,
            sampleRate = 44100, // Default sample rate, can be extracted from file
            duration = duration
        )
    }

    /**
     * Extract audio title from URI.
     */
    private fun extractTitle(uri: Uri): String {
        var title = "Unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    title = cursor.getString(nameIndex) ?: "Unknown"
                    title = title.substringBeforeLast(".")
                }
            }
        }
        return title
    }

    /**
     * Extract audio duration from URI.
     */
    private fun extractDuration(uri: Uri): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Read audio data from URI and convert to FloatArray.
     * Uses MediaExtractor to decode the audio file.
     */
    private fun readAudioData(uri: Uri): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        // Find audio track
        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                break
            }
        }

        if (audioTrackIndex == -1) {
            extractor.release()
            throw IllegalArgumentException("No audio track found in file")
        }

        extractor.selectTrack(audioTrackIndex)
        val format = extractor.getTrackFormat(audioTrackIndex)

        // Get sample rate and channel count
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        // Read raw audio bytes
        val audioBytes = mutableListOf<Byte>()
        val buffer = ByteBuffer.allocate(1024 * 1024) // 1MB buffer

        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            buffer.position(0)
            buffer.limit(sampleSize)
            while (buffer.hasRemaining()) {
                audioBytes.add(buffer.get())
            }
            buffer.clear()
            extractor.advance()
        }

        extractor.release()

        // Convert bytes to float array (assuming 16-bit PCM)
        val byteArray = audioBytes.toByteArray()
        val floatArray = FloatArray(byteArray.size / 2)
        val byteBuffer = ByteBuffer.wrap(byteArray)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        for (i in floatArray.indices) {
            val sample = byteBuffer.short
            floatArray[i] = sample / 32768f // Normalize to [-1, 1]
        }

        return floatArray
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            try {
                separatedTrackRepository.deleteTrack(trackId)
                loadTracks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearProcessSuccess() {
        _uiState.value = _uiState.value.copy(processSuccess = false)
    }
}

