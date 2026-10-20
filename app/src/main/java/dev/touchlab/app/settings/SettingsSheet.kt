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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onReduceMotionChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("settings_sheet"),
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Keep it comfortable. Every change applies immediately.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(16.dp))

            SettingRow(
                title = "Sound effects",
                description = "Soft taps when you touch, flick and bounce.",
                checked = settings.soundEnabled,
                onCheckedChange = onSoundChanged,
                tag = "setting_sound",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.7f))
            SettingRow(
                title = "Haptic feedback",
                description = "A light tap on touches and impacts, never every frame.",
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChanged,
                tag = "setting_haptic",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.7f))
            SettingRow(
                title = "Reduce motion",
                description = "Calmer drift, softer springs, less decoration.",
                checked = settings.reduceMotion,
                onCheckedChange = onReduceMotionChanged,
                tag = "setting_reduce_motion",
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onDismiss,
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
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
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
