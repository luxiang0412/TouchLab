package dev.touchlab.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.touchlab.app.R
import dev.touchlab.app.ui.Toy
import dev.touchlab.app.ui.components.AmbientBackdrop
import dev.touchlab.app.ui.components.LabCard
import dev.touchlab.app.ui.theme.Background
import dev.touchlab.app.ui.theme.JellyMint
import dev.touchlab.app.ui.theme.TextPrimary
import dev.touchlab.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    reduceMotion: Boolean,
    onOpenToy: (Toy) -> Unit,
    onOpenSettings: () -> Unit,
    onUiFeedback: () -> Unit = {},
    modifier: Modifier = Modifier,
    versionName: String,
) {
    Box(modifier = modifier.fillMaxSize().background(Background)) {
        AmbientBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .testTag("home_screen"),
        ) {
            Spacer(Modifier.height(20.dp))
            BrandWordmark()
            Spacer(Modifier.height(30.dp))
            Text(
                text = stringResource(R.string.home_headline),
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_sub),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
            Spacer(Modifier.height(26.dp))

            Toy.entries.forEachIndexed { index, toy ->
                if (index > 0) Spacer(Modifier.height(16.dp))
                LabCard(
                    toy = toy,
                    reduceMotion = reduceMotion,
                    onClick = {
                        onUiFeedback()
                        onOpenToy(toy)
                    },
                )
            }

            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = {
                        onUiFeedback()
                        onOpenSettings()
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("settings_button"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TextSecondary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                    )
                }
                Text(
                    text = stringResource(R.string.version_prefix, versionName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.home_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.72f),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BrandWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(JellyMint),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
                color = TextPrimary,
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
    }
}
