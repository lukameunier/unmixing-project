package fr.mastersd.sime.unmixingproject.viewmodels

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.repository.SeparatedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MusicUiState(
    val track: SeparatedTrack? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isVocalsPlaying: Boolean = false,
    val isInstrumentalPlaying: Boolean = false
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val separatedTrackRepository: SeparatedTrackRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var vocalsPlayer: ExoPlayer? = null
    private var instrumentalPlayer: ExoPlayer? = null

    fun loadTrack(trackId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val track = separatedTrackRepository.getTrackById(trackId)
                _uiState.value = _uiState.value.copy(track = track, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun toggleVocals() {
        val track = _uiState.value.track ?: return
        val isPlaying = _uiState.value.isVocalsPlaying

        if (isPlaying) {
            vocalsPlayer?.pause()
            _uiState.value = _uiState.value.copy(isVocalsPlaying = false)
        } else {
            stopInstrumental()
            if (vocalsPlayer == null) {
                vocalsPlayer = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(File(track.vocalPath).toUri()))
                    prepare()
                }
            }
            vocalsPlayer?.play()
            _uiState.value = _uiState.value.copy(isVocalsPlaying = true)
        }
    }

    fun toggleInstrumental() {
        val track = _uiState.value.track ?: return
        val isPlaying = _uiState.value.isInstrumentalPlaying

        if (isPlaying) {
            instrumentalPlayer?.pause()
            _uiState.value = _uiState.value.copy(isInstrumentalPlaying = false)
        } else {
            stopVocals()
            if (instrumentalPlayer == null) {
                instrumentalPlayer = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(File(track.instrumentalPath).toUri()))
                    prepare()
                }
            }
            instrumentalPlayer?.play()
            _uiState.value = _uiState.value.copy(isInstrumentalPlaying = true)
        }
    }

    private fun stopVocals() {
        vocalsPlayer?.pause()
        _uiState.value = _uiState.value.copy(isVocalsPlaying = false)
    }

    private fun stopInstrumental() {
        instrumentalPlayer?.pause()
        _uiState.value = _uiState.value.copy(isInstrumentalPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()
        vocalsPlayer?.release()
        instrumentalPlayer?.release()
        vocalsPlayer = null
        instrumentalPlayer = null
    }
}