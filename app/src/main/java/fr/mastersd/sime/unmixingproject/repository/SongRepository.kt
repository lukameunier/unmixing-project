package fr.mastersd.sime.unmixingproject.repository

import fr.mastersd.sime.unmixingproject.data.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): List<Song>
    suspend fun getSongById(id: String): Song?
    suspend fun addSong(song: Song)
    suspend fun deleteSong(id: String)
    suspend fun updateWaveformData(id: String, waveformData: ByteArray)
}