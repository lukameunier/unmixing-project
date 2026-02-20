package fr.mastersd.sime.unmixingproject.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SeparatedTrackEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun separatedTrackDao(): SeparatedTrackDao
}

