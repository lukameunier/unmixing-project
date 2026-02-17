package fr.mastersd.sime.unmixingproject.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.mastersd.sime.unmixingproject.data.Song
import fr.mastersd.sime.unmixingproject.repository.SongRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class SongUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val importSuccess: Boolean = false
)

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songRepository: SongRepositoryImpl,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongUiState())
    val uiState: StateFlow<SongUiState> = _uiState.asStateFlow()

    val songs: StateFlow<List<Song>> = songRepository.getAllSongsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val songList = songRepository.getAllSongsSuspend()
                _uiState.value = _uiState.value.copy(
                    songs = songList,
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

    fun importSongFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, importSuccess = false)
            try {
                val songInfo = extractSongInfo(uri)
                val cachedFilePath = cacheSongFile(uri, songInfo.first)

                val song = Song(
                    id = UUID.randomUUID().toString(),
                    title = songInfo.first,
                    uri = uri,
                    duration = songInfo.second
                )

                songRepository.addSongWithCachedPath(song, cachedFilePath)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    importSuccess = true,
                    error = null
                )

                loadSongs()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de l'import: ${e.message}",
                    importSuccess = false
                )
            }
        }
    }

    private suspend fun extractSongInfo(uri: Uri): Pair<String, Long> = withContext(Dispatchers.IO) {
        var title = "Unknown"
        var duration = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    title = cursor.getString(nameIndex) ?: "Unknown"
                    // Remove file extension for cleaner title
                    title = title.substringBeforeLast(".")
                }
            }
        }

        // Try to get duration using MediaMetadataRetriever
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durationStr?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (e: Exception) {
            // Duration couldn't be extracted, use default
        }

        Pair(title, duration)
    }

    private suspend fun cacheSongFile(uri: Uri, title: String): String = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "songs")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val fileName = "${UUID.randomUUID()}_${title.replace(" ", "_")}.audio"
        val cachedFile = File(cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(cachedFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        cachedFile.absolutePath
    }

    fun deleteSong(songId: String) {
        viewModelScope.launch {
            try {
                songRepository.deleteSong(songId)
                loadSongs()
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

    fun clearImportSuccess() {
        _uiState.value = _uiState.value.copy(importSuccess = false)
    }
}

