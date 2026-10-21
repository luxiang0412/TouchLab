package dev.touchlab.app.localization

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * In-app language support for English and Simplified Chinese, following the
 * system locale by default. The override is kept in a tiny SharedPreferences
 * so it can be restored synchronously in [MainActivity.attachBaseContext]
 * before any resource lookup happens.
 */
object AppLanguage {

    const val SYSTEM = "system"
    const val ZH = "zh-CN"
    const val EN = "en"

    private const val PREFS_NAME = "touchlab_language"
    private const val KEY_CODE = "code"

    /** Wraps [base] so resources resolve in the chosen language. */
    fun wrap(base: Context): Context {
        val code = current(base.applicationContext)
        if (code == SYSTEM) return base
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(code))
        return base.createConfigurationContext(configuration)
    }

    fun current(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CODE, SYSTEM) ?: SYSTEM
    }

    fun set(context: Context, code: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CODE, code)
            .apply()
    }
}