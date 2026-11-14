package com.diary.petcare

import android.app.Application
import com.diary.petcare.data.reminder.ReminderNotifier
import com.diary.petcare.domain.reminder.ReminderSync
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PetDiaryApp : Application() {

    @Inject lateinit var notifier: ReminderNotifier
    @Inject lateinit var reminderSync: ReminderSync

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notifier.createChannels()
        // Heals the timer if jobs were lost (e.g. app data restored); cheap because jobs are unique.
        appScope.launch { runCatching { reminderSync.rescheduleAll() } }
    }
}
