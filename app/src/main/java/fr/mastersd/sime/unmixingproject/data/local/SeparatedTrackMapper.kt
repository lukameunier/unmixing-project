package fr.mastersd.sime.unmixingproject.data.local

import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun SeparatedTrackEntity.toSeparatedTrack(): SeparatedTrack {
    return SeparatedTrack(
        id = id,
        originalTitle = originalTitle,
        vocalData = byteArrayToFloatArray(vocalData),
        instrumentalData = byteArrayToFloatArray(instrumentalData),
        sampleRate = sampleRate,
        processedAt = processedAt
    )
}

fun SeparatedTrack.toEntity(): SeparatedTrackEntity {
    return SeparatedTrackEntity(
        id = id,
        originalTitle = originalTitle,
        vocalData = floatArrayToByteArray(vocalData),
        instrumentalData = floatArrayToByteArray(instrumentalData),
        sampleRate = sampleRate,
        processedAt = processedAt
    )
}

fun List<SeparatedTrackEntity>.toSeparatedTracks(): List<SeparatedTrack> = map { it.toSeparatedTrack() }

/**
 * Convert FloatArray to ByteArray for database storage
 */
private fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
    byteBuffer.order(ByteOrder.nativeOrder())
    byteBuffer.asFloatBuffer().put(floatArray)
    return byteBuffer.array()
}

/**
 * Convert ByteArray back to FloatArray when reading from database
 */
private fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
    val byteBuffer = ByteBuffer.wrap(byteArray)
    byteBuffer.order(ByteOrder.nativeOrder())
    val floatArray = FloatArray(byteArray.size / 4)
    byteBuffer.asFloatBuffer().get(floatArray)
    return floatArray
}

