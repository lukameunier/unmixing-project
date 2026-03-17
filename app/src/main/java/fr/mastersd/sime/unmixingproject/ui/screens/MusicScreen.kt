package fr.mastersd.sime.unmixingproject.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import fr.mastersd.sime.unmixingproject.viewmodels.MusicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun MusicScreen(
    modifier: Modifier,
    trackId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: MusicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour"
                )
            }
            Text(
                text = "Piste séparée",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Erreur : ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.track != null -> {
                TrackContent(
                    track = uiState.track!!,
                    isVocalsPlaying = uiState.isVocalsPlaying,
                    isInstrumentalPlaying = uiState.isInstrumentalPlaying,
                    onToggleVocals = { viewModel.toggleVocals() },
                    onToggleInstrumental = { viewModel.toggleInstrumental() }
                )
            }
        }
    }
}

@Composable
private fun TrackContent(
    track: SeparatedTrack,
    isVocalsPlaying: Boolean,
    isInstrumentalPlaying: Boolean,
    onToggleVocals: () -> Unit,
    onToggleInstrumental: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Infos de la piste
        TrackInfoCard(track)

        // Player Vocals
        StemPlayerCard(
            label = "🎤 Vocals",
            audioData = track.vocalData,
            isPlaying = isVocalsPlaying,
            accentColor = MaterialTheme.colorScheme.primary,
            onToggle = onToggleVocals
        )

        // Player Instrumental
        StemPlayerCard(
            label = "🎸 Instrumental",
            audioData = track.instrumentalData,
            isPlaying = isInstrumentalPlaying,
            accentColor = MaterialTheme.colorScheme.secondary,
            onToggle = onToggleInstrumental
        )
    }
}

@Composable
private fun TrackInfoCard(track: SeparatedTrack) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = track.originalTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(label = "Date", value = formatDate(track.processedAt))
                InfoChip(label = "Sample rate", value = "${track.sampleRate} Hz")
                InfoChip(
                    label = "Durée ~",
                    value = formatDuration(track.vocalData.size, track.sampleRate)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StemPlayerCard(
    label: String,
    audioData: FloatArray,
    isPlaying: Boolean,
    accentColor: Color,
    onToggle: () -> Unit
) {
    // Échantillonne les données audio pour la waveform
    val waveformSamples = remember(audioData) {
        buildWaveformSamples(audioData, barCount = 60)
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.5f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Waveform
            Waveform(
                samples = waveformSamples,
                isPlaying = isPlaying,
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )

            // Bouton play/pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Menu else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Waveform(
    samples: List<Float>,
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / samples.size
        val centerY = size.height / 2f

        samples.forEachIndexed { index, amplitude ->
            val barHeight = amplitude * size.height * 0.9f
            val x = index * barWidth + barWidth / 2f

            drawLine(
                color = color.copy(alpha = if (isPlaying) 1f else 0.4f),
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = (barWidth * 0.6f).coerceAtLeast(2f),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Réduit le FloatArray audio en un nombre fixe de barres pour la waveform.
 * Prend la valeur absolue max de chaque segment.
 */
private fun buildWaveformSamples(audioData: FloatArray, barCount: Int): List<Float> {
    if (audioData.isEmpty()) {
        // Génère une waveform vide mais non nulle pour le placeholder
        return List(barCount) { Random.nextFloat() * 0.3f + 0.05f }
    }

    val segmentSize = (audioData.size / barCount).coerceAtLeast(1)
    return List(barCount) { barIndex ->
        val start = barIndex * segmentSize
        val end = minOf(start + segmentSize, audioData.size)
        var max = 0f
        for (i in start until end) {
            val v = abs(audioData[i])
            if (v > max) max = v
        }
        max.coerceIn(0.05f, 1f)
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDuration(sampleCount: Int, sampleRate: Int): String {
    if (sampleRate == 0) return "—"
    val totalSeconds = sampleCount / sampleRate
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}