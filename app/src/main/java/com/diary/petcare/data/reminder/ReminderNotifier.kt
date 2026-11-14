package com.diary.petcare.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.diary.petcare.MainActivity
import com.diary.petcare.R
import com.diary.petcare.domain.model.GroomingType
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.ui.components.label
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Builds and shows reminder notifications with "Done" and "Snooze" buttons. */
@Singleton
class ReminderNotifier @Inject constructor(@ApplicationContext private val context: Context) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(CH_FEEDING, context.getString(R.string.channel_feeding), NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CH_MEDICINE, context.getString(R.string.channel_medicine), NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CH_HEALTH, context.getString(R.string.channel_health), NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CH_GROOMING, context.getString(R.string.channel_grooming), NotificationManager.IMPORTANCE_DEFAULT),
        )
        nm.createNotificationChannels(channels)
    }

    fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Shows the reminder for the dose/meal/visit planned at [occurrenceAt]. */
    fun show(reminder: Reminder, petName: String, occurrenceAt: Long) {
        if (!canNotify()) return
        val title = context.getString(titleRes(reminder.type), petName)
        val subject = when (reminder.type) {
            ReminderType.GROOMING -> runCatching { context.getString(GroomingType.valueOf(reminder.title).label) }
                .getOrDefault(reminder.title)
            else -> reminder.title
        }
        val text = listOf(subject, reminder.message).filter { it.isNotBlank() }.joinToString(" • ")

        val open = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channelFor(reminder.type))
            .setSmallIcon(R.drawable.ic_stat_paw)
            .setColor(ContextCompat.getColor(context, R.color.brand_peach))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, context.getString(R.string.notif_done), action(ReminderActionReceiver.ACTION_DONE, reminder.id, occurrenceAt))
            .addAction(0, context.getString(R.string.notif_snooze), action(ReminderActionReceiver.ACTION_SNOOZE, reminder.id, occurrenceAt))
            .build()
        try {
            NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission was taken away between the check and the call.
        }
    }

    fun cancel(reminderId: Long) = NotificationManagerCompat.from(context).cancel(reminderId.toInt())

    private fun action(action: String, reminderId: Long, occurrenceAt: Long): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderActionReceiver.EXTRA_ID, reminderId)
            putExtra(ReminderActionReceiver.EXTRA_OCCURRENCE, occurrenceAt)
        }
        val code = (reminderId * 2 + if (action == ReminderActionReceiver.ACTION_DONE) 0 else 1).toInt()
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun titleRes(type: ReminderType) = when (type) {
        ReminderType.FEEDING -> R.string.notif_title_feeding
        ReminderType.MEDICINE -> R.string.notif_title_medicine
        ReminderType.VACCINE -> R.string.notif_title_vaccine
        ReminderType.VET -> R.string.notif_title_vet
        ReminderType.GROOMING -> R.string.notif_title_grooming
        ReminderType.CUSTOM -> R.string.notif_title_custom
    }

    private fun channelFor(type: ReminderType) = when (type) {
        ReminderType.FEEDING -> CH_FEEDING
        ReminderType.MEDICINE -> CH_MEDICINE
        ReminderType.VACCINE, ReminderType.VET, ReminderType.CUSTOM -> CH_HEALTH
        ReminderType.GROOMING -> CH_GROOMING
    }

    companion object {
        const val CH_FEEDING = "feeding"
        const val CH_MEDICINE = "medicine"
        const val CH_HEALTH = "health"
        const val CH_GROOMING = "grooming"
    }
}
