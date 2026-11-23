package com.detectivegenius.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.designsystem.avatar.AvatarDefinition
import com.detectivegenius.designsystem.avatar.AvatarRegistry
import com.detectivegenius.designsystem.avatar.AvatarState
import com.detectivegenius.designsystem.theme.DetectiveAppTheme

@Composable
fun AvatarView(
    avatarId: String,
    state: AvatarState = AvatarState.IDLE,
    size: Dp = 64.dp,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val def: AvatarDefinition = AvatarRegistry.findById(avatarId)

    val scale by animateFloatAsState(
        targetValue = when (state) {
            AvatarState.SELECTED -> 1.1f
            AvatarState.TALKING -> 1.05f
            AvatarState.VICTORY -> 1.15f
            AvatarState.ELIMINATED -> 0.9f
            else -> 1f
        },
        label = "avatarScale"
    )

    val borderColor = when {
        state == AvatarState.ELIMINATED -> DetectiveAppTheme.colors.error
        state == AvatarState.VICTORY -> Color(0xFFFFD700)
        isSelected || state == AvatarState.SELECTED -> DetectiveAppTheme.colors.accentInvestigation
        else -> DetectiveAppTheme.colors.border
    }

    val backgroundBrush = if (state == AvatarState.ELIMINATED) {
        Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
    } else {
        Brush.linearGradient(listOf(Color(def.primaryColor), Color(def.secondaryColor)))
    }

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(2.dp, borderColor, CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = def.emoji,
            fontSize = (size.value * 0.45f).sp
        )

        if (state == AvatarState.ELIMINATED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.Red, fontSize = (size.value * 0.4f).sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetectiveCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DetectiveAppTheme.colors.surface,
    borderColor: Color = DetectiveAppTheme.colors.border,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun DetectiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    val containerColor = if (isPrimary) {
        DetectiveAppTheme.colors.primary
    } else {
        DetectiveAppTheme.colors.surfaceElevated
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = if (isPrimary) Color.White else DetectiveAppTheme.colors.textPrimary,
            disabledContainerColor = DetectiveAppTheme.colors.disabled,
            disabledContentColor = DetectiveAppTheme.colors.textMuted
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MistakeIndicator(
    mistakes: Int,
    maxMistakes: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxMistakes) { index ->
            val isConsumed = index < mistakes
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConsumed) DetectiveAppTheme.colors.error
                        else DetectiveAppTheme.colors.surfaceElevated
                    )
                    .border(
                        1.dp,
                        if (isConsumed) DetectiveAppTheme.colors.error
                        else DetectiveAppTheme.colors.border,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun MysteryStatusBadge(
    isSolved: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isSolved) DetectiveAppTheme.colors.success else DetectiveAppTheme.colors.surfaceHighlight
    val text = if (isSolved) "SOLVED" else "UNSOLVED"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
