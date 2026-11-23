package com.detectivegenius

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.detectivegenius.designsystem.theme.DetectiveTheme
import com.detectivegenius.navigation.Screen
import com.detectivegenius.ui.screens.*
import com.detectivegenius.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STRICT INVARIANT: Force portrait orientation exclusively!
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val isDarkTheme by gameViewModel.isDarkTheme.collectAsState()
            val language by gameViewModel.currentLanguage.collectAsState()

            // RTL support for Persian, LTR for English
            val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                DetectiveTheme(darkTheme = isDarkTheme) {
                    DetectiveNavigationGraph(viewModel = gameViewModel)
                }
            }
        }
    }
}

@Composable
fun DetectiveNavigationGraph(viewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onCreateRoom = { navController.navigate(Screen.CreateRoom.route) },
                onJoinRoom = { navController.navigate(Screen.JoinRoom.route) },
                onHowToPlay = { navController.navigate(Screen.Onboarding.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.CreateRoom.route) {
            CreateRoomScreen(
                viewModel = viewModel,
                onRoomCreated = { navController.navigate(Screen.Lobby.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.JoinRoom.route) {
            JoinRoomScreen(
                viewModel = viewModel,
                onRoomJoined = { navController.navigate(Screen.Lobby.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Lobby.route) {
            LobbyScreen(
                viewModel = viewModel,
                onGameStarted = { navController.navigate(Screen.QuestionCreation.route) }
            )
        }

        composable(Screen.QuestionCreation.route) {
            QuestionCreationScreen(
                viewModel = viewModel,
                onQuestionsSubmitted = { navController.navigate(Screen.Answering.route) }
            )
        }

        composable(Screen.Answering.route) {
            AnsweringScreen(
                viewModel = viewModel,
                onAnswersSubmitted = { navController.navigate(Screen.Distribution.route) }
            )
        }

        composable(Screen.Distribution.route) {
            DistributionScreen(
                viewModel = viewModel,
                onOpenMysteryBoard = { navController.navigate(Screen.MysteryBoard.route) }
            )
        }

        composable(Screen.MysteryBoard.route) {
            MysteryBoardScreen(
                viewModel = viewModel,
                onInspectPackages = { navController.navigate(Screen.PackageViewer.route) },
                onOpenChat = { navController.navigate(Screen.Chat.route) },
                onEliminated = { navController.navigate(Screen.Elimination.route) },
                onVictory = { navController.navigate(Screen.Victory.route) }
            )
        }

        composable(Screen.PackageViewer.route) {
            PackageViewerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Victory.route) {
            VictoryScreen(
                viewModel = viewModel,
                onReturnHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Elimination.route) {
            EliminationScreen(
                viewModel = viewModel,
                onOpenComms = { navController.navigate(Screen.Chat.route) },
                onViewEvidence = { navController.navigate(Screen.PackageViewer.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
