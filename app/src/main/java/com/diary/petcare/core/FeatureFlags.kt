package com.diary.petcare.core

/**
 * Switches for features that are written but not shipped in version 1.
 * Turn one on to bring that feature back. See app/src/v2/README.md.
 */
object FeatureFlags {
    /** Reminder notifications. The code lives in app/src/v2 and is not compiled in v1. */
    const val REMINDERS = false

    /** The 3-screen onboarding shown on first launch. */
    const val ONBOARDING = false

    /** Dark theme and the theme picker. When off, the app is always light. */
    const val DARK_MODE = false

    /** Language picker (English / Urdu). When off, the app is always English. */
    const val LANGUAGE_PICKER = false

    /** PIN app lock. */
    const val APP_LOCK = false
}
