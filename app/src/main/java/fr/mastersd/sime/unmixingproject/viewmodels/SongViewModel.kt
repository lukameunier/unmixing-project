package fr.mastersd.sime.unmixingproject.viewmodels

import android.content.Context
import android.media.MediaCodec
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
import fr.mastersd.sime.unmixingproject.pytorch.AudioDecoder
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
                val title = extractTitle(uri)
                val duration = extractDuration(uri)

                val chunks = AudioDecoder.decodeToChunks(context, uri)

                unmixingRepository.unmixChunked(title, uri.toString(), duration, chunks).collect { state ->
                    when (state) {
                        is ProcessingState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                processingProgress = state.progress
                            )
                        }
                        is ProcessingState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                processSuccess = true,
                                processingProgress = 1f,
                                currentAudioBuffer = null
                            )
                            loadTracks()
                        }
                        is ProcessingState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = state.message,
                                currentAudioBuffer = null
                            )
                        }
                        is ProcessingState.Idle -> { }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur: ${e.message}",
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

        val (audioData, sampleRate) = readAudioData(uri) // on récupère les deux

        AudioBuffer(
            title = title,
            audioData = audioData,
            sampleRate = sampleRate,
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
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Read audio data from URI and convert to FloatArray.
     * Uses MediaExtractor to decode the audio file.
     */
    private fun readAudioData(uri: Uri): Pair<FloatArray, Int> {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        var audioTrackIndex = -1
        var inputFormat: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                inputFormat = format
                break
            }
        }

        if (audioTrackIndex == -1 || inputFormat == null) {
            extractor.release()
            throw IllegalArgumentException("No audio track found in file")
        }

        extractor.selectTrack(audioTrackIndex)

        val actualSampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) // ici, pas en classe

        val mime = inputFormat.getString(MediaFormat.KEY_MIME)!!
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(inputFormat, null, null, 0)
        codec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val pcmSamples = mutableListOf<Short>()
        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val inputBufferId = codec.dequeueInputBuffer(10_000L)
                if (inputBufferId >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferId)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(
                            inputBufferId, 0, 0,
                            0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(
                            inputBufferId, 0, sampleSize,
                            extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
            }

            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            when {
                outputBufferId >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)!!
                    outputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    val shortBuffer = outputBuffer.asShortBuffer()
                    while (shortBuffer.hasRemaining()) {
                        pcmSamples.add(shortBuffer.get())
                    }
                    codec.releaseOutputBuffer(outputBufferId, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> { }
                outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> { }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        val floatArray = FloatArray(pcmSamples.size) { i -> pcmSamples[i] / 32768f }
        return Pair(floatArray, actualSampleRate)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearProcessSuccess() {
        _uiState.value = _uiState.value.copy(processSuccess = false)
    }
}

