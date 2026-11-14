package com.diary.petcare.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ProfileState {
    data object Loading : ProfileState
    data object Missing : ProfileState
    data class Ready(val pet: Pet) : ProfileState
}

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: PetRepository,
) : ViewModel() {
    val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)

    val state: StateFlow<ProfileState> = repo.observePet(petId)
        .map { if (it == null) ProfileState.Missing else ProfileState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState.Loading)
}
