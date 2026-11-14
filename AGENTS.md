# AGENTS.md — Pet Diary (for Codex)

**App:** Pet Diary: Pet Care Log
**Package:** `com.diary.petcare`
**What it does:** An offline pet care app. It tracks pets (cat, dog, bird, parrot, hen, rabbit, fish…), food, vaccines, medicine, vet visits, weight, grooming and expenses, with reminders.
**Stack:** Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Hilt, Room (KSP), Coroutines/Flow, Navigation Compose, DataStore, WorkManager.

## Rules
1. **Only write code. Do NOT run Gradle or build the app** (no `./gradlew`, no assemble, no tests run). Write the Gradle files (version catalog, wrapper config, build scripts), but do not run them. The user will build in Android Studio.
2. **Every screen and every feature gets its own new branch.** For each item in the list below:
   - `git checkout main && git pull`
   - `git checkout -b feature/<name>` (branch names are given below)
   - write the code → `git add . && git commit -m "feat(<name>): <short summary>"`
   - `git push -u origin feature/<name>`
   - merge into `main` and push `main`, so the next branch starts from the latest code.
3. **If anything is unclear, ask the user first.** Use short, simple English and give options. Always ask before adding a library that is not listed here, and if the git remote is missing.
4. **Fully offline.** Never add the INTERNET permission.
5. Money is stored as a `Long` in paisa, weight as a `Long` in grams, and dates as epoch millis.
6. All text goes in `strings.xml`: English in `values/`, Urdu in `values-ur/`. Support RTL, light theme and dark theme.
7. `minSdk 24`, and use the latest stable versions for the rest. Never commit keystores or `local.properties`.

## Branches (do them in this order)

| # | Branch | What to build |
|---|--------|---------------|
| 1 | `feature/project-setup` | Gradle files, version catalog, Hilt app class, package structure (core/data/domain/ui/di) |
| 2 | `feature/theme-navigation` | M3 theme (light/dark, soft friendly colors), Navigation Compose routes, bottom bar (Home, Pets, Reminders, Settings) |
| 3 | `feature/database` | Room: Pet, FeedingSchedule, FeedingLog, Vaccine, Medicine, VetVisit, WeightLog, Grooming, Expense, Note. DAOs, repositories, use cases |
| 4 | `feature/home-dashboard` | Today: feeding due, medicine due, upcoming vaccines, and quick log buttons for each pet |
| 5 | `feature/pets` | Pet list + add/edit: photo, name, type, breed, gender, birthday/age, color, microchip no., notes |
| 6 | `feature/pet-profile` | Pet detail screen with tabs: Overview, Health, Food, Weight, Expenses, Gallery |
| 7 | `feature/feeding` | Feeding schedule (times + food + quantity), one-tap "fed" log, and feeding history |
| 8 | `feature/vaccines` | Vaccine records: name, date, next due date, vet, card photo. Ready-made vaccine lists for common pets that the user can edit |
| 9 | `feature/medicine` | Medicine/deworming course: dose, times, start/end date, "given" checkbox |
| 10 | `feature/vet-visits` | Vet visits: reason, diagnosis notes, prescription photo, cost, next visit date |
| 11 | `feature/weight` | Weight log with a simple growth chart (Compose Canvas) |
| 12 | `feature/grooming` | Bath, nail cut, haircut log, with repeat reminders |
| 13 | `feature/reminders` | WorkManager + notifications for feeding, medicine, vaccines, vet and grooming. Snooze, mark as done, and a list of all reminders |
| 14 | `feature/expenses` | Pet expenses (food, vet, toys, other) and a monthly total per pet |
| 15 | `feature/gallery-timeline` | Photo gallery per pet and a life timeline (all events by date) |
| 16 | `feature/health-report-pdf` | Pet health report PDF (profile, vaccines, medicines, vet visits, weight) to share with the vet |
| 17 | `feature/backup-restore` | Offline backup file (DB + photos zip) through SAF, and restore |
| 18 | `feature/settings-onboarding` | Language (EN/UR), theme, weight unit (kg/lb), currency, app lock (PIN/biometric), 3-screen onboarding |
| 19 | `feature/polish-release` | App icon, splash, empty states with cute illustrations, R8 rules, signing config read from `keystore.properties` |

After each branch, tell the user in 2–3 lines what was done and the branch name.
