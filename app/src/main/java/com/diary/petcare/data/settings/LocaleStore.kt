package com.diary.petcare.data.settings

import android.content.Context
import android.content.res.Configuration
import com.diary.petcare.core.FeatureFlags
import com.diary.petcare.domain.model.AppLanguage
import java.util.Locale

/**
 * The app language must be known before the first screen draws, so a copy is kept in
 * plain SharedPreferences (read synchronously in attachBaseContext). DataStore stays the
 * source of truth for the settings screen.
 */
object LocaleStore {
    private const val PREFS = "locale_cache"
    private const val KEY = "language"

    fun save(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, language.name).apply()
    }

    fun load(context: Context): AppLanguage {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        return AppLanguage.entries.firstOrNull { it.name == name } ?: AppLanguage.SYSTEM
    }

    /** Returns a context that uses the saved language (or the base one for "system"). */
    fun wrap(base: Context): Context {
        // v1 is English only, whatever the phone language is (also keeps the layout left-to-right).
        val tag = if (FeatureFlags.LANGUAGE_PICKER) load(base).tag ?: return base else "en"
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}
