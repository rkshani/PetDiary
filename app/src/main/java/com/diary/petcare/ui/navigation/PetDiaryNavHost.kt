package com.diary.petcare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.diary.petcare.R
import com.diary.petcare.ui.components.PlaceholderScreen
import com.diary.petcare.ui.feeding.FeedingHistoryScreen
import com.diary.petcare.ui.feeding.FeedingScheduleEditScreen
import com.diary.petcare.ui.health.MedicineEditScreen
import com.diary.petcare.ui.health.VaccineEditScreen
import com.diary.petcare.ui.health.VaccinePresetsScreen
import com.diary.petcare.ui.health.VetVisitEditScreen
import com.diary.petcare.ui.pets.PetEditScreen
import com.diary.petcare.ui.weight.WeightEditScreen
import com.diary.petcare.ui.pets.PetsScreen
import com.diary.petcare.ui.profile.PetProfileScreen
import com.diary.petcare.ui.profile.ProfileNav

private fun NavBackStackEntry.longArg(name: String): Long =
    arguments?.getString(name)?.toLongOrNull() ?: 0L

@Composable
fun PetDiaryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME,
) {
    val back: () -> Unit = { navController.popBackStack() }
    val go: (String) -> Unit = { navController.navigate(it) }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.HOME) { PlaceholderScreen(R.string.nav_home) }
        composable(Routes.PETS) {
            PetsScreen(
                onAddPet = { go(Routes.petEdit()) },
                onOpenPet = { go(Routes.petProfile(it)) },
            )
        }
        composable(Routes.REMINDERS) { PlaceholderScreen(R.string.nav_reminders) }
        composable(Routes.SETTINGS) { PlaceholderScreen(R.string.nav_settings) }

        composable(Routes.PET_EDIT) {
            PetEditScreen(
                onBack = back,
                onSaved = { id, isNew ->
                    if (isNew) {
                        navController.navigate(Routes.petProfile(id)) {
                            popUpTo(Routes.PET_EDIT) { inclusive = true }
                        }
                    } else {
                        back()
                    }
                },
                onDeleted = { navController.popBackStack(Routes.PETS, inclusive = false) },
            )
        }

        composable(Routes.PET_PROFILE) { entry ->
            val petId = entry.longArg(Routes.ARG_PET_ID)
            PetProfileScreen(onBack = back, nav = profileNav(petId, go))
        }

        // Screens below are filled in by their feature branches.
        composable(Routes.FEEDING_SCHEDULE_EDIT) { FeedingScheduleEditScreen(onBack = back) }
        composable(Routes.FEEDING_HISTORY) { FeedingHistoryScreen(onBack = back) }
        composable(Routes.VACCINE_EDIT) { VaccineEditScreen(onBack = back) }
        composable(Routes.VACCINE_PRESETS) { VaccinePresetsScreen(onBack = back) }
        composable(Routes.MEDICINE_EDIT) { MedicineEditScreen(onBack = back) }
        composable(Routes.VET_VISIT_EDIT) { VetVisitEditScreen(onBack = back) }
        composable(Routes.WEIGHT_EDIT) { WeightEditScreen(onBack = back) }
        composable(Routes.GROOMING_EDIT) { PlaceholderScreen(R.string.tab_overview) }
        composable(Routes.EXPENSE_EDIT) { PlaceholderScreen(R.string.tab_expenses) }
        composable(Routes.NOTE_EDIT) { PlaceholderScreen(R.string.notes) }
        composable(Routes.TIMELINE) { PlaceholderScreen(R.string.profile_timeline) }
        composable(Routes.HEALTH_REPORT) { PlaceholderScreen(R.string.profile_health_report) }
    }
}

private fun profileNav(petId: Long, go: (String) -> Unit) = ProfileNav(
    editPet = { go(Routes.petEdit(petId)) },
    editFeedingSchedule = { go(Routes.feedingScheduleEdit(petId, it)) },
    feedingHistory = { go(Routes.feedingHistory(petId)) },
    editVaccine = { go(Routes.vaccineEdit(petId, it)) },
    vaccinePresets = { go(Routes.vaccinePresets(petId)) },
    editMedicine = { go(Routes.medicineEdit(petId, it)) },
    editVetVisit = { go(Routes.vetVisitEdit(petId, it)) },
    editWeight = { go(Routes.weightEdit(petId, it)) },
    editGrooming = { go(Routes.groomingEdit(petId, it)) },
    editExpense = { go(Routes.expenseEdit(petId, it)) },
    editNote = { go(Routes.noteEdit(petId, it)) },
    timeline = { go(Routes.timeline(petId)) },
    healthReport = { go(Routes.healthReport(petId)) },
)
