package com.diary.petcare.ui.pets

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.data.photo.PhotoStorage
import com.diary.petcare.domain.model.Gender
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.PetType
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.usecase.DeletePetUseCase
import com.diary.petcare.domain.usecase.SavePetUseCase
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetForm(
    val id: Long = 0,
    val name: String = "",
    val type: PetType = PetType.CAT,
    val breed: String = "",
    val gender: Gender = Gender.UNKNOWN,
    val birthDate: Long? = null,
    val ageYears: String = "",
    val ageMonths: String = "",
    val color: String = "",
    val microchip: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val nameError: Boolean = false,
    val loaded: Boolean = false,
    /** Set after save: the saved pet id. */
    val savedId: Long? = null,
    val deleted: Boolean = false,
)

@HiltViewModel
class PetEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: PetRepository,
    private val savePet: SavePetUseCase,
    private val deletePet: DeletePetUseCase,
    private val photos: PhotoStorage,
) : ViewModel() {

    private val petId = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = petId != 0L

    private val _form = MutableStateFlow(PetForm(loaded = !isEdit))
    val form: StateFlow<PetForm> = _form.asStateFlow()

    /** Photos added in this session; deleted again if the user leaves without saving. */
    private val newPhotos = mutableListOf<String>()
    private var originalPhoto: String? = null

    init {
        if (isEdit) {
            viewModelScope.launch {
                repo.getPet(petId)?.let { p ->
                    originalPhoto = p.photoPath
                    _form.value = PetForm(
                        id = p.id, name = p.name, type = p.type, breed = p.breed, gender = p.gender,
                        birthDate = p.birthDate, color = p.color, microchip = p.microchip, notes = p.notes,
                        photoPath = p.photoPath, createdAt = p.createdAt, loaded = true,
                    )
                }
            }
        }
    }

    fun update(transform: (PetForm) -> PetForm) = _form.update(transform)

    /** Sets an approximate birthday from an age the user typed. */
    fun setAge(years: String, months: String) {
        val y = years.filter(Char::isDigit).take(2)
        val m = months.filter(Char::isDigit).take(2)
        val total = (y.toIntOrNull() ?: 0) * 12 + (m.toIntOrNull() ?: 0)
        _form.update {
            it.copy(
                ageYears = y,
                ageMonths = m,
                birthDate = if (total > 0) DateTimeUtils.startOfDay(DateTimeUtils.addMonths(DateTimeUtils.now(), -total)) else it.birthDate,
            )
        }
    }

    fun newCameraUri(): Uri = photos.newCameraUri()

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val path = photos.import(uri) ?: return@launch
            newPhotos += path
            _form.update { it.copy(photoPath = path) }
        }
    }

    fun removePhoto() = _form.update { it.copy(photoPath = null) }

    fun save() {
        val f = _form.value
        if (f.name.isBlank()) {
            _form.update { it.copy(nameError = true) }
            return
        }
        viewModelScope.launch {
            val id = savePet(
                Pet(
                    id = f.id, name = f.name, type = f.type, breed = f.breed.trim(), gender = f.gender,
                    birthDate = f.birthDate, color = f.color.trim(), microchip = f.microchip.trim(),
                    notes = f.notes.trim(), photoPath = f.photoPath, createdAt = f.createdAt,
                ),
            )
            // Clean up photos that are no longer used.
            newPhotos.filter { it != f.photoPath }.forEach { photos.delete(it) }
            if (originalPhoto != null && originalPhoto != f.photoPath) photos.delete(originalPhoto)
            newPhotos.clear()
            _form.update { it.copy(savedId = id) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deletePet(petId)
            photos.delete(originalPhoto)
            _form.update { it.copy(deleted = true) }
        }
    }

    override fun onCleared() {
        // Photos picked but never saved.
        if (_form.value.savedId == null) {
            val leftovers = newPhotos.toList()
            kotlinx.coroutines.MainScope().launch { leftovers.forEach { photos.delete(it) } }
        }
    }
}
