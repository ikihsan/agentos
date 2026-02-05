package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.MediaPickerComponent
import dev.agentos.uirenderer.domain.UiInteractionCallback

/**
 * Renders a MediaPickerComponent.
 * 
 * Note: Actual media picking requires Activity result handling
 * which must be implemented in the host Activity/Fragment.
 */
@Composable
fun MediaPickerRenderer(
    component: MediaPickerComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (component.label != null) {
            Text(
                text = component.label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main picker button
            OutlinedButton(
                onClick = {
                    // Media picking is handled by the host Activity
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getMediaIcon(component.mediaType),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(getMediaLabel(component.mediaType, component.multiple))
            }

            // Camera button for images/videos
            if (component.allowCamera && 
                (component.mediaType == "image" || component.mediaType == "video")) {
                OutlinedButton(
                    onClick = {
                        // Camera capture is handled by the host Activity
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo"
                    )
                }
            }
        }

        // Size limit info
        if (component.maxSizeBytes != null) {
            val maxSizeMb = component.maxSizeBytes / (1024 * 1024)
            Text(
                text = "Max size: ${maxSizeMb}MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun getMediaIcon(mediaType: String) = when (mediaType) {
    "image" -> Icons.Default.Image
    "video" -> Icons.Default.VideoLibrary
    else -> Icons.Default.AttachFile
}

private fun getMediaLabel(mediaType: String, multiple: Boolean): String {
    val type = when (mediaType) {
        "image" -> if (multiple) "Images" else "Image"
        "video" -> if (multiple) "Videos" else "Video"
        "audio" -> if (multiple) "Audio Files" else "Audio"
        else -> if (multiple) "Files" else "File"
    }
    return "Select $type"
}
