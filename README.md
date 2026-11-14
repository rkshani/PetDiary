# Pet Diary: Pet Care Log

An offline pet care app for Android. Package: `com.diary.petcare`.

Track cats, dogs, birds, parrots, hens, rabbits, fish and more: feeding schedules and logs,
vaccines (with ready-made lists), medicine and deworming courses, vet visits, weight with a
growth chart, grooming, expenses with monthly totals, notes, a photo gallery and a life
timeline. Reminders use WorkManager notifications with Done and Snooze. A health report PDF
can be shared with the vet, and a backup .zip can be saved and restored with the Storage
Access Framework.

- Fully offline: no INTERNET permission, no account.
- English and Urdu (RTL), light and dark theme.
- Money is stored in paisa (`Long`), weight in grams (`Long`), dates as epoch millis.

## Stack

Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture (`core`, `data`, `domain`,
`ui`, `di`), Hilt, Room (KSP), Coroutines/Flow, Navigation Compose, DataStore, WorkManager,
Coil (local images), core-splashscreen.

## Build

Open the project in Android Studio and run the `app` configuration. `minSdk 24`.

Release signing: copy `keystore.properties.example` to `keystore.properties` and fill it
in. The file and any `.jks` keystore are git-ignored. Without it, the release build is unsigned.

Low-RAM PC: `gradle.properties` already uses one worker, in-process Kotlin compilation and
a 1.5 GB heap.
