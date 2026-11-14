package com.diary.petcare.ui.navigation

/**
 * All navigation routes. String routes keep the app free of extra libraries.
 * An id of 0 means "new item".
 */
object Routes {
    const val ARG_PET_ID = "petId"
    const val ARG_ID = "id"

    // Bottom bar destinations
    const val HOME = "home"
    const val PETS = "pets"
    const val REMINDERS = "reminders"
    const val SETTINGS = "settings"

    // Pets
    const val PET_EDIT = "pet_edit/{id}"
    const val PET_PROFILE = "pet/{petId}"

    // Per-pet feature screens: <feature>/{petId}/{id}
    const val FEEDING_SCHEDULE_EDIT = "feeding_schedule_edit/{petId}/{id}"
    const val FEEDING_HISTORY = "feeding_history/{petId}"
    const val VACCINE_EDIT = "vaccine_edit/{petId}/{id}"
    const val VACCINE_PRESETS = "vaccine_presets/{petId}"
    const val MEDICINE_EDIT = "medicine_edit/{petId}/{id}"
    const val VET_VISIT_EDIT = "vet_visit_edit/{petId}/{id}"
    const val WEIGHT_EDIT = "weight_edit/{petId}/{id}"
    const val GROOMING_EDIT = "grooming_edit/{petId}/{id}"
    const val EXPENSE_EDIT = "expense_edit/{petId}/{id}"
    const val NOTE_EDIT = "note_edit/{petId}/{id}"
    const val TIMELINE = "timeline/{petId}"
    const val HEALTH_REPORT = "health_report/{petId}"

    // App
    const val BACKUP = "backup"
    const val ONBOARDING = "onboarding"
    const val LOCK = "lock"
    const val PIN_SETUP = "pin_setup"

    fun petEdit(id: Long = 0) = "pet_edit/$id"
    fun petProfile(petId: Long) = "pet/$petId"
    fun feedingScheduleEdit(petId: Long, id: Long = 0) = "feeding_schedule_edit/$petId/$id"
    fun feedingHistory(petId: Long) = "feeding_history/$petId"
    fun vaccineEdit(petId: Long, id: Long = 0) = "vaccine_edit/$petId/$id"
    fun vaccinePresets(petId: Long) = "vaccine_presets/$petId"
    fun medicineEdit(petId: Long, id: Long = 0) = "medicine_edit/$petId/$id"
    fun vetVisitEdit(petId: Long, id: Long = 0) = "vet_visit_edit/$petId/$id"
    fun weightEdit(petId: Long, id: Long = 0) = "weight_edit/$petId/$id"
    fun groomingEdit(petId: Long, id: Long = 0) = "grooming_edit/$petId/$id"
    fun expenseEdit(petId: Long, id: Long = 0) = "expense_edit/$petId/$id"
    fun noteEdit(petId: Long, id: Long = 0) = "note_edit/$petId/$id"
    fun timeline(petId: Long) = "timeline/$petId"
    fun healthReport(petId: Long) = "health_report/$petId"
}
