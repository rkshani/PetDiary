package com.diary.petcare.ui.onboarding

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.domain.model.AppLanguage
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun finish(onDone: () -> Unit) = viewModelScope.launch {
        repo.setOnboardingDone(true)
        onDone()
    }

    fun setLanguage(language: AppLanguage, onSaved: () -> Unit) = viewModelScope.launch {
        repo.setLanguage(language)
        onSaved()
    }
}

private data class Page(val emoji: String, val title: Int, val message: Int)

private val pages = listOf(
    Page("🐾", R.string.onboarding_1_title, R.string.onboarding_1_message),
    Page("⏰", R.string.onboarding_2_title, R.string.onboarding_2_message),
    Page("🔒", R.string.onboarding_3_title, R.string.onboarding_3_message),
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    val last = pager.currentPage == pages.lastIndex

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            if (!last) TextButton(onClick = { viewModel.finish(onFinished) }) { Text(stringResource(R.string.action_skip)) }
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    Modifier.size(180.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) { Text(page.emoji, fontSize = 88.sp) }
                Spacer(Modifier.height(32.dp))
                Text(stringResource(page.title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(page.message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (index == 0) {
                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(AppLanguage.ENGLISH, AppLanguage.URDU).forEach { lang ->
                            FilterChip(
                                selected = settings.language == lang,
                                onClick = {
                                    if (settings.language != lang) viewModel.setLanguage(lang) { activity?.recreate() }
                                },
                                label = { Text(if (lang == AppLanguage.ENGLISH) "English" else "اردو") },
                            )
                        }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    Box(
                        Modifier
                            .size(if (i == pager.currentPage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }
            Button(onClick = {
                if (last) viewModel.finish(onFinished) else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            }) {
                Text(stringResource(if (last) R.string.onboarding_start else R.string.action_next))
            }
        }
    }
}
