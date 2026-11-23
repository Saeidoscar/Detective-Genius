package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.R
import com.detectivegenius.designsystem.components.AvatarView
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val allPlayers = view?.players ?: emptyList()
    val otherPlayers = allPlayers.filter { it.id != view?.playerId }

    val activeTab by viewModel.activeChatTab.collectAsState()
    val activePartnerId by viewModel.activePrivatePartnerId.collectAsState()
    val groupMsgs by viewModel.groupChat.collectAsState()
    val privateMap by viewModel.privateChats.collectAsState()
    val chatInput by viewModel.chatInputText.collectAsState()

    val currentMessages = if (activeTab == 0) {
        groupMsgs
    } else {
        if (activePartnerId != null) privateMap[activePartnerId] ?: emptyList() else emptyList()
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.chat_title),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back), color = DetectiveAppTheme.colors.primary)
            }
        }

        // Tabs: Room Discussion (Group) vs Private Wire
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DetectiveAppTheme.colors.surface,
            contentColor = DetectiveAppTheme.colors.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { viewModel.activeChatTab.value = 0 },
                text = { Text(stringResource(R.string.chat_tab_group)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = {
                    viewModel.activeChatTab.value = 1
                    if (viewModel.activePrivatePartnerId.value == null && otherPlayers.isNotEmpty()) {
                        viewModel.activePrivatePartnerId.value = otherPlayers.first().id
                    }
                },
                text = { Text(stringResource(R.string.chat_tab_private)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // In Private tab: Partner picker row
        if (activeTab == 1) {
            Text(
                text = stringResource(R.string.chat_select_partner),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(otherPlayers) { p ->
                    val isSelected = p.id == activePartnerId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DetectiveAppTheme.colors.surfaceHighlight else DetectiveAppTheme.colors.surface)
                            .clickable { viewModel.activePrivatePartnerId.value = p.id }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AvatarView(avatarId = p.avatarId, size = 28.dp)
                        Text(
                            text = p.nickname,
                            color = if (isSelected) DetectiveAppTheme.colors.accentInvestigation else DetectiveAppTheme.colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🔒 " + stringResource(R.string.chat_private_notice),
                color = DetectiveAppTheme.colors.textMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentMessages) { msg ->
                val isMe = msg.senderId == view?.playerId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        AvatarView(avatarId = msg.senderAvatarId, size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        if (!isMe) {
                            Text(
                                text = msg.senderNickname,
                                color = DetectiveAppTheme.colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isMe) DetectiveAppTheme.colors.primary
                                    else DetectiveAppTheme.colors.surfaceElevated
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isMe) Color.White else DetectiveAppTheme.colors.textPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = timeFormat.format(Date(msg.timestamp)),
                            color = DetectiveAppTheme.colors.textMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Input bar
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.chatInputText.value = it },
                placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DetectiveAppTheme.colors.textPrimary,
                    unfocusedTextColor = DetectiveAppTheme.colors.textPrimary,
                    focusedBorderColor = DetectiveAppTheme.colors.primary,
                    unfocusedBorderColor = DetectiveAppTheme.colors.border
                )
            )

            Button(
                onClick = { viewModel.sendChatMessage() },
                enabled = chatInput.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DetectiveAppTheme.colors.primary)
            ) {
                Text(stringResource(R.string.chat_send), color = Color.White)
            }
        }
    }
}
