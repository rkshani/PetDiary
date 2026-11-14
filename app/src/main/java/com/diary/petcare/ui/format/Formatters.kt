package com.diary.petcare.ui.format

import androidx.compose.runtime.staticCompositionLocalOf
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.model.Currency
import com.diary.petcare.domain.model.WeightUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Current settings for formatting (currency, weight unit) anywhere in the UI tree. */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

object Formatters {

    private const val GRAMS_PER_POUND = 453.59237

    // ---- Money (stored in paisa / cents) ----

    fun money(paisa: Long, currency: Currency): String {
        val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = if (paisa % 100 == 0L) 0 else 2
            maximumFractionDigits = 2
        }
        return "${currency.symbol} ${nf.format(paisa / 100.0)}"
    }

    /** "1250.5" -> 125050. Returns null for empty or bad input. */
    fun parseMoney(text: String): Long? {
        val clean = text.trim().replace(",", "")
        if (clean.isEmpty()) return null
        return runCatching {
            BigDecimal(clean).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
        }.getOrNull()?.takeIf { it >= 0 }
    }

    /** 125050 -> "1250.5" for edit fields. */
    fun moneyInput(paisa: Long): String =
        if (paisa == 0L) "" else BigDecimal(paisa).movePointLeft(2).stripTrailingZeros().toPlainString()

    // ---- Weight (stored in grams) ----

    fun weight(grams: Long, unit: WeightUnit): String {
        val df = DecimalFormat("0.##")
        return when (unit) {
            WeightUnit.KG -> if (grams < 1000) "$grams g" else "${df.format(grams / 1000.0)} kg"
            WeightUnit.LB -> "${df.format(grams / GRAMS_PER_POUND)} lb"
        }
    }

    /** Number in the chosen unit, e.g. 4.25 kg -> 4250 g. */
    fun parseWeight(text: String, unit: WeightUnit): Long? {
        val v = text.trim().replace(",", ".").toDoubleOrNull() ?: return null
        if (v <= 0) return null
        return when (unit) {
            WeightUnit.KG -> Math.round(v * 1000)
            WeightUnit.LB -> Math.round(v * GRAMS_PER_POUND)
        }
    }

    fun weightInput(grams: Long, unit: WeightUnit): String {
        val df = DecimalFormat("0.###")
        return when (unit) {
            WeightUnit.KG -> df.format(grams / 1000.0)
            WeightUnit.LB -> df.format(grams / GRAMS_PER_POUND)
        }
    }

    /** Weight value in the unit (for charts). */
    fun weightValue(grams: Long, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> grams / 1000.0
        WeightUnit.LB -> grams / GRAMS_PER_POUND
    }

    // ---- Dates ----

    fun date(millis: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))

    fun shortDate(millis: Long): String = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(millis))

    fun dateTime(millis: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))

    fun time(millis: Long): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))

    /** Minutes after midnight -> "8:30 AM" in the device style. */
    fun timeOfDay(minutes: Int): String {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
        }
        return time(c.timeInMillis)
    }

    fun monthYear(millis: Long): String =
        java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(millis))

    // The M3 date picker works in UTC midnight; the app stores local midnight.

    fun localToUtcDate(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun utcToLocalDate(utcMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
}
