package fr.mastersd.sime.unmixingproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.mastersd.sime.unmixingproject.R
import fr.mastersd.sime.unmixingproject.data.SeparatedTrack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SeparatedTrackItem(
    track: SeparatedTrack,
    modifier: Modifier = Modifier,
    onCLick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color(0xFF1E2D45), shape)
            .background(Color(0xFF111827))
            .clickable { onCLick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF00F5FF).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF00F5FF).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(id = R.drawable.mic_24dp),
                    contentDescription = null,
                    tint = Color(0xFF00F5FF)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = track.originalTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE8F4FF)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00F5FF).copy(alpha = 0.1f))
                            .border(0.5.dp, Color(0xFF00F5FF).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VOCALS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00F5FF)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF7C3AFF).copy(alpha = 0.1f))
                            .border(0.5.dp, Color(0xFF7C3AFF).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "INSTR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7C3AFF)
                        )
                    }
                }
                Text(
                    text = formatDate(track.processedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8BA3C0)
                )
            }
        }

        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(id = R.drawable.search_24dp),
            contentDescription = null,
            tint = Color(0xFF8BA3C0)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}