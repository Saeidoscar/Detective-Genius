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
fun QuestionCreationScreen(
    viewModel: GameViewModel,
    onQuestionsSubmitted: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val inputs by viewModel.questionInputs.collectAsState()
    var isSubmitted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val completedCount = inputs.count { it.trim().length in ValidationRules.MIN_QUESTION_LENGTH..ValidationRules.MAX_QUESTION_LENGTH }
    val validation = ValidationRules.validateQuestions(inputs)
    val isValid = validation is ValidationRules.QuestionValidationResult.Valid

    LaunchedEffect(view?.phase) {
        if (view?.phase == GamePhase.ANSWERING) {
            onQuestionsSubmitted()
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
            text = stringResource(R.string.qc_title),
            color = DetectiveAppTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.qc_instructions),
            color = DetectiveAppTheme.colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        // Progress
        DetectiveCard {
            Text(
                text = stringResource(R.string.qc_progress, completedCount),
                color = DetectiveAppTheme.colors.accentInvestigation,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completedCount / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = DetectiveAppTheme.colors.accentInvestigation,
                trackColor = DetectiveAppTheme.colors.surfaceHighlight
            )
        }

        // 3 Question Fields
        for (i in 0..2) {
            val qText = inputs[i]
            val length = qText.length
            val isTooShort = length > 0 && length < ValidationRules.MIN_QUESTION_LENGTH
            val isTooLong = length > ValidationRules.MAX_QUESTION_LENGTH

            DetectiveCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${i + 1}",
                        color = DetectiveAppTheme.colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$length / ${ValidationRules.MAX_QUESTION_LENGTH}",
                        color = if (isTooLong) DetectiveAppTheme.colors.error else DetectiveAppTheme.colors.textMuted,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qText,
                    onValueChange = { if (!isSubmitted) viewModel.updateQuestionInput(i, it) },
                    placeholder = { Text(stringResource(R.string.qc_placeholder)) },
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
                if (isTooShort) {
                    Text(
                        text = stringResource(R.string.qc_error_too_short),
                        color = DetectiveAppTheme.colors.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (isSubmitted) {
            Text(
                text = stringResource(R.string.qc_submitted_waiting),
                color = DetectiveAppTheme.colors.success,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            DetectiveButton(
                text = stringResource(R.string.qc_submit_btn),
                onClick = {
                    if (isValid) {
                        isSubmitted = true
                        viewModel.submitQuestions()
                    }
                },
                enabled = isValid,
                isPrimary = true
            )
        }
    }
}
