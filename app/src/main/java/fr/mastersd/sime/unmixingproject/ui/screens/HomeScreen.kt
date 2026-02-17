package fr.mastersd.sime.unmixingproject.ui.screens

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import fr.mastersd.sime.unmixingproject.R
import fr.mastersd.sime.unmixingproject.ui.components.SongItem
import fr.mastersd.sime.unmixingproject.ui.theme.UnmixingProjectTheme
import fr.mastersd.sime.unmixingproject.viewmodels.SongViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: SongViewModel = hiltViewModel(),
    onNavigateToMusic: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val songs by viewModel.songs.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Permission state based on Android version
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(audioPermission)

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importSongFromUri(it) }
    }

    // Handle import success
    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            snackbarHostState.showSnackbar("Musique importée avec succès!")
            viewModel.clearImportSuccess()
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Permission rationale dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Permission requise") },
            text = {
                Text("L'application a besoin d'accéder à vos fichiers audio pour importer des musiques.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        permissionState.launchPermissionRequest()
                    }
                ) {
                    Text("Autoriser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Songs", style = MaterialTheme.typography.headlineLarge)
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.search_24dp),
                contentDescription = "Search",
                tint = Color.Green
            )
        }

        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = songs,
                key = { it.id }
            ) { song ->
                SongItem(song = song)
            }

            // Empty state
            if (songs.isEmpty() && !uiState.isLoading) {
                item {
                    Text(
                        text = "Aucune musique importée. Cliquez sur 'Import songs' pour commencer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    when {
                        permissionState.status.isGranted -> {
                            // Permission granted, open file picker
                            filePickerLauncher.launch(arrayOf("audio/*"))
                        }
                        permissionState.status.shouldShowRationale -> {
                            // Show rationale dialog
                            showPermissionRationale = true
                        }
                        else -> {
                            // Request permission
                            permissionState.launchPermissionRequest()
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(4f),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = R.drawable.download_24dp),
                    contentDescription = "Import",
                    tint = Color.Green
                )
                Text(text = "Import songs")
            }

            Button(
                onClick = {},
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = R.drawable.mic_24dp),
                    contentDescription = "Record",
                    tint = Color.Green
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Green,
                contentColor = Color.Black
            ),
            shape = MaterialTheme.shapes.small,
            onClick = onNavigateToMusic,
            enabled = songs.isNotEmpty()
        ) {
            Text(text = "Unmix")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun HomeScreenPreview() {
    UnmixingProjectTheme {
        // Preview with mock data - Note: ViewModel won't work in preview
        HomeScreenPreviewContent()
    }
}

@Composable
private fun HomeScreenPreviewContent() {
    // Simplified preview without ViewModel
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Songs",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Preview - Import songs functionality",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}