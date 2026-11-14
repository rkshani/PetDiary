package com.diary.petcare.ui.health

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diary.petcare.ui.profile.ProfileNav

/** Health tab: vaccines, medicines and vet visits, each with its own section. */
@Composable
fun HealthTab(nav: ProfileNav) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        VaccinesSection(nav)
        // HEALTH_SECTIONS
    }
}
