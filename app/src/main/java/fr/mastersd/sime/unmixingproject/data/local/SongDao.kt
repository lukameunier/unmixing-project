package fr.mastersd.sime.unmixingproject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    suspend fun getAllSongsList(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("UPDATE songs SET waveformData = :waveformData WHERE id = :id")
    suspend fun updateWaveformData(id: String, waveformData: ByteArray)

    @Query("UPDATE songs SET cachedFilePath = :cachedFilePath WHERE id = :id")
    suspend fun updateCachedFilePath(id: String, cachedFilePath: String)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
}

