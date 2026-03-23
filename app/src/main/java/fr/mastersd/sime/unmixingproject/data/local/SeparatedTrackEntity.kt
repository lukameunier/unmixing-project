package fr.mastersd.sime.unmixingproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "separated_tracks")
data class SeparatedTrackEntity(
    @PrimaryKey
    val id: String,
    val originalTitle: String,
    val vocalPath: String,
    val originalPath: String = "",
    val instrumentalPath: String,
    val sampleRate: Int = 44100,
    val processedAt: Long = System.currentTimeMillis()
)