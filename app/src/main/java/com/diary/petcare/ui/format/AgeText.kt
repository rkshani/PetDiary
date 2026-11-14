package com.diary.petcare.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils

/** "2 years 3 months", "5 months" or "Newborn". */
@Composable
fun ageText(birthDate: Long): String {
    val (years, months) = DateTimeUtils.age(birthDate)
    val y = if (years > 0) pluralStringResource(R.plurals.age_years, years, years) else null
    val m = if (months > 0) pluralStringResource(R.plurals.age_months, months, months) else null
    return listOfNotNull(y, m).joinToString(" ").ifEmpty { stringResource(R.string.age_newborn) }
}

/** "Today", "Tomorrow", "In 3 days", "Overdue". */
@Composable
fun dueText(millis: Long): String {
    val days = DateTimeUtils.daysFromToday(millis)
    return when {
        days < 0 -> stringResource(R.string.overdue)
        days == 0 -> stringResource(R.string.today)
        days == 1 -> stringResource(R.string.tomorrow)
        else -> stringResource(R.string.in_days, days)
    }
}
