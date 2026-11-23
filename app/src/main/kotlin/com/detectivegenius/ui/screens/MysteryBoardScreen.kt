package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.model.GamePhase
import com.detectivegenius.designsystem.components.*
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.viewmodel.GameViewModel

@Composable
fun MysteryBoardScreen(
    viewModel: GameViewModel,
    onInspectPackages: () -> Unit,
    onOpenChat: () -> Unit,
    onEliminated: () -> Unit,
    onVictory: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val solved = view?.solvedChallenges ?: emptySet()
    val mistakes = view?.mistakes ?: 0
    val isEliminated = view?.isEliminated ?: false
    val activeChallenge by viewModel.activeChallengeForDeduction.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(isEliminated) {
        if (isEliminated) onEliminated()
    }

    LaunchedEffect(view?.phase, view?.winnerPlayerId) {
        if (view?.phase == GamePhase.FINISHED && view?.winnerPlayerId != null) {
            onVictory()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Mistakes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.mb_title),
                    color = DetectiveAppTheme.colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.mb_mistakes_count, mistakes),
                    color = if (mistakes >= 2) DetectiveAppTheme.colors.error else DetectiveAppTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            MistakeIndicator(mistakes = mistakes)
        }

        if (mistakes == 2) {
            Text(
                text = "⚠️ " + stringResource(R.string.mb_mistakes_warning),
                color = DetectiveAppTheme.colors.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Top Navigation Tabs (Evidence Archive & Comms)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DetectiveButton(
                    text = "📂 " + stringResource(R.string.mb_inspect_packages),
                    onClick = onInspectPackages,
                    isPrimary = false
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DetectiveButton(
                    text = "📡 " + stringResource(R.string.chat_title),
                    onClick = onOpenChat,
                    isPrimary = false
                )
            }
        }

        // 4 Challenges
        ChallengeCard(
            title = stringResource(R.string.mb_q1_title),
            desc = stringResource(R.string.mb_q1_desc),
            isSolved = solved.contains(ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS),
            onDeduce = { viewModel.openDeductionDialog(ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS) }
        )

        ChallengeCard(
            title = stringResource(R.string.mb_q2_title),
            desc = stringResource(R.string.mb_q2_desc),
            isSolved = solved.contains(ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER),
            onDeduce = { viewModel.openDeductionDialog(ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER) }
        )

        ChallengeCard(
            title = stringResource(R.string.mb_q3_title),
            desc = stringResource(R.string.mb_q3_desc),
            isSolved = solved.contains(ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING),
            onDeduce = { viewModel.openDeductionDialog(ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING) }
        )

        ChallengeCard(
            title = stringResource(R.string.mb_q4_title),
            desc = stringResource(R.string.mb_q4_desc),
            isSolved = solved.contains(ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING),
            onDeduce = { viewModel.openDeductionDialog(ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING) }
        )
    }

    // Deduction Dialog
    if (activeChallenge != null) {
        DeductionModalDialog(
            challengeType = activeChallenge!!,
            viewModel = viewModel,
            onDismiss = { viewModel.closeDeductionDialog() }
        )
    }
}

@Composable
fun ChallengeCard(
    title: String,
    desc: String,
    isSolved: Boolean,
    onDeduce: () -> Unit
) {
    DetectiveCard(
        borderColor = if (isSolved) DetectiveAppTheme.colors.success else DetectiveAppTheme.colors.border
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            MysteryStatusBadge(isSolved = isSolved)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = desc,
            color = DetectiveAppTheme.colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (!isSolved) {
            DetectiveButton(
                text = stringResource(R.string.mb_solve_action),
                onClick = onDeduce,
                isPrimary = true,
                modifier = Modifier.height(42.dp)
            )
        }
    }
}

@Composable
fun DeductionModalDialog(
    challengeType: ChallengeType,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val allPlayers = view?.players ?: emptyList()
    val singleSuspect by viewModel.selectedSuspectSingle.collectAsState()
    val suspectPair by viewModel.selectedSuspectPair.collectAsState()
    val feedback by viewModel.deductionFeedback.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (challengeType.isSetBased) stringResource(R.string.guess_title_pair) else stringResource(R.string.guess_title_single),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (challengeType.isSetBased) {
                    Text(
                        text = stringResource(R.string.guess_pair_hint),
                        color = DetectiveAppTheme.colors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                // Grid of players
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allPlayers) { p ->
                        val isSelected = if (!challengeType.isSetBased) {
                            singleSuspect == p.id
                        } else {
                            suspectPair.contains(p.id)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DetectiveAppTheme.colors.surfaceHighlight else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) DetectiveAppTheme.colors.accentInvestigation else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.toggleSuspectSelection(p.id) }
                                .padding(6.dp)
                        ) {
                            AvatarView(avatarId = p.avatarId, size = 42.dp, isSelected = isSelected)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = p.nickname,
                                color = DetectiveAppTheme.colors.textPrimary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (feedback != null) {
                    Text(
                        text = feedback!!,
                        color = DetectiveAppTheme.colors.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitDeduction() }) {
                Text(stringResource(R.string.guess_submit), color = DetectiveAppTheme.colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = DetectiveAppTheme.colors.textMuted)
            }
        },
        containerColor = DetectiveAppTheme.colors.surface
    )
}
