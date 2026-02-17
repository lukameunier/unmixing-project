package fr.mastersd.sime.unmixingproject.repository

import fr.mastersd.sime.unmixingproject.data.Song
import fr.mastersd.sime.unmixingproject.data.local.SongDao
import fr.mastersd.sime.unmixingproject.data.local.toEntity
import fr.mastersd.sime.unmixingproject.data.local.toSong
import fr.mastersd.sime.unmixingproject.data.local.toSongs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : SongRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { it.toSongs() }
    }

    suspend fun getAllSongsSuspend(): List<Song> {
        return songDao.getAllSongsList().toSongs()
    }

    override suspend fun getSongById(id: String): Song? {
        return songDao.getSongById(id)?.toSong()
    }

    override suspend fun addSong(song: Song) {
        songDao.insertSong(song.toEntity())
    }

    suspend fun addSongWithCachedPath(song: Song, cachedFilePath: String) {
        songDao.insertSong(song.toEntity(cachedFilePath))
    }

    override suspend fun deleteSong(id: String) {
        songDao.deleteSong(id)
    }

    override suspend fun updateWaveformData(id: String, waveformData: ByteArray) {
        songDao.updateWaveformData(id, waveformData)
    }

    suspend fun updateCachedFilePath(id: String, cachedFilePath: String) {
        songDao.updateCachedFilePath(id, cachedFilePath)
    }
}