package dev.touchlab.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.touchlab.app.R
import dev.touchlab.app.ui.Toy
import dev.touchlab.app.ui.theme.Background
import dev.touchlab.app.ui.theme.OutlineSoft
import dev.touchlab.app.ui.theme.SurfaceRaised
import dev.touchlab.app.ui.theme.TextPrimary
import dev.touchlab.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PlayScaffold(
    toy: Toy,
    paused: Boolean,
    reduceMotion: Boolean,
    onBack: () -> Unit,
    onTogglePause: () -> Unit,
    onReset: () -> Unit,
    onSettings: () -> Unit,
    extraControls: @Composable ColumnScope.() -> Unit = {},
    canvas: @Composable (notifyInteraction: () -> Unit) -> Unit,
) {
    var hintVisible by rememberSaveable(toy) { mutableStateOf(true) }
    var interacted by remember { mutableStateOf(false) }

    LaunchedEffect(toy) {
        delay(5200)
        hintVisible = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("play_screen"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("play_back"),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back to toys",
                    tint = TextPrimary,
                )
            }
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(toy.accent),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = toy.title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = CircleShape,
                color = SurfaceRaised,
                border = BorderStroke(1.dp, OutlineSoft.copy(alpha = 0.6f)),
            ) {
                Text(
                    text = toy.index,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            AmbientBackdrop()
            Box(modifier = Modifier.fillMaxSize()) {
                canvas {
                    if (!interacted) {
                        interacted = true
                        hintVisible = false
                    }
                }
            }

            HintOverlay(
                visible = hintVisible && !paused,
                text = toy.hint,
                accent = toy.accent,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            )

            if (paused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background.copy(alpha = 0.52f))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent().changes.forEach { it.consume() }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SurfaceRaised),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_pause),
                                contentDescription = null,
                                tint = toy.accent,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Paused",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "The simulation is resting.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 16.dp),
        ) {
            Text(
                text = if (paused) "Paused — nothing is moving." else toy.accessibilityHint,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PlayPillButton(
                    icon = if (paused) R.drawable.ic_play else R.drawable.ic_pause,
                    label = if (paused) "Resume" else "Pause",
                    accent = toy.accent,
                    filled = false,
                    onClick = onTogglePause,
                    tag = "play_pause",
                    modifier = Modifier.weight(1f),
                )
                PlayPillButton(
                    icon = R.drawable.ic_reset,
                    label = "Reset",
                    accent = toy.accent,
                    filled = true,
                    onClick = onReset,
                    tag = "play_reset",
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(SurfaceRaised)
                        .testTag("play_settings"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = "Open settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            extraControls()
        }
    }
}

@Composable
private fun HintOverlay(
    visible: Boolean,
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320)),
        exit = fadeOut(tween(220)),
        modifier = modifier,
    ) {
        HintChip(text = text, accent = accent)
    }
}

@Composable
private fun PlayPillButton(
    icon: Int,
    label: String,
    accent: Color,
    filled: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(50.dp)
                .testTag(tag),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = Background,
            ),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(50.dp)
                .testTag(tag),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, OutlineSoft),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary,
                containerColor = Color.Transparent,
            ),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = accent,
            )
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun HintChip(text: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.semantics { contentDescription = "Hint: $text" },
        shape = CircleShape,
        color = SurfaceRaised.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
    }
}

@Composable
fun SmallTogglePill(
    label: String,
    checked: Boolean,
    accent: Color,
    onToggle: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .testTag(tag),
        onClick = onToggle,
        shape = CircleShape,
        color = if (checked) accent.copy(alpha = 0.18f) else SurfaceRaised,
        border = BorderStroke(
            1.dp,
            if (checked) accent.copy(alpha = 0.55f) else OutlineSoft,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (checked) accent else TextSecondary),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (checked) TextPrimary else TextSecondary,
            )
        }
    }
}

