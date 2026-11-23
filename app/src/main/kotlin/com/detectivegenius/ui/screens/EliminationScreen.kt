package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun EliminationScreen(
    viewModel: GameViewModel,
    onOpenComms: () -> Unit,
    onViewEvidence: () -> Unit
) {
    val selectedAvatar by viewModel.selectedAvatarId.collectAsState()

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
            Text(text = "🛑", fontSize = 48.sp)
            Text(
                text = stringResource(R.string.elimination_title),
                color = DetectiveAppTheme.colors.error,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.elimination_message),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        AvatarView(
            avatarId = selectedAvatar,
            state = AvatarState.ELIMINATED,
            size = 96.dp
        )

        DetectiveCard(
            backgroundColor = DetectiveAppTheme.colors.surfaceElevated
        ) {
            Text(
                text = stringResource(R.string.elimination_spectate),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetectiveButton(
                text = "📡 " + stringResource(R.string.chat_title),
                onClick = onOpenComms,
                isPrimary = true
            )
            DetectiveButton(
                text = "📂 " + stringResource(R.string.mb_inspect_packages),
                onClick = onViewEvidence,
                isPrimary = false
            )
        }
    }
}
