# Parked for version 2

Code in this folder is **not compiled** into the v1 APK. It is kept so the features can come back.
The switches are in `app/src/main/java/com/diary/petcare/core/FeatureFlags.kt`.

## Reminder notifications (`java/.../data/reminder`, `di/ReminderModule.kt`, `ui/reminders`)

To turn reminders back on:

1. In `app/build.gradle.kts`, un-comment the `work-runtime-ktx` dependency and the
   `sourceSets` line that adds `src/v2/java`.
2. Delete `app/src/main/java/com/diary/petcare/di/NoReminderModule.kt` and
   `data/reminder/NoOpReminderScheduler.kt`.
3. Set `FeatureFlags.REMINDERS = true` (shows the "Remind me" switches again).
4. In `AndroidManifest.xml`, add back the `POST_NOTIFICATIONS` permission (delete its
   `tools:node="remove"` lines, and the WorkManager ones) and the receiver:
   `<receiver android:name=".data.reminder.ReminderActionReceiver" android:exported="false" />`
5. Add the Reminders tab back in `TopLevelDestination`, the route in `PetDiaryNavHost`
   (`RemindersScreen`), `AskNotificationPermissionOnce()` in `PetDiaryAppUi`, and
   `notifier.createChannels()` + `reminderSync.rescheduleAll()` in `PetDiaryApp`.
6. In `BackupManager.restore`, cancel the old jobs before swapping the database:
   `WorkManager.getInstance(context).cancelAllWorkByTag("reminders")`.

## Dark theme (`res/values-night`)

Move `res/values-night` back to `app/src/main/res` and set `FeatureFlags.DARK_MODE = true`.

## Urdu

The Urdu strings are still in `app/src/main/res/values-ur`. Add `"ur"` back to
`localeFilters` in `app/build.gradle.kts` and set `FeatureFlags.LANGUAGE_PICKER = true`.

## Onboarding and app lock

Set `FeatureFlags.ONBOARDING` / `FeatureFlags.APP_LOCK` to `true`.
