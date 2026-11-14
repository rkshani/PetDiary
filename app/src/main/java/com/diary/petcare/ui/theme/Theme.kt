package com.diary.petcare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Peach40,
    onPrimary = Cream99,
    primaryContainer = Peach90,
    onPrimaryContainer = Peach10,
    secondary = Mint40,
    onSecondary = Cream99,
    secondaryContainer = Mint90,
    onSecondaryContainer = Mint10,
    tertiary = Sky40,
    onTertiary = Cream99,
    tertiaryContainer = Sky90,
    onTertiaryContainer = Sky10,
    background = Cream99,
    onBackground = Brown10,
    surface = Cream99,
    onSurface = Brown10,
    surfaceVariant = Cream90,
    onSurfaceVariant = Brown30,
    surfaceContainerLowest = Cream99,
    surfaceContainerLow = Cream95,
    surfaceContainer = Cream95,
    surfaceContainerHigh = Cream90,
    outline = Brown50,
    error = ErrorLight,
)

private val DarkColors = darkColorScheme(
    primary = Peach80,
    onPrimary = Peach10,
    primaryContainer = Peach30,
    onPrimaryContainer = Peach90,
    secondary = Mint80,
    onSecondary = Mint10,
    secondaryContainer = Mint30,
    onSecondaryContainer = Mint90,
    tertiary = Sky80,
    onTertiary = Sky10,
    tertiaryContainer = Sky30,
    onTertiaryContainer = Sky90,
    background = Night6,
    onBackground = Night90,
    surface = Night6,
    onSurface = Night90,
    surfaceVariant = Night17,
    onSurfaceVariant = Night80,
    surfaceContainerLowest = Night6,
    surfaceContainerLow = Night12,
    surfaceContainer = Night12,
    surfaceContainerHigh = Night17,
    outline = Brown50,
    error = ErrorDark,
)

/** Pet Diary theme. [darkTheme] comes from the user's theme setting (system, light or dark). */
@Composable
fun PetDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PetTypography,
        shapes = PetShapes,
        content = content,
    )
}
