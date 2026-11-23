package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun JoinRoomScreen(
    viewModel: GameViewModel,
    onRoomJoined: () -> Unit,
    onBack: () -> Unit
) {
    var nickname by remember { mutableStateOf("Player Detective") }
    var hostIp by remember { mutableStateOf("192.168.1.100") }
    var pin by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(1) } // 0 = Discovery, 1 = Manual
    val selectedAvatar by viewModel.selectedAvatarId.collectAsState()
    val feedback by viewModel.deductionFeedback.collectAsState()
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
            text = stringResource(R.string.join_room_title),
            color = DetectiveAppTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Nickname
        OutlinedTextField(
            value = nickname,
            onValueChange = {
                nickname = it
                viewModel.nickname.value = it
            },
            label = { Text(stringResource(R.string.join_room_nickname)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DetectiveAppTheme.colors.textPrimary,
                unfocusedTextColor = DetectiveAppTheme.colors.textPrimary,
                focusedBorderColor = DetectiveAppTheme.colors.primary,
                unfocusedBorderColor = DetectiveAppTheme.colors.border
            )
        )

        // Avatar
        Text(
            text = stringResource(R.string.join_room_avatar),
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

        // Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DetectiveAppTheme.colors.surface,
            contentColor = DetectiveAppTheme.colors.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text(stringResource(R.string.join_room_tab_discovery)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text(stringResource(R.string.join_room_tab_manual)) }
            )
        }

        if (activeTab == 0) {
            DetectiveCard {
                Text(
                    text = stringResource(R.string.join_room_searching),
                    color = DetectiveAppTheme.colors.textSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.join_room_no_rooms),
                    color = DetectiveAppTheme.colors.textMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            // Manual Connect
            DetectiveCard {
                OutlinedTextField(
                    value = hostIp,
                    onValueChange = { hostIp = it },
                    label = { Text(stringResource(R.string.join_room_host_ip)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text(stringResource(R.string.join_room_pin)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (feedback != null) {
            Text(
                text = feedback!!,
                color = DetectiveAppTheme.colors.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DetectiveButton(
            text = stringResource(R.string.join_room_connect_btn),
            onClick = {
                viewModel.nickname.value = nickname
                viewModel.joinRoom(hostIp, pin)
                onRoomJoined()
            },
            enabled = pin.length == 4,
            isPrimary = true
        )

        DetectiveButton(
            text = stringResource(R.string.back),
            onClick = onBack,
            isPrimary = false
        )
    }
}
