package com.diary.petcare.domain.model

/*
 * Domain models. Rules: money is a Long in paisa, weight is a Long in grams,
 * dates are epoch millis, and a time of day is minutes after midnight (0..1439).
 */

data class Pet(
    val id: Long = 0,
    val name: String,
    val type: PetType = PetType.CAT,
    val breed: String = "",
    val gender: Gender = Gender.UNKNOWN,
    val birthDate: Long? = null,
    val color: String = "",
    val microchip: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

data class FeedingSchedule(
    val id: Long = 0,
    val petId: Long,
    val timeOfDay: Int,
    val food: String,
    val quantity: String = "",
    val reminderEnabled: Boolean = true,
)

data class FeedingLog(
    val id: Long = 0,
    val petId: Long,
    val scheduleId: Long? = null,
    val fedAt: Long = System.currentTimeMillis(),
    val food: String = "",
    val quantity: String = "",
    val note: String = "",
)

data class Vaccine(
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val dateGiven: Long? = null,
    val nextDueDate: Long? = null,
    val vetName: String = "",
    val cardPhotoPath: String? = null,
    val notes: String = "",
    val reminderEnabled: Boolean = true,
)

data class Medicine(
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val kind: MedicineKind = MedicineKind.MEDICINE,
    val dose: String = "",
    val timesOfDay: List<Int> = emptyList(),
    val startDate: Long,
    val endDate: Long? = null,
    val notes: String = "",
    val reminderEnabled: Boolean = true,
)

data class MedicineDose(
    val id: Long = 0,
    val medicineId: Long,
    val petId: Long,
    val scheduledAt: Long,
    val givenAt: Long? = null,
)

data class VetVisit(
    val id: Long = 0,
    val petId: Long,
    val date: Long,
    val vetName: String = "",
    val clinic: String = "",
    val reason: String = "",
    val diagnosis: String = "",
    val prescriptionPhotoPath: String? = null,
    val costPaisa: Long = 0,
    val nextVisitDate: Long? = null,
    val reminderEnabled: Boolean = true,
)

data class WeightLog(
    val id: Long = 0,
    val petId: Long,
    val date: Long,
    val weightGrams: Long,
    val note: String = "",
)

data class Grooming(
    val id: Long = 0,
    val petId: Long,
    val type: GroomingType,
    val date: Long,
    val repeatDays: Int = 0,
    val note: String = "",
    val reminderEnabled: Boolean = true,
) {
    /** Next due date when the task repeats, otherwise null. */
    val nextDueDate: Long? get() = if (repeatDays > 0) date + repeatDays * DAY_MILLIS else null
}

data class Expense(
    val id: Long = 0,
    val petId: Long,
    val category: ExpenseCategory,
    val amountPaisa: Long,
    val date: Long,
    val note: String = "",
)

data class Note(
    val id: Long = 0,
    val petId: Long,
    val date: Long,
    val title: String,
    val text: String = "",
)

data class Photo(
    val id: Long = 0,
    val petId: Long,
    val path: String,
    val takenAt: Long,
    val caption: String = "",
)

data class Reminder(
    val id: Long = 0,
    val petId: Long,
    val type: ReminderType,
    val refId: Long,
    val slot: Int = 0,
    val title: String,
    val message: String = "",
    val triggerAt: Long,
    val repeatIntervalMillis: Long = 0,
    val enabled: Boolean = true,
    val lastDoneAt: Long? = null,
)

const val MINUTE_MILLIS = 60_000L
const val HOUR_MILLIS = 60 * MINUTE_MILLIS
const val DAY_MILLIS = 24 * HOUR_MILLIS
