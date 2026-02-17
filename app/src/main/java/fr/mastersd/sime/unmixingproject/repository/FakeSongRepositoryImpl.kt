package fr.mastersd.sime.unmixingproject.repository

import androidx.core.net.toUri
import fr.mastersd.sime.unmixingproject.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSongRepositoryImpl : SongRepository {

    val fakeSongs = listOf(
        Song(
            id = "1",
            title = "Bohemian Rhapsody",
            uri = "content://media/external/audio/media/1".toUri(),
            duration = 354000L
        ),
        Song(
            id = "2",
            title = "Billie Jean",
            uri = "content://media/external/audio/media/2".toUri(),
            duration = 294000L
        ),
        Song(
            id = "3",
            title = "Hotel California",
            uri = "content://media/external/audio/media/3".toUri(),
            duration = 391000L
        ),
        Song(
            id = "4",
            title = "Smells Like Teen Spirit",
            uri = "content://media/external/audio/media/4".toUri(),
            duration = 301000L
        ),
        Song(
            id = "5",
            title = "Lose Yourself",
            uri = "content://media/external/audio/media/5".toUri(),
            duration = 326000L
        )
    )

    private val _songs = MutableStateFlow<List<Song>>(value = fakeSongs)
    val songs: List<Song> get() = _songs.value

    //override fun getAllSongs(): Flow<List<Song>> = _songs.asStateFlow()

    override fun getAllSongs(): List<Song> {
        return _songs.value
    }

    override suspend fun getSongById(id: String): Song? {
        return _songs.value.find { it.id == id }
    }

    override suspend fun addSong(song: Song) {
        _songs.update { current ->
            if (current.any { it.id == song.id }) current
            else current + song
        }
    }

    override suspend fun deleteSong(id: String) {
        _songs.update { current -> current.filter { it.id != id } }
    }

    override suspend fun updateWaveformData(id: String, waveformData: ByteArray) {
        _songs.update { current ->
            current.map { song ->
                if (song.id == id) song.copy(waveformData = waveformData) else song
            }
        }
    }
}