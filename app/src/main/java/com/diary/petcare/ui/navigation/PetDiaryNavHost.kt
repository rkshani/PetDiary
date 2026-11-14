package com.diary.petcare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.diary.petcare.R
import com.diary.petcare.ui.components.PlaceholderScreen

@Composable
fun PetDiaryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME,
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.HOME) { PlaceholderScreen(R.string.nav_home) }
        composable(Routes.PETS) { PlaceholderScreen(R.string.nav_pets) }
        composable(Routes.REMINDERS) { PlaceholderScreen(R.string.nav_reminders) }
        composable(Routes.SETTINGS) { PlaceholderScreen(R.string.nav_settings) }
    }
}
