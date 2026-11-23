package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.random.Random

@Composable
fun CreateRoomScreen(
    viewModel: GameViewModel,
    onRoomCreated: () -> Unit,
    onBack: () -> Unit
) {
    var nickname by remember { mutableStateOf("Host Detective") }
    var pin by remember { mutableStateOf(String.format("%04d", Random.nextInt(1000, 9999))) }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.create_room_title),
            color = DetectiveAppTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Nickname Input
        OutlinedTextField(
            value = nickname,
            onValueChange = {
                nickname = it
                viewModel.nickname.value = it
            },
            label = { Text(stringResource(R.string.create_room_nickname)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DetectiveAppTheme.colors.textPrimary,
                unfocusedTextColor = DetectiveAppTheme.colors.textPrimary,
                focusedBorderColor = DetectiveAppTheme.colors.primary,
                unfocusedBorderColor = DetectiveAppTheme.colors.border
            )
        )

        // Avatar Selection
        Text(
            text = stringResource(R.string.create_room_avatar),
            color = DetectiveAppTheme.colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(AvatarRegistry.ALL_AVATARS) { av ->
                AvatarView(
                    avatarId = av.id,
                    state = if (av.id == selectedAvatar) AvatarState.SELECTED else AvatarState.IDLE,
                    size = 54.dp,
                    isSelected = av.id == selectedAvatar,
                    onClick = { viewModel.selectedAvatarId.value = av.id }
                )
            }
        }

        // Room Security PIN & IP
        DetectiveCard {
            Text(
                text = stringResource(R.string.create_room_pin),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 13.sp
            )
            Text(
                text = pin,
                color = DetectiveAppTheme.colors.accentInvestigation,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.create_room_ip_label) + " 192.168.1.100 (Port 8080)",
                color = DetectiveAppTheme.colors.textMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DetectiveButton(
            text = stringResource(R.string.create_room_start_btn),
            onClick = {
                viewModel.nickname.value = nickname
                viewModel.hostRoom(pin)
                onRoomCreated()
            },
            isPrimary = true
        )

        DetectiveButton(
            text = stringResource(R.string.back),
            onClick = onBack,
            isPrimary = false
        )
    }
}
