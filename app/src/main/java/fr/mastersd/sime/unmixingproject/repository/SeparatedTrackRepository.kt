package fr.mastersd.sime.unmixingproject.repository

import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.data.local.SeparatedTrackDao
import fr.mastersd.sime.unmixingproject.data.local.toEntity
import fr.mastersd.sime.unmixingproject.data.local.toSeparatedTrack
import fr.mastersd.sime.unmixingproject.data.local.toSeparatedTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeparatedTrackRepository @Inject constructor(
    private val separatedTrackDao: SeparatedTrackDao
) {

    fun getAllTracks(): Flow<List<SeparatedTrack>> {
        return separatedTrackDao.getAllTracks().map { it.toSeparatedTracks() }
    }

    suspend fun getAllTracksSuspend(): List<SeparatedTrack> {
        return separatedTrackDao.getAllTracksList().toSeparatedTracks()
    }

    suspend fun getTrackById(id: String): SeparatedTrack? {
        return separatedTrackDao.getTrackById(id)?.toSeparatedTrack()
    }

    suspend fun saveTrack(track: SeparatedTrack) {
        separatedTrackDao.insertTrack(track.toEntity())
    }

    suspend fun deleteTrack(id: String) {
        separatedTrackDao.deleteTrack(id)
    }

    suspend fun getTrackCount(): Int {
        return separatedTrackDao.getTrackCount()
    }
}

