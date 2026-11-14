package com.diary.petcare.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.diary.petcare.R
import java.io.File

/** Picks a photo from the gallery or camera. [onPicked] gets a content Uri to import. */
@Composable
fun rememberPhotoPicker(onPicked: (Uri) -> Unit, cameraUri: () -> Uri): PhotoPickerState {
    var pendingCamera by rememberSaveable { mutableStateOf<String?>(null) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingCamera
        if (ok && uri != null) onPicked(Uri.parse(uri))
        pendingCamera = null
    }
    return remember(gallery, camera) {
        PhotoPickerState(
            openGallery = {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            openCamera = {
                val uri = cameraUri()
                pendingCamera = uri.toString()
                camera.launch(uri)
            },
        )
    }
}

class PhotoPickerState(val openGallery: () -> Unit, val openCamera: () -> Unit)

/** Menu with "Gallery", "Camera" and optionally "Remove". */
@Composable
fun PhotoSourceMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    picker: PhotoPickerState,
    onRemove: (() -> Unit)? = null,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.photo_from_gallery)) },
            leadingIcon = { Icon(Icons.Outlined.PhotoLibrary, null) },
            onClick = { onDismiss(); picker.openGallery() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.photo_from_camera)) },
            leadingIcon = { Icon(Icons.Outlined.PhotoCamera, null) },
            onClick = { onDismiss(); picker.openCamera() },
        )
        if (onRemove != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.photo_remove)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                onClick = { onDismiss(); onRemove() },
            )
        }
    }
}

/** Round pet photo, or the pet-type emoji when there is no photo. */
@Composable
fun PetAvatar(
    photoPath: String?,
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    Box(
        modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(emoji, fontSize = (size.value * 0.5f).sp)
        }
    }
}

/** A tappable photo box for forms (vaccine card, prescription). */
@Composable
fun PhotoBox(
    photoPath: String?,
    label: String,
    picker: PhotoPickerState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    var menu by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .clickable { menu = true },
            contentAlignment = Alignment.Center,
        ) {
            if (photoPath != null) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        PhotoSourceMenu(
            expanded = menu,
            onDismiss = { menu = false },
            picker = picker,
            onRemove = if (photoPath != null) onRemove else null,
        )
    }
}
