package com.diary.petcare.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.diary.petcare.domain.reminder.ReminderActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Handles the "Done" and "Snooze" buttons on reminder notifications. Not exported. */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, 0)
        if (id == 0L) return
        val occurrence = intent.getLongExtra(EXTRA_OCCURRENCE, 0).takeIf { it > 0 }
        val deps = ReminderEntryPoint.get(context)
        deps.notifier().cancel(id)
        val pending = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    ACTION_DONE -> deps.actions().markDone(id, occurrence)
                    ACTION_SNOOZE -> deps.scheduler().snooze(id, occurrence ?: System.currentTimeMillis(), ReminderActions.SNOOZE_MINUTES)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.diary.petcare.action.REMINDER_DONE"
        const val ACTION_SNOOZE = "com.diary.petcare.action.REMINDER_SNOOZE"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_OCCURRENCE = "occurrence"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
