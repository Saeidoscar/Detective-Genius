package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.R
import com.detectivegenius.core.model.GamePhase
import com.detectivegenius.designsystem.components.DetectiveButton
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.gameengine.rules.ValidationRules
import com.detectivegenius.viewmodel.GameViewModel

@Composable
fun AnsweringScreen(
    viewModel: GameViewModel,
    onAnswersSubmitted: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val assignedQuestions = view?.assignedQuestionsToAnswer ?: emptyList()
    val answerInputs by viewModel.answerInputs.collectAsState()
    var isSubmitted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val allAnswered = assignedQuestions.isNotEmpty() && assignedQuestions.all { q ->
        val text = answerInputs[q.id]?.trim()
        !text.isNullOrEmpty() && text.length <= ValidationRules.MAX_ANSWER_LENGTH
    }

    LaunchedEffect(view?.phase) {
        if (view?.phase == GamePhase.DISTRIBUTION || view?.phase == GamePhase.INVESTIGATION) {
            onAnswersSubmitted()
        }
    }

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
            text = stringResource(R.string.ans_title),
            color = DetectiveAppTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.ans_instructions),
            color = DetectiveAppTheme.colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        assignedQuestions.forEachIndexed { index, q ->
            val ansText = answerInputs[q.id] ?: ""
            val length = ansText.length
            val isTooLong = length > ValidationRules.MAX_ANSWER_LENGTH

            DetectiveCard {
                Text(
                    text = stringResource(R.string.ans_q_label, index + 1),
                    color = DetectiveAppTheme.colors.accentInvestigation,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = q.text,
                    color = DetectiveAppTheme.colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$length / ${ValidationRules.MAX_ANSWER_LENGTH}",
                        color = if (isTooLong) DetectiveAppTheme.colors.error else DetectiveAppTheme.colors.textMuted,
                        fontSize = 11.sp
                    )
                }
                OutlinedTextField(
                    value = ansText,
                    onValueChange = { if (!isSubmitted) viewModel.updateAnswerInput(q.id, it) },
                    placeholder = { Text(stringResource(R.string.ans_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitted,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DetectiveAppTheme.colors.textPrimary,
                        unfocusedTextColor = DetectiveAppTheme.colors.textPrimary,
                        focusedBorderColor = DetectiveAppTheme.colors.primary,
                        unfocusedBorderColor = DetectiveAppTheme.colors.border
                    )
                )
            }
        }

        if (isSubmitted) {
            Text(
                text = stringResource(R.string.ans_submitted_waiting),
                color = DetectiveAppTheme.colors.success,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            DetectiveButton(
                text = stringResource(R.string.ans_submit_btn),
                onClick = {
                    if (allAnswered) {
                        isSubmitted = true
                        viewModel.submitAnswers()
                    }
                },
                enabled = allAnswered,
                isPrimary = true
            )
        }
    }
}
