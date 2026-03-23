package fr.mastersd.sime.unmixingproject.ui.screens

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import fr.mastersd.sime.unmixingproject.R
import fr.mastersd.sime.unmixingproject.ui.components.SeparatedTrackItem
import fr.mastersd.sime.unmixingproject.ui.theme.UnmixingProjectTheme
import fr.mastersd.sime.unmixingproject.viewmodels.SongViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: SongViewModel = hiltViewModel(),
    onNavigateToMusic: (trackId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val separatedTracks by viewModel.separatedTracks.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(audioPermission)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.processAudioFromUri(it) }
    }

    LaunchedEffect(uiState.processSuccess) {
        if (uiState.processSuccess) {
            snackbarHostState.showSnackbar("Audio traité avec succès !")
            viewModel.clearProcessSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            containerColor = Color(0xFF111827),
            titleContentColor = Color(0xFFE8F4FF),
            textContentColor = Color(0xFF8BA3C0),
            title = { Text("Permission requise") },
            text = { Text("L'application a besoin d'accéder à vos fichiers audio.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    permissionState.launchPermissionRequest()
                }) {
                    Text("Autoriser", color = Color(0xFF00F5FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Annuler", color = Color(0xFF8BA3C0))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UNMIXING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F5FF),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Separated Tracks",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE8F4FF)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF1E2D45), RoundedCornerShape(10.dp))
                    .background(Color(0xFF111827)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.search_24dp),
                    contentDescription = "Search",
                    tint = Color(0xFF8BA3C0)
                )
            }
        }

        // Progress
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E2D45), RoundedCornerShape(12.dp))
                    .background(Color(0xFF111827))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Traitement en cours...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE8F4FF)
                    )
                    Text(
                        text = "${(uiState.processingProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00F5FF),
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { uiState.processingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF00F5FF),
                    trackColor = Color(0xFF1E2D45)
                )
                uiState.currentAudioBuffer?.let { buffer ->
                    Text(
                        text = buffer.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8BA3C0)
                    )
                }
            }
        }

        // Liste
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = separatedTracks, key = { it.id }) { track ->
                SeparatedTrackItem(
                    track = track,
                    onCLick = { onNavigateToMusic(track.id) }
                )
            }

            if (separatedTracks.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Aucune piste séparée",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF8BA3C0)
                            )
                            Text(
                                text = "Importez un fichier audio pour commencer",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4A6380)
                            )
                        }
                    }
                }
            }
        }

        // Boutons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    when {
                        permissionState.status.isGranted -> filePickerLauncher.launch(arrayOf("audio/*"))
                        permissionState.status.shouldShowRationale -> showPermissionRationale = true
                        else -> permissionState.launchPermissionRequest()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(4f)
                    .height(52.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00F5FF),
                    contentColor = Color(0xFF080C14),
                    disabledContainerColor = Color(0xFF00F5FF).copy(alpha = 0.3f),
                    disabledContentColor = Color(0xFF080C14).copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.download_24dp),
                    contentDescription = null,
                    tint = Color(0xFF080C14)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Import & Unmix",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AFF).copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF7C3AFF)
                )
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(id = R.drawable.mic_24dp),
                    contentDescription = null,
                    tint = Color(0xFF7C3AFF)
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun HomeScreenPreview() {
    UnmixingProjectTheme {
        HomeScreenPreviewContent()
    }
}

@Composable
private fun HomeScreenPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "UNMIXING", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00F5FF))
        Text(text = "Separated Tracks", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE8F4FF))
    }
}