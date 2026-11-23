package com.detectivegenius.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.R
import com.detectivegenius.designsystem.avatar.AvatarState
import com.detectivegenius.designsystem.components.AvatarView
import com.detectivegenius.designsystem.components.DetectiveButton
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.viewmodel.GameViewModel

@Composable
fun VictoryScreen(
    viewModel: GameViewModel,
    onReturnHome: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val winnerId = view?.winnerPlayerId
    val winnerPlayer = view?.players?.find { it.id == winnerId }

    val infiniteTransition = rememberInfiniteTransition(label = "victoryGlow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winnerScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "🏆", fontSize = 54.sp)
            Text(
                text = stringResource(R.string.victory_title),
                color = DetectiveAppTheme.colors.accentInvestigation,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.victory_winner_announcement, winnerPlayer?.nickname ?: "Mystery Solver"),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier.scale(scale),
            contentAlignment = Alignment.Center
        ) {
            AvatarView(
                avatarId = winnerPlayer?.avatarId ?: "detective",
                state = AvatarState.VICTORY,
                size = 110.dp
            )
        }

        DetectiveCard(
            backgroundColor = DetectiveAppTheme.colors.surfaceElevated
        ) {
            Text(
                text = "All 4 Mysteries Successfully Solved!",
                color = DetectiveAppTheme.colors.success,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✓ Who answered my questions\n✓ Whose questions I answered\n✓ Package Authors held\n✓ Package Answerers held",
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DetectiveButton(
            text = stringResource(R.string.victory_play_again),
            onClick = onReturnHome,
            isPrimary = true
        )
    }
}
