package fr.mastersd.sime.unmixingproject.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.repository.SeparatedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicUiState(
    val track: SeparatedTrack? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Player state
    val isVocalsPlaying: Boolean = false,
    val isInstrumentalPlaying: Boolean = false,
    val vocalsProgress: Float = 0f,
    val instrumentalProgress: Float = 0f
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val separatedTrackRepository: SeparatedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    fun loadTrack(trackId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val track = separatedTrackRepository.getTrackById(trackId)
                _uiState.value = _uiState.value.copy(
                    track = track,
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

    fun toggleVocals() {
        _uiState.value = _uiState.value.copy(
            isVocalsPlaying = !_uiState.value.isVocalsPlaying,
            isInstrumentalPlaying = false // stop l'autre
        )
    }

    fun toggleInstrumental() {
        _uiState.value = _uiState.value.copy(
            isInstrumentalPlaying = !_uiState.value.isInstrumentalPlaying,
            isVocalsPlaying = false // stop l'autre
        )
    }
}