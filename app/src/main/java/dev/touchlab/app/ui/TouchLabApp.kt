package dev.touchlab.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.touchlab.app.feature.home.HomeScreen
import dev.touchlab.app.feature.play.PlayShell
import dev.touchlab.app.settings.Settings
import dev.touchlab.app.settings.SettingsRepository
import dev.touchlab.app.settings.SettingsSheet
import dev.touchlab.app.ui.theme.Background
import kotlinx.coroutines.launch

@Composable
fun TouchLabApp(versionName: String) {
    val context = LocalContext.current
    val repository = remember(context) { SettingsRepository(context) }
    val scope = rememberCoroutineScope()
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = Settings())

    val systemReduceMotion = remember(context) {
        android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    val reduceMotion = settings.reduceMotion || systemReduceMotion

    var openToyName by rememberSaveable { mutableStateOf<String?>(null) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    val openToy = openToyName?.let { name -> Toy.entries.firstOrNull { it.name == name } }

    BackHandler(enabled = openToy != null && !settingsOpen) {
        openToyName = null
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Crossfade(
            targetState = openToy,
            animationSpec = tween(durationMillis = if (reduceMotion) 0 else 220),
            label = "screenTransition",
        ) { toy ->
            if (toy == null) {
                HomeScreen(
                    reduceMotion = reduceMotion,
                    onOpenToy = { openToyName = it.name },
                    onOpenSettings = { settingsOpen = true },
                    versionName = versionName,
                )
            } else {
                PlayShell(
                    toy = toy,
                    settings = settings.copy(reduceMotion = reduceMotion),
                    externallyPaused = settingsOpen,
                    onBack = { openToyName = null },
                    onOpenSettings = { settingsOpen = true },
                )
            }
        }
    }

    if (settingsOpen) {
        SettingsSheet(
            settings = settings,
            onSoundChanged = { enabled ->
                scope.launch { repository.setSoundEnabled(enabled) }
            },
            onHapticsChanged = { enabled ->
                scope.launch { repository.setHapticsEnabled(enabled) }
            },
            onReduceMotionChanged = { enabled ->
                scope.launch { repository.setReduceMotion(enabled) }
            },
            onDismiss = { settingsOpen = false },
        )
    }
}
