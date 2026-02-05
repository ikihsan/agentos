package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.ContactPickerComponent
import dev.agentos.uirenderer.domain.UiInteractionCallback

/**
 * Renders a ContactPickerComponent.
 * 
 * Note: Actual contact picking requires Activity result handling
 * which must be implemented in the host Activity/Fragment.
 */
@Composable
fun ContactPickerRenderer(
    component: ContactPickerComponent,
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

        OutlinedButton(
            onClick = {
                // Contact picking is handled by the host Activity
                // This button triggers the contact picker intent
                // The result is passed back through onInteraction
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    component.multiple -> "Select Contacts"
                    else -> "Select Contact"
                }
            )
        }

        if (component.filter != null) {
            Text(
                text = "Filter: ${component.filter}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
