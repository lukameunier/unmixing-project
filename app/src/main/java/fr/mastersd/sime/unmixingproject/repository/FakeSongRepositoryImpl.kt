package fr.mastersd.sime.unmixingproject.repository

import androidx.core.net.toUri
import fr.mastersd.sime.unmixingproject.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FakeSongRepositoryImpl @Inject constructor(): SongRepository {

    val fakeSongs = listOf(
        Song(id = "1",  title = "Bohemian Rhapsody",       uri = "content://media/external/audio/media/1".toUri(),  duration = 354000L),
        Song(id = "2",  title = "Billie Jean",              uri = "content://media/external/audio/media/2".toUri(),  duration = 294000L),
        Song(id = "3",  title = "Hotel California",         uri = "content://media/external/audio/media/3".toUri(),  duration = 391000L),
        Song(id = "4",  title = "Smells Like Teen Spirit",  uri = "content://media/external/audio/media/4".toUri(),  duration = 301000L),
        Song(id = "5",  title = "Lose Yourself",            uri = "content://media/external/audio/media/5".toUri(),  duration = 326000L),
        Song(id = "6",  title = "Stairway to Heaven",       uri = "content://media/external/audio/media/6".toUri(),  duration = 482000L),
        Song(id = "7",  title = "Superstition",             uri = "content://media/external/audio/media/7".toUri(),  duration = 245000L),
        Song(id = "8",  title = "Purple Rain",              uri = "content://media/external/audio/media/8".toUri(),  duration = 520000L),
        Song(id = "9",  title = "Blinding Lights",          uri = "content://media/external/audio/media/9".toUri(),  duration = 200000L),
        Song(id = "10", title = "Thriller",                 uri = "content://media/external/audio/media/10".toUri(), duration = 358000L),
        Song(id = "11", title = "Shape of You",             uri = "content://media/external/audio/media/11".toUri(), duration = 234000L),
        Song(id = "12", title = "Redbone",                  uri = "content://media/external/audio/media/12".toUri(), duration = 329000L),
        Song(id = "13", title = "Nights",                   uri = "content://media/external/audio/media/13".toUri(), duration = 309000L),
        Song(id = "14", title = "Hotline Bling",            uri = "content://media/external/audio/media/14".toUri(), duration = 267000L),
        Song(id = "15", title = "DNA.",                     uri = "content://media/external/audio/media/15".toUri(), duration = 185000L),
        Song(id = "16", title = "Bad Guy",                  uri = "content://media/external/audio/media/16".toUri(), duration = 194000L),
        Song(id = "17", title = "Levitating",               uri = "content://media/external/audio/media/17".toUri(), duration = 203000L),
        Song(id = "18", title = "Savage",                   uri = "content://media/external/audio/media/18".toUri(), duration = 218000L),
        Song(id = "19", title = "Montero",                  uri = "content://media/external/audio/media/19".toUri(), duration = 137000L),
        Song(id = "20", title = "As It Was",                uri = "content://media/external/audio/media/20".toUri(), duration = 167000L),
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