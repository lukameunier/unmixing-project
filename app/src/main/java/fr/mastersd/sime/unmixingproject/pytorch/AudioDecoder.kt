package fr.mastersd.sime.unmixingproject.pytorch

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.ByteOrder

object AudioDecoder {

    /**
     * Décode l'URI audio et émet des chunks PCM de [chunkSamples] samples (par canal, interleaved).
     * Ne charge jamais plus d'un chunk en RAM à la fois.
     */
    fun decodeToChunks(
        context: Context,
        uri: Uri,
        chunkSamples: Int = AudioPreprocessor.SEGMENT_SAMPLES * AudioPreprocessor.NB_CHANNELS
    ): Flow<PcmChunk> = flow {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        var audioTrackIndex = -1
        var inputFormat: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                audioTrackIndex = i
                inputFormat = format
                break
            }
        }

        if (audioTrackIndex == -1 || inputFormat == null) {
            extractor.release()
            throw IllegalArgumentException("No audio track found")
        }

        extractor.selectTrack(audioTrackIndex)
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)

        val mime = inputFormat.getString(MediaFormat.KEY_MIME)!!
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(inputFormat, null, null, 0)
        codec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        // Buffer circulaire pour accumuler les samples décodés
        val accumulator = mutableListOf<Float>()

        while (!outputDone) {
            if (!inputDone) {
                val inputBufferId = codec.dequeueInputBuffer(10_000L)
                if (inputBufferId >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferId)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(
                            inputBufferId, 0, 0,
                            0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(
                            inputBufferId, 0, sampleSize,
                            extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
            }

            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            when {
                outputBufferId >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)!!
                    outputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    val shortBuffer = outputBuffer.asShortBuffer()
                    while (shortBuffer.hasRemaining()) {
                        accumulator.add(shortBuffer.get() / 32768f)
                    }
                    codec.releaseOutputBuffer(outputBufferId, false)

                    // Émettre des chunks complets dès qu'on en a assez
                    while (accumulator.size >= chunkSamples) {
                        val chunkFloats = FloatArray(chunkSamples) { accumulator[it] }
                        repeat(chunkSamples) { accumulator.removeAt(0) }
                        emit(PcmChunk(chunkFloats, sampleRate, isFinal = false))
                    }

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> { }
                outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> { }
            }
        }

        // Émettre le dernier chunk partiel (zero-padded géré par buildChunks)
        if (accumulator.isNotEmpty()) {
            emit(PcmChunk(accumulator.toFloatArray(), sampleRate, isFinal = true))
        }

        codec.stop()
        codec.release()
        extractor.release()
    }
}