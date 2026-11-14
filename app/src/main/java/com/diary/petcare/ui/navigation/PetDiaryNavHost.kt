package com.diary.petcare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.diary.petcare.R
import com.diary.petcare.ui.components.PlaceholderScreen
import com.diary.petcare.ui.pets.PetsScreen

@Composable
fun PetDiaryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME,
) {
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.HOME) { PlaceholderScreen(R.string.nav_home) }
        composable(Routes.PETS) {
            PetsScreen(
                onAddPet = { navController.navigate(Routes.petEdit()) },
                onOpenPet = { navController.navigate(Routes.petProfile(it)) },
            )
        }
        composable(Routes.REMINDERS) { PlaceholderScreen(R.string.nav_reminders) }
        composable(Routes.SETTINGS) { PlaceholderScreen(R.string.nav_settings) }
        composable(Routes.PET_EDIT) { PlaceholderScreen(R.string.pets_add) }
        composable(Routes.PET_PROFILE) { PlaceholderScreen(R.string.pets_title) }
    }
}
