package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detectivegenius.R
import com.detectivegenius.designsystem.components.DetectiveButton
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "📜",
            fontSize = 44.sp
        )
        Text(
            text = stringResource(R.string.onboarding_title_1),
            color = DetectiveAppTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        DetectiveCard {
            Text(
                text = "1. " + stringResource(R.string.onboarding_title_1),
                color = DetectiveAppTheme.colors.accentInvestigation,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_desc_1),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        DetectiveCard {
            Text(
                text = "2. " + stringResource(R.string.onboarding_title_2),
                color = DetectiveAppTheme.colors.accentInvestigation,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_desc_2),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        DetectiveCard {
            Text(
                text = "3. " + stringResource(R.string.onboarding_title_3),
                color = DetectiveAppTheme.colors.accentInvestigation,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_desc_3),
                color = DetectiveAppTheme.colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetectiveButton(
            text = stringResource(R.string.onboarding_button_start),
            onClick = onFinishOnboarding,
            isPrimary = true
        )
    }
}
