package fr.mastersd.sime.unmixingproject.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.unmixingproject.data.Song
import fr.mastersd.sime.unmixingproject.repository.SongRepository
import fr.mastersd.sime.unmixingproject.tflite.TFLiteModelRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val modelRunner: TFLiteModelRunner
) : ViewModel() {

    val songs: StateFlow<List<Song>> = songRepository
        .getAllSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun testLoadModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                modelRunner.initialize()
                Log.d("TFLite", "Model loaded: ${modelRunner.isInitialized}")
            } catch (e: Exception) {
                Log.e("TFLite", "Model load Failed: ${e.message}")
            }
        }
    }
}