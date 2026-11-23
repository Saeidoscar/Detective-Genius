package com.detectivegenius.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object CreateRoom : Screen("create_room")
    data object JoinRoom : Screen("join_room")
    data object Lobby : Screen("lobby")
    data object QuestionCreation : Screen("question_creation")
    data object Answering : Screen("answering")
    data object Distribution : Screen("distribution")
    data object MysteryBoard : Screen("mystery_board")
    data object PackageViewer : Screen("package_viewer")
    data object Chat : Screen("chat")
    data object Victory : Screen("victory")
    data object Elimination : Screen("elimination")
    data object Settings : Screen("settings")
}
