# Pet Diary release (R8) rules.
# Room, Hilt, WorkManager, Compose, Coil and DataStore ship their own consumer rules.

# Enum names are stored in the database and in settings (PetType, ThemeMode, ...).
# Keep their names so saved data still matches after shrinking.
-keepclassmembers enum com.diary.petcare.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# WorkManager creates the reminder worker by reflection with this constructor.
-keep class com.diary.petcare.data.reminder.ReminderWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Hilt entry point used by the worker and the notification receiver.
-keep interface com.diary.petcare.data.reminder.ReminderEntryPoint { *; }

# Better crash stack traces without exposing source file names.
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile
