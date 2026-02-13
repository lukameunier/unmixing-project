package fr.mastersd.sime.unmixingproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.mastersd.sime.unmixingproject.ui.screens.HomeScreen
import fr.mastersd.sime.unmixingproject.ui.screens.MusicScreen


@Composable
fun Navigation(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMusic = {
                    navController.navigate(Screen.Music.route)
                }
            )
        }

        composable(Screen.Music.route) {
            MusicScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }
    }
}