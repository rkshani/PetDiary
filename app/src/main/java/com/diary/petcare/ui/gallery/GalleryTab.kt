package com.diary.petcare.ui.gallery

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.diary.petcare.R
import com.diary.petcare.data.photo.PhotoStorage
import com.diary.petcare.domain.model.Photo
import com.diary.petcare.domain.repository.PhotoRepository
import com.diary.petcare.ui.components.ConfirmDeleteDialog
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.PhotoSourceMenu
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.components.rememberPhotoPicker
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: PhotoRepository,
    private val storage: PhotoStorage,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)

    val photos: StateFlow<List<Photo>> = repo.observeForPet(petId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun newCameraUri(): Uri = storage.newCameraUri()

    fun add(uri: Uri) {
        viewModelScope.launch {
            val path = storage.import(uri) ?: return@launch
            repo.save(Photo(petId = petId, path = path, takenAt = System.currentTimeMillis()))
        }
    }

    fun setCaption(photo: Photo, caption: String) {
        viewModelScope.launch { repo.save(photo.copy(caption = caption.trim())) }
    }

    fun delete(photo: Photo) {
        viewModelScope.launch {
            repo.delete(photo.id)
            storage.delete(photo.path)
        }
    }
}

@Composable
fun GalleryTab(viewModel: GalleryViewModel = hiltViewModel()) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val picker = rememberPhotoPicker(viewModel::add, viewModel::newCameraUri)
    var menu by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf<Photo?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box {
                SectionHeader(stringResource(R.string.gallery_title)) {
                    TextButton(onClick = { menu = true }) {
                        Icon(Icons.Outlined.AddAPhoto, null)
                        Text(stringResource(R.string.photo_add))
                    }
                }
                PhotoSourceMenu(expanded = menu, onDismiss = { menu = false }, picker = picker)
            }
        }
        if (photos.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    emoji = "📸",
                    title = stringResource(R.string.gallery_empty_title),
                    message = stringResource(R.string.gallery_empty_message),
                    actionLabel = stringResource(R.string.photo_add),
                    onAction = { menu = true },
                )
            }
        }
        items(photos, key = { it.id }) { p ->
            AsyncImage(
                model = File(p.path),
                contentDescription = p.caption.ifBlank { null },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { open = p },
            )
        }
    }

    open?.let { p ->
        PhotoViewer(
            photo = p,
            onClose = { open = null },
            onCaption = { viewModel.setCaption(p, it) },
            onDelete = { viewModel.delete(p); open = null },
        )
    }
}

@Composable
private fun PhotoViewer(photo: Photo, onClose: () -> Unit, onCaption: (String) -> Unit, onDelete: () -> Unit) {
    var caption by remember(photo.id) { mutableStateOf(photo.caption) }
    var confirm by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { onCaption(caption); onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onCaption(caption); onClose() }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.action_back), tint = Color.White)
                }
                Text(Formatters.date(photo.takenAt), Modifier.weight(1f), color = Color.White)
                IconButton(onClick = { confirm = true }) {
                    Icon(Icons.Outlined.Delete, stringResource(R.string.action_delete), tint = Color.White)
                }
            }
            AsyncImage(
                model = File(photo.path),
                contentDescription = photo.caption.ifBlank { null },
                contentScale = ContentScale.Fit,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text(stringResource(R.string.gallery_caption)) },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                singleLine = true,
            )
        }
    }
    if (confirm) {
        ConfirmDeleteDialog(
            message = stringResource(R.string.gallery_delete_message),
            onConfirm = onDelete,
            onDismiss = { confirm = false },
        )
    }
}
