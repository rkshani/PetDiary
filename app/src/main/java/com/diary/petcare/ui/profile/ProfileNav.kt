package com.diary.petcare.ui.profile

/** Navigation callbacks that the profile tabs need. Ids of 0 mean "new". */
data class ProfileNav(
    val editPet: () -> Unit = {},
    val editFeedingSchedule: (Long) -> Unit = {},
    val feedingHistory: () -> Unit = {},
    val editVaccine: (Long) -> Unit = {},
    val vaccinePresets: () -> Unit = {},
    val editMedicine: (Long) -> Unit = {},
    val editVetVisit: (Long) -> Unit = {},
    val editWeight: (Long) -> Unit = {},
    val editGrooming: (Long) -> Unit = {},
    val editExpense: (Long) -> Unit = {},
    val editNote: (Long) -> Unit = {},
    val timeline: () -> Unit = {},
    val healthReport: () -> Unit = {},
)
