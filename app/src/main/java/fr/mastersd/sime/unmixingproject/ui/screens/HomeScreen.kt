package fr.mastersd.sime.unmixingproject.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.mastersd.sime.unmixingproject.R
import fr.mastersd.sime.unmixingproject.ui.components.SongItem
import fr.mastersd.sime.unmixingproject.ui.theme.UnmixingProjectTheme
import fr.mastersd.sime.unmixingproject.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMusic: () -> Unit = {}
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()

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

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items = songs, key = { it.id }) { song ->
                SongItem(song = song)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {},
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(4f)
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
            onClick = { viewModel.testLoadModel() }
        ) {
            Text(text = "Unmix")
        }
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MyComponentPreview() {
    UnmixingProjectTheme {
        HomeScreen(modifier = Modifier.fillMaxSize())
    }
}