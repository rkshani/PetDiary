package com.diary.petcare.ui.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.R
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.PetAvatar
import com.diary.petcare.ui.components.PetTopBar
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.format.ageText
import kotlinx.coroutines.launch

enum class ProfileTab(@param:StringRes val title: Int) {
    OVERVIEW(R.string.tab_overview),
    HEALTH(R.string.tab_health),
    FOOD(R.string.tab_food),
    WEIGHT(R.string.tab_weight),
    EXPENSES(R.string.tab_expenses),
    GALLERY(R.string.tab_gallery),
}

@Composable
fun PetProfileScreen(
    onBack: () -> Unit,
    nav: ProfileNav,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) { if (state is ProfileState.Missing) onBack() }
    val pet = (state as? ProfileState.Ready)?.pet
    var menu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PetTopBar(title = pet?.name.orEmpty(), onBack = onBack) {
                IconButton(onClick = nav.editPet) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.action_edit))
                }
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.profile_more))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profile_timeline)) },
                        leadingIcon = { Icon(Icons.Outlined.Timeline, null) },
                        onClick = { menu = false; nav.timeline() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profile_health_report)) },
                        leadingIcon = { Icon(Icons.Outlined.PictureAsPdf, null) },
                        onClick = { menu = false; nav.healthReport() },
                    )
                }
            }
        },
    ) { padding ->
        if (pet == null) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            ProfileHeader(pet)
            val tabs = ProfileTab.entries
            val pager = rememberPagerState { tabs.size }
            val scope = rememberCoroutineScope()
            PrimaryScrollableTabRow(selectedTabIndex = pager.currentPage, edgePadding = 8.dp) {
                tabs.forEachIndexed { i, tab ->
                    Tab(
                        selected = pager.currentPage == i,
                        onClick = { scope.launch { pager.animateScrollToPage(i) } },
                        text = { Text(stringResource(tab.title)) },
                    )
                }
            }
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 0) { page ->
                ProfileTabContent(tabs[page], pet, nav)
            }
        }
    }
}

/** Each tab's content. Feature branches plug their tab in here. */
@Composable
private fun ProfileTabContent(tab: ProfileTab, pet: Pet, nav: ProfileNav) {
    when (tab) {
        ProfileTab.OVERVIEW -> OverviewTab(pet, nav) {
            com.diary.petcare.ui.grooming.GroomingSection(nav)
            com.diary.petcare.ui.notes.NotesSection(nav)
            // OVERVIEW_SECTIONS
        }
        ProfileTab.HEALTH -> com.diary.petcare.ui.health.HealthTab(nav)
        ProfileTab.FOOD -> com.diary.petcare.ui.feeding.FoodTab(nav)
        ProfileTab.WEIGHT -> com.diary.petcare.ui.weight.WeightTab(nav)
        ProfileTab.EXPENSES -> com.diary.petcare.ui.expenses.ExpensesTab(nav)
        else -> com.diary.petcare.ui.components.PlaceholderScreen(tab.title)
    }
}

@Composable
private fun ProfileHeader(pet: Pet) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        PetAvatar(pet.photoPath, pet.type.emoji, size = 72.dp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(pet.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                listOf(stringResource(pet.type.label), pet.breed).filter { it.isNotBlank() }.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            pet.birthDate?.let {
                Text(ageText(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
