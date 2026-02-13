package fr.mastersd.sime.unmixingproject.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Music : Screen("music")
}