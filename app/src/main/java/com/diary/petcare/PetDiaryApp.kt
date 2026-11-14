package com.diary.petcare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * v1 has no reminder notifications. In v2 this class creates the notification channels and
 * calls ReminderSync.rescheduleAll() on start (see app/src/v2/README.md).
 */
@HiltAndroidApp
class PetDiaryApp : Application()
