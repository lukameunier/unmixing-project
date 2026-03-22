package fr.mastersd.sime.unmixingproject.data.local

import fr.mastersd.sime.unmixingproject.data.SeparatedTrack

fun SeparatedTrackEntity.toSeparatedTrack(): SeparatedTrack {
    return SeparatedTrack(
        id = id,
        originalTitle = originalTitle,
        vocalPath = vocalPath,
        instrumentalPath = instrumentalPath,
        sampleRate = sampleRate,
        processedAt = processedAt
    )
}

fun SeparatedTrack.toEntity(): SeparatedTrackEntity {
    return SeparatedTrackEntity(
        id = id,
        originalTitle = originalTitle,
        vocalPath = vocalPath,
        instrumentalPath = instrumentalPath,
        sampleRate = sampleRate,
        processedAt = processedAt
    )
}

fun List<SeparatedTrackEntity>.toSeparatedTracks(): List<SeparatedTrack> =
    map { it.toSeparatedTrack() }