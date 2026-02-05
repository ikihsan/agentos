package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.ChoiceListComponent
import dev.agentos.uirenderer.domain.UiInteraction
import dev.agentos.uirenderer.domain.UiInteractionCallback

/**
 * Renders a ChoiceListComponent.
 */
@Composable
fun ChoiceListRenderer(
    component: ChoiceListComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    val defaultSelected = component.defaultSelected?.toSet() ?: emptySet()
    var selectedIds by remember { mutableStateOf(defaultSelected) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (component.title != null) {
            Text(
                text = component.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (component.description != null) {
            Text(
                text = component.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        when (component.selectionType) {
            "single" -> SingleSelectionList(
                choices = component.choices,
                selectedId = selectedIds.firstOrNull(),
                onSelect = { id ->
                    val choice = component.choices.find { it.id == id }
                    if (choice != null) {
                        onInteraction.onInteraction(
                            UiInteraction.Choice(component.id, id, choice.label)
                        )
                    }
                }
            )
            "multiple" -> MultiSelectionList(
                choices = component.choices,
                selectedIds = selectedIds,
                onSelectionChange = { ids ->
                    selectedIds = ids
                },
                onConfirm = {
                    onInteraction.onInteraction(
                        UiInteraction.MultiChoice(component.id, selectedIds.toList())
                    )
                }
            )
        }
    }
}

@Composable
private fun SingleSelectionList(
    choices: List<ChoiceListComponent.Choice>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        choices.forEach { choice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = choice.id == selectedId,
                        onClick = { 
                            if (choice.enabled != false) {
                                onSelect(choice.id) 
                            }
                        },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = choice.id == selectedId,
                    onClick = null, // handled by selectable
                    enabled = choice.enabled != false
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = choice.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (choice.description != null) {
                        Text(
                            text = choice.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiSelectionList(
    choices: List<ChoiceListComponent.Choice>,
    selectedIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onConfirm: () -> Unit
) {
    Column {
        choices.forEach { choice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = choice.enabled != false) {
                        val newSelection = if (choice.id in selectedIds) {
                            selectedIds - choice.id
                        } else {
                            selectedIds + choice.id
                        }
                        onSelectionChange(newSelection)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = choice.id in selectedIds,
                    onCheckedChange = null, // handled by clickable
                    enabled = choice.enabled != false
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = choice.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (choice.description != null) {
                        Text(
                            text = choice.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onConfirm,
            modifier = Modifier.align(Alignment.End),
            enabled = selectedIds.isNotEmpty()
        ) {
            Text("Confirm Selection")
        }
    }
}
