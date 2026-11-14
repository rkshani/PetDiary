package com.diary.petcare.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AppLanguage(val tag: String?) { SYSTEM(null), ENGLISH("en"), URDU("ur") }

enum class WeightUnit { KG, LB }

/** Supported currencies. Amounts are always stored in the minor unit (paisa / cents). */
enum class Currency(val code: String, val symbol: String) {
    PKR("PKR", "Rs"),
    USD("USD", "$"),
    INR("INR", "₹"),
    AED("AED", "AED"),
    SAR("SAR", "SAR"),
    GBP("GBP", "£"),
    EUR("EUR", "€"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val currency: Currency = Currency.PKR,
    val onboardingDone: Boolean = false,
    val lockEnabled: Boolean = false,
)
