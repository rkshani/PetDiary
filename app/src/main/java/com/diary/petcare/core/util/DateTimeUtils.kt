package com.diary.petcare.core.util

import com.diary.petcare.domain.model.DAY_MILLIS
import java.util.Calendar

/** Small date helpers. All values are epoch millis in the device time zone. */
object DateTimeUtils {

    fun now(): Long = System.currentTimeMillis()

    fun startOfDay(millis: Long = now()): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfDay(millis: Long = now()): Long = startOfDay(millis) + DAY_MILLIS - 1

    /** The given day at [minutesOfDay] (0..1439). */
    fun atTimeOfDay(dayMillis: Long, minutesOfDay: Int): Long = Calendar.getInstance().apply {
        timeInMillis = startOfDay(dayMillis)
        set(Calendar.HOUR_OF_DAY, minutesOfDay / 60)
        set(Calendar.MINUTE, minutesOfDay % 60)
    }.timeInMillis

    /** First moment at [minutesOfDay] that is after [from]. */
    fun nextOccurrence(minutesOfDay: Int, from: Long = now()): Long {
        var t = atTimeOfDay(from, minutesOfDay)
        while (t <= from) t = atTimeOfDay(t + DAY_MILLIS, minutesOfDay)
        return t
    }

    fun minutesOfDay(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    fun startOfMonth(millis: Long = now()): Long = Calendar.getInstance().apply {
        timeInMillis = startOfDay(millis)
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    fun addMonths(millis: Long, months: Int): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.MONTH, months)
    }.timeInMillis

    fun addDays(millis: Long, days: Int): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.DAY_OF_YEAR, days)
    }.timeInMillis

    /** Whole days from today to [millis]. Negative when it is in the past. */
    fun daysFromToday(millis: Long): Int =
        ((startOfDay(millis) - startOfDay()) / DAY_MILLIS).toInt()

    /** Age as (years, months) between [birth] and [at]. */
    fun age(birth: Long, at: Long = now()): Pair<Int, Int> {
        val b = Calendar.getInstance().apply { timeInMillis = birth }
        val n = Calendar.getInstance().apply { timeInMillis = at }
        var months = (n.get(Calendar.YEAR) - b.get(Calendar.YEAR)) * 12 +
            (n.get(Calendar.MONTH) - b.get(Calendar.MONTH))
        if (n.get(Calendar.DAY_OF_MONTH) < b.get(Calendar.DAY_OF_MONTH)) months--
        if (months < 0) months = 0
        return months / 12 to months % 12
    }
}
