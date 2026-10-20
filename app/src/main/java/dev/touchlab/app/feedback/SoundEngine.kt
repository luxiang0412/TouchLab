package dev.touchlab.app.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import dev.touchlab.app.BuildConfig
import dev.touchlab.app.R

enum class SoundCue(
    val resourceId: Int,
    val volume: Float,
    val minIntervalMs: Long,
) {
    JellyPress(R.raw.jelly_press, 0.30f, 70L),
    JellyRelease(R.raw.jelly_release, 0.26f, 90L),
    DustTouch(R.raw.dust_touch, 0.20f, 110L),
    MarbleHit(R.raw.marble_hit, 0.22f, 90L),
    UiTap(R.raw.ui_tap, 0.16f, 50L),
}

/**
 * Small, tolerant SoundPool wrapper. If audio cannot be initialized or a
 * resource fails to load, playback is silently skipped; the toys never depend
 * on sound. Every cue is rate-limited so dense marble collisions cannot turn
 * into a buzz.
 */
class SoundEngine(context: Context) {
    private val pool: SoundPool? = runCatching {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    }.getOrNull()

    private val soundIds = IntArray(SoundCue.entries.size)
    private val lastPlayedAt = LongArray(SoundCue.entries.size)

    init {
        val appContext = context.applicationContext
        val soundPool = pool
        if (soundPool != null) {
            SoundCue.entries.forEachIndexed { index, cue ->
                soundIds[index] = runCatching {
                    soundPool.load(appContext, cue.resourceId, 1)
                }.getOrDefault(0)
            }
        }
    }

    fun play(cue: SoundCue, enabled: Boolean = true) {
        if (!enabled) return
        val soundPool = pool ?: return
        val id = soundIds[cue.ordinal]
        if (id <= 0) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastPlayedAt[cue.ordinal] < cue.minIntervalMs) return
        lastPlayedAt[cue.ordinal] = now
        if (BuildConfig.DEBUG) Log.d("TouchLabSound", "play ${cue.name}")
        runCatching {
            soundPool.play(id, cue.volume, cue.volume, 1, 0, 1f)
        }
    }

    fun release() {
        runCatching { pool?.release() }
    }
}
