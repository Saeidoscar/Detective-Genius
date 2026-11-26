package com.detectivegenius.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.detectivegenius.designsystem.components.DetectiveButton
import com.detectivegenius.designsystem.components.DetectiveCard
import com.detectivegenius.designsystem.theme.DetectiveAppTheme
import com.detectivegenius.viewmodel.GameViewModel

@Composable
fun PackageViewerScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val view by viewModel.clientView.collectAsState()
    val packages = view?.receivedPackages ?: emptyList()
    var selectedPackageIndex by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetectiveAppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.pv_title),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back), color = DetectiveAppTheme.colors.primary)
            }
        }

        // Package Tabs (Only show if multiple packages received)
        if (packages.size > 1) {
            TabRow(
                selectedTabIndex = selectedPackageIndex,
                containerColor = DetectiveAppTheme.colors.surface,
                contentColor = DetectiveAppTheme.colors.primary
            ) {
                Tab(
                    selected = selectedPackageIndex == 0,
                    onClick = { selectedPackageIndex = 0 },
                    text = { Text(stringResource(R.string.pv_package_1)) }
                )
                Tab(
                    selected = selectedPackageIndex == 1,
                    onClick = { selectedPackageIndex = 1 },
                    text = { Text(stringResource(R.string.pv_package_2)) }
                )
            }
        }

        // Classified Redacted Notice
        DetectiveCard(
            backgroundColor = DetectiveAppTheme.colors.surfaceElevated,
            borderColor = DetectiveAppTheme.colors.accentInvestigation
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔒", fontSize = 18.sp)
                Text(
                    text = stringResource(R.string.pv_sealed_header),
                    color = DetectiveAppTheme.colors.accentInvestigation,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        val currentPackage = packages.getOrNull(selectedPackageIndex)

        if (currentPackage != null) {
            currentPackage.questions.forEachIndexed { idx, qText ->
                val aText = currentPackage.answers.getOrNull(idx) ?: "No answer recorded"
                DetectiveCard {
                    Text(
                        text = stringResource(R.string.pv_qa_pair, idx + 1),
                        color = DetectiveAppTheme.colors.accentInvestigation,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Q: $qText",
                        color = DetectiveAppTheme.colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A: $aText",
                        color = DetectiveAppTheme.colors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            DetectiveCard {
                Text(
                    text = "Awaiting package delivery...",
                    color = DetectiveAppTheme.colors.textMuted,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        DetectiveButton(
            text = stringResource(R.string.back),
            onClick = onBack,
            isPrimary = false
        )
    }
}
