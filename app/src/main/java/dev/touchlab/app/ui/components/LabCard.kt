package dev.touchlab.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.touchlab.app.R
import dev.touchlab.app.ui.Toy
import dev.touchlab.app.ui.theme.Surface
import dev.touchlab.app.ui.theme.SurfaceRaised
import dev.touchlab.app.ui.theme.TextPrimary
import dev.touchlab.app.ui.theme.TextSecondary

@Composable
fun LabCard(
    toy: Toy,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val fontScale = LocalDensity.current.fontScale
    val cardHeight = (168f + (fontScale - 1f) * 58f).coerceIn(168f, 232f)
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.98f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "labCardScale",
    )
    val indication = LocalIndication.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("home_${toy.name.lowercase()}_card")
            .semantics { contentDescription = "Open ${toy.title}" }
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                role = Role.Button,
                onClickLabel = "Open ${toy.title}",
                onClick = onClick,
            ),
        shape = RoundedCornerShape(28.dp),
        color = Surface,
        contentColor = TextPrimary,
        border = BorderStroke(1.dp, toy.accent.copy(alpha = 0.20f)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CardSheen(modifier = Modifier.fillMaxSize(), accent = toy.accent)
            IllustrationFor(toy = toy, modifier = Modifier.fillMaxHeight().fillMaxWidth(0.48f).align(Alignment.CenterEnd))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.62f)
                    .padding(start = 22.dp, top = 24.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = toy.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = toy.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = toy.number,
                style = MaterialTheme.typography.labelMedium,
                color = toy.accent.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 22.dp, end = 22.dp),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceRaised)
                    .semantics { contentDescription = "Open ${toy.title}" },
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides toy.accent) {
                    Icon(
                        painter = painterResource(R.drawable.ic_forward),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun IllustrationFor(toy: Toy, modifier: Modifier) {
    when (toy) {
        Toy.Jelly -> JellyIllustration(modifier)
        Toy.Dust -> DustIllustration(modifier)
        Toy.Marbles -> MarblesIllustration(modifier)
    }
}
