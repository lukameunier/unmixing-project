package fr.mastersd.sime.unmixingproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                modifier = modifier,
                onNavigateToMusic = { trackId ->
                    navController.navigate(Screen.Music.createRoute(trackId))
                }
            )
        }

        composable(
            route = Screen.Music.route,
            arguments = listOf(navArgument("trackId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
            MusicScreen(
                modifier = modifier,
                trackId = trackId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}