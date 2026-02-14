package fr.mastersd.sime.unmixingproject.data

import android.net.Uri

data class UnmixedTrack(
    val songId: String,
    val stems: Map<StemType, Uri>,
    val processedAt: Long
)