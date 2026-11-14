package com.diary.petcare.ui.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PetsViewModel @Inject constructor(repo: PetRepository) : ViewModel() {
    /** Null while loading. */
    val pets: StateFlow<List<Pet>?> = repo.observePets()
        .map<List<Pet>, List<Pet>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
