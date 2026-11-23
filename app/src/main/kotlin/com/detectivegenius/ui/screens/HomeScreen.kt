package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.R
import com.detectivegenius.designsystem.avatar.AvatarRegistry
import com.detectivegenius.designsystem.avatar.AvatarState
import com.detectivegenius.designsystem.components.AvatarView
import com.detectivegenius.designsystem.components.DetectiveButton
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit
) {
    val selectedAvatar by viewModel.selectedAvatarId.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Branding Header
        Text(
            text = "🕵️‍♂️",
            fontSize = 48.sp
        )
        Text(
            text = stringResource(R.string.home_title),
            style = DetectiveAppTheme.colors.let {
                androidx.compose.ui.text.TextStyle(
                    color = it.textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            color = DetectiveAppTheme.colors.accentInvestigation,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Avatar Highlight & Avatar Picker
        DetectiveCard {
            Text(
                text = stringResource(R.string.create_room_avatar),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AvatarView(
                    avatarId = selectedAvatar,
                    state = AvatarState.SELECTED,
                    size = 80.dp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AvatarRegistry.ALL_AVATARS) { av ->
                    AvatarView(
                        avatarId = av.id,
                        state = if (av.id == selectedAvatar) AvatarState.SELECTED else AvatarState.IDLE,
                        size = 52.dp,
                        isSelected = av.id == selectedAvatar,
                        onClick = { viewModel.selectedAvatarId.value = av.id }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Actions
        DetectiveButton(
            text = stringResource(R.string.home_create_room),
            onClick = onCreateRoom,
            isPrimary = true
        )

        DetectiveButton(
            text = stringResource(R.string.home_join_room),
            onClick = onJoinRoom,
            isPrimary = false
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DetectiveButton(
                    text = stringResource(R.string.home_how_to_play),
                    onClick = onHowToPlay,
                    isPrimary = false
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DetectiveButton(
                    text = stringResource(R.string.home_settings),
                    onClick = onSettings,
                    isPrimary = false
                )
            }
        }
    }
}
