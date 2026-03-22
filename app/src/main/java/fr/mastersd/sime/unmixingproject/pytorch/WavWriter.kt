package fr.mastersd.sime.unmixingproject.pytorch

import java.io.File
import java.io.RandomAccessFile

class WavWriter(
    private val file: File,
    private val sampleRate: Int,
    private val channels: Int = 1
) : AutoCloseable {

    private val raf = RandomAccessFile(file, "rw")
    private var dataSize = 0

    init {
        writeHeader()
    }

    fun write(samples: FloatArray) {
        for (sample in samples) {
            val s = (sample.coerceIn(-1f, 1f) * 32767f).toInt().toShort()
            raf.write(s.toInt() and 0xFF)
            raf.write((s.toInt() shr 8) and 0xFF)
        }
        dataSize += samples.size * 2
    }

    private fun writeHeader() {
        raf.seek(0)
        raf.write("RIFF".toByteArray())
        writeInt(0) // taille totale — on la fixe à la fin
        raf.write("WAVE".toByteArray())
        raf.write("fmt ".toByteArray())
        writeInt(16) // taille du bloc fmt
        writeShort(1) // PCM
        writeShort(channels)
        writeInt(sampleRate)
        writeInt(sampleRate * channels * 2) // byte rate
        writeShort(channels * 2) // block align
        writeShort(16) // bits per sample
        raf.write("data".toByteArray())
        writeInt(0) // taille data — on la fixe à la fin
    }

    private fun finalizeHeader() {
        raf.seek(4)
        writeInt(36 + dataSize)
        raf.seek(40)
        writeInt(dataSize)
    }

    private fun writeInt(value: Int) {
        raf.write(value and 0xFF)
        raf.write((value shr 8) and 0xFF)
        raf.write((value shr 16) and 0xFF)
        raf.write((value shr 24) and 0xFF)
    }

    private fun writeShort(value: Int) {
        raf.write(value and 0xFF)
        raf.write((value shr 8) and 0xFF)
    }

    override fun close() {
        finalizeHeader()
        raf.close()
    }
}