package fr.mastersd.sime.unmixingproject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SeparatedTrackDao {

    @Query("SELECT * FROM separated_tracks ORDER BY processedAt DESC")
    fun getAllTracks(): Flow<List<SeparatedTrackEntity>>

    @Query("SELECT * FROM separated_tracks ORDER BY processedAt DESC")
    suspend fun getAllTracksList(): List<SeparatedTrackEntity>

    @Query("SELECT * FROM separated_tracks WHERE id = :id")
    suspend fun getTrackById(id: String): SeparatedTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: SeparatedTrackEntity)

    @Query("DELETE FROM separated_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("SELECT COUNT(*) FROM separated_tracks")
    suspend fun getTrackCount(): Int
}

