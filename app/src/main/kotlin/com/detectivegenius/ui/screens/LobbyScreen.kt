package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.R
import com.detectivegenius.core.model.GamePhase
import com.detectivegenius.designsystem.components.AvatarView
import com.detectivegenius.designsystem.components.DetectiveButton
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.viewmodel.GameViewModel

@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    onGameStarted: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val players = view?.players ?: emptyList()
    val me = players.find { it.id == view?.playerId }
    val isHost = view?.hostPlayerId == view?.playerId
    val isReady = me?.isReady ?: false

    val playerCount = players.size
    val isEven = playerCount % 2 == 0
    val isEnough = playerCount >= 4
    val allReady = players.isNotEmpty() && players.all { it.isReady }
    val canStart = isHost && isEven && isEnough && allReady

    LaunchedEffect(view?.phase) {
        if (view?.phase == GamePhase.QUESTION_CREATION) {
            onGameStarted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room Info
        DetectiveCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.lobby_title),
                        color = DetectiveAppTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.lobby_room_pin, view?.roomPin ?: "----"),
                        color = DetectiveAppTheme.colors.accentInvestigation,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = stringResource(R.string.lobby_players_count, playerCount, 20),
                    color = DetectiveAppTheme.colors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Warnings / Rules
        if (!isEnough) {
            Text(
                text = "⚠️ " + stringResource(R.string.lobby_min_players_warning),
                color = DetectiveAppTheme.colors.warning,
                fontSize = 13.sp
            )
        } else if (!isEven) {
            Text(
                text = "⚠️ " + stringResource(R.string.lobby_even_players_warning),
                color = DetectiveAppTheme.colors.warning,
                fontSize = 13.sp
            )
        } else if (!allReady) {
            Text(
                text = "⏳ " + stringResource(R.string.lobby_not_all_ready_warning),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 13.sp
            )
        }

        // Player List
        Text(
            text = stringResource(R.string.lobby_players_title),
            color = DetectiveAppTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(players) { p ->
                DetectiveCard(
                    backgroundColor = if (p.id == view?.playerId) DetectiveAppTheme.colors.surfaceElevated else DetectiveAppTheme.colors.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AvatarView(
                                avatarId = p.avatarId,
                                size = 44.dp
                            )
                            Column {
                                Text(
                                    text = p.nickname + if (p.id == view?.hostPlayerId) " (Host)" else "",
                                    color = DetectiveAppTheme.colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = if (p.isReady) stringResource(R.string.lobby_player_ready) else stringResource(R.string.lobby_player_not_ready),
                            color = if (p.isReady) DetectiveAppTheme.colors.success else DetectiveAppTheme.colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Actions
        DetectiveButton(
            text = if (isReady) stringResource(R.string.lobby_ready_toggle_off) else stringResource(R.string.lobby_ready_toggle_on),
            onClick = { viewModel.setReady(!isReady) },
            isPrimary = !isReady
        )

        if (isHost) {
            DetectiveButton(
                text = stringResource(R.string.lobby_start_game_btn),
                onClick = { viewModel.startGame() },
                enabled = canStart,
                isPrimary = true
            )
        } else {
            Text(
                text = stringResource(R.string.lobby_waiting_for_host),
                color = DetectiveAppTheme.colors.textMuted,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
