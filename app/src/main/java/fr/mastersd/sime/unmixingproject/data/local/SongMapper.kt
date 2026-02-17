package fr.mastersd.sime.unmixingproject.data.local

import androidx.core.net.toUri
import fr.mastersd.sime.unmixingproject.data.Song

fun SongEntity.toSong(): Song {
    return Song(
        id = id,
        title = title,
        uri = uri.toUri(),
        duration = duration,
        waveformData = waveformData
    )
}

fun Song.toEntity(cachedFilePath: String? = null): SongEntity {
    return SongEntity(
        id = id,
        title = title,
        uri = uri.toString(),
        duration = duration,
        waveformData = waveformData,
        cachedFilePath = cachedFilePath
    )
}

fun List<SongEntity>.toSongs(): List<Song> = map { it.toSong() }

fun List<Song>.toEntities(): List<SongEntity> = map { it.toEntity() }

