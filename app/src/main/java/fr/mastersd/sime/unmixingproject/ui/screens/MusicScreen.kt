package fr.mastersd.sime.unmixingproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
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
            .fillMaxWidth()
            .background(Color(0xFF080C14))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color(0xFF8BA3C0)
                )
            }
            Text(
                text = "Piste séparée",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE8F4FF)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00F5FF))
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Erreur : ${uiState.error}", color = Color(0xFFFF4D6D))
                }
            }
            uiState.track != null -> {
                TrackContent(
                    track = uiState.track!!,
                    isVocalsPlaying = uiState.isVocalsPlaying,
                    isInstrumentalPlaying = uiState.isInstrumentalPlaying,
                    isOriginalPlaying = uiState.isOriginalPlaying,
                    onToggleVocals = { viewModel.toggleVocals() },
                    onToggleInstrumental = { viewModel.toggleInstrumental() },
                    onToggleOriginal = { viewModel.toggleOriginal() }
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
    isOriginalPlaying: Boolean,
    onToggleVocals: () -> Unit,
    onToggleInstrumental: () -> Unit,
    onToggleOriginal: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TrackInfoCard(track)
        if (track.originalPath.isNotEmpty()) {
            StemPlayerCard(
                label = "🎵 Original",
                isPlaying = isOriginalPlaying,
                accentColor = Color(0xFF8BA3C0),
                onToggle = onToggleOriginal
            )
        }
        StemPlayerCard(
            label = "🎤 Vocals",
            isPlaying = isVocalsPlaying,
            accentColor = Color(0xFF00F5FF),
            onToggle = onToggleVocals
        )
        StemPlayerCard(
            label = "🎸 Instrumental",
            isPlaying = isInstrumentalPlaying,
            accentColor = Color(0xFF7C3AFF),
            onToggle = onToggleInstrumental
        )
    }
}

@Composable
private fun TrackInfoCard(track: SeparatedTrack) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E2D45), RoundedCornerShape(16.dp))
            .background(Color(0xFF111827))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = track.originalTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE8F4FF)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            InfoChip(label = "DATE", value = formatDate(track.processedAt))
            InfoChip(label = "SAMPLE RATE", value = "${track.sampleRate} Hz")
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4A6380)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE8F4FF)
        )
    }
}

@Composable
private fun StemPlayerCard(
    label: String,
    isPlaying: Boolean,
    accentColor: Color,
    onToggle: () -> Unit
) {
    // Waveform placeholder générée une seule fois par card
    val waveformSamples = remember {
        List(60) { Random.nextFloat() * 0.8f + 0.1f }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .background(Color(0xFF111827))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE8F4FF)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            val barWidth = size.width / waveformSamples.size
            val centerY = size.height / 2f

            waveformSamples.forEachIndexed { index, amplitude ->
                val barHeight = amplitude * size.height * 0.9f
                val x = index * barWidth + barWidth / 2f
                drawLine(
                    color = accentColor.copy(alpha = if (isPlaying) 1f else 0.35f),
                    start = Offset(x, centerY - barHeight / 2f),
                    end = Offset(x, centerY + barHeight / 2f),
                    strokeWidth = (barWidth * 0.6f).coerceAtLeast(2f),
                    cap = StrokeCap.Round
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) accentColor else Color.Transparent)
                    .border(1.5.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Menu else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlaying) Color(0xFF080C14) else accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}