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
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()
    val soundEnabled by viewModel.soundAndHapticsManager.soundEnabled.collectAsState()
    val musicEnabled by viewModel.soundAndHapticsManager.musicEnabled.collectAsState()
    val hapticsEnabled by viewModel.soundAndHapticsManager.hapticsEnabled.collectAsState()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back), color = DetectiveAppTheme.colors.primary)
            }
        }

        // Language Selection
        DetectiveCard {
            Text(
                text = stringResource(R.string.settings_language),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.currentLanguage.value = "en" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (language == "en") DetectiveAppTheme.colors.primary else DetectiveAppTheme.colors.surfaceElevated
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.settings_lang_en))
                }
                Button(
                    onClick = { viewModel.currentLanguage.value = "fa" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (language == "fa") DetectiveAppTheme.colors.primary else DetectiveAppTheme.colors.surfaceElevated
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.settings_lang_fa))
                }
            }
        }

        // Theme Selection
        DetectiveCard {
            Text(
                text = stringResource(R.string.settings_theme),
                color = DetectiveAppTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.isDarkTheme.value = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) DetectiveAppTheme.colors.primary else DetectiveAppTheme.colors.surfaceElevated
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.settings_theme_dark))
                }
                Button(
                    onClick = { viewModel.isDarkTheme.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isDark) DetectiveAppTheme.colors.primary else DetectiveAppTheme.colors.surfaceElevated
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.settings_theme_light))
                }
            }
        }

        // Toggles: Sound, Music, Haptics, Reduced Motion
        DetectiveCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_sound_fx), color = DetectiveAppTheme.colors.textPrimary)
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.soundAndHapticsManager.setSoundEnabled(it) }
                )
            }
            Divider(color = DetectiveAppTheme.colors.divider, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_music), color = DetectiveAppTheme.colors.textPrimary)
                Switch(
                    checked = musicEnabled,
                    onCheckedChange = { viewModel.soundAndHapticsManager.setMusicEnabled(it) }
                )
            }
            Divider(color = DetectiveAppTheme.colors.divider, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_haptics), color = DetectiveAppTheme.colors.textPrimary)
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { viewModel.soundAndHapticsManager.setHapticsEnabled(it) }
                )
            }
            Divider(color = DetectiveAppTheme.colors.divider, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_reduced_motion), color = DetectiveAppTheme.colors.textPrimary)
                Switch(
                    checked = reducedMotion,
                    onCheckedChange = { viewModel.reducedMotion.value = it }
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_version, "1.0.0"),
            color = DetectiveAppTheme.colors.textMuted,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        DetectiveButton(
            text = stringResource(R.string.back),
            onClick = onBack,
            isPrimary = false
        )
    }
}
