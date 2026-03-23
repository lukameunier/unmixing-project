package fr.mastersd.sime.unmixingproject.viewmodels

import android.content.Context
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
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

                val originalFile = File(context.filesDir, "original_${System.currentTimeMillis()}.audio")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    originalFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val chunks = AudioDecoder.decodeToChunks(context, uri)

                unmixingRepository.unmixChunked(
                    title,
                    originalFile.absolutePath,
                    duration,
                    chunks
                ).collect { state ->
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearProcessSuccess() {
        _uiState.value = _uiState.value.copy(processSuccess = false)
    }
}