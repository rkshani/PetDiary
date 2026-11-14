package com.diary.petcare.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.diary.petcare.R

enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    PETS(Routes.PETS, R.string.nav_pets, Icons.Filled.Pets, Icons.Outlined.Pets),
    // v2: REMINDERS(Routes.REMINDERS, R.string.nav_reminders, Icons.Filled.NotificationsActive, Icons.Outlined.NotificationsNone),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}
