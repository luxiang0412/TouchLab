package dev.touchlab.app.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.touchlab.app.R
import dev.touchlab.app.localization.AppLanguage
import dev.touchlab.app.ui.components.SmallTogglePill
import dev.touchlab.app.ui.theme.Background
import dev.touchlab.app.ui.theme.JellyMint
import dev.touchlab.app.ui.theme.OutlineSoft
import dev.touchlab.app.ui.theme.Surface
import dev.touchlab.app.ui.theme.SurfaceRaised
import dev.touchlab.app.ui.theme.TextPrimary
import dev.touchlab.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    languageCode: String,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onReduceMotionChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onUiFeedback: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        contentColor = TextPrimary,
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle(
                color = TextSecondary.copy(alpha = 0.5f),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("settings_sheet"),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(16.dp))

            SettingRow(
                title = stringResource(R.string.setting_sound),
                description = stringResource(R.string.setting_sound_desc),
                checked = settings.soundEnabled,
                onCheckedChange = {
                    onUiFeedback()
                    onSoundChanged(it)
                },
                tag = "setting_sound",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.7f))
            SettingRow(
                title = stringResource(R.string.setting_haptic),
                description = stringResource(R.string.setting_haptic_desc),
                checked = settings.hapticsEnabled,
                onCheckedChange = {
                    onUiFeedback()
                    onHapticsChanged(it)
                },
                tag = "setting_haptic",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.7f))
            SettingRow(
                title = stringResource(R.string.setting_motion),
                description = stringResource(R.string.setting_motion_desc),
                checked = settings.reduceMotion,
                onCheckedChange = {
                    onUiFeedback()
                    onReduceMotionChanged(it)
                },
                tag = "setting_reduce_motion",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.7f))
            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                Text(
                    text = stringResource(R.string.setting_language),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.setting_language_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LanguagePill(
                        code = AppLanguage.SYSTEM,
                        current = languageCode,
                        onSelect = {
                            onUiFeedback()
                            onLanguageChanged(it)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    LanguagePill(
                        code = AppLanguage.ZH,
                        current = languageCode,
                        onSelect = {
                            onUiFeedback()
                            onLanguageChanged(it)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    LanguagePill(
                        code = AppLanguage.EN,
                        current = languageCode,
                        onSelect = {
                            onUiFeedback()
                            onLanguageChanged(it)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onUiFeedback()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("settings_done"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JellyMint,
                    contentColor = Background,
                ),
            ) {
                Text(
                    text = stringResource(R.string.settings_done),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun LanguagePill(
    code: String,
    current: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (code) {
        AppLanguage.ZH -> stringResource(R.string.lang_zh)
        AppLanguage.EN -> stringResource(R.string.lang_en)
        else -> stringResource(R.string.lang_system)
    }
    val tag = when (code) {
        AppLanguage.ZH -> "setting_language_zh"
        AppLanguage.EN -> "setting_language_en"
        else -> "setting_language_system"
    }
    SmallTogglePill(
        label = label,
        checked = code == current,
        accent = JellyMint,
        onToggle = { onSelect(code) },
        tag = tag,
        modifier = modifier,
    )
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .size(width = 52.dp, height = 32.dp)
                .testTag(tag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Background,
                checkedTrackColor = JellyMint,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceRaised,
                uncheckedBorderColor = OutlineSoft,
            ),
        )
    }
}