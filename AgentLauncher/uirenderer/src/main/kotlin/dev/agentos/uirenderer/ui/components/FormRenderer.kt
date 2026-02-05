package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.FormComponent
import dev.agentos.uirenderer.domain.UiInteraction
import dev.agentos.uirenderer.domain.UiInteractionCallback

/**
 * Renders a FormComponent.
 */
@Composable
fun FormRenderer(
    component: FormComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    val formValues = remember { mutableStateMapOf<String, Any?>() }
    
    // Initialize with default values
    LaunchedEffect(component) {
        component.fields.forEach { field ->
            field.defaultValue?.let { formValues[field.id] = it }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Title
        if (component.title != null) {
            Text(
                text = component.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // Description
        if (component.description != null) {
            Text(
                text = component.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Form fields
        component.fields.forEach { field ->
            FormFieldRenderer(
                field = field,
                value = formValues[field.id],
                onValueChange = { formValues[field.id] = it },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit button
        Button(
            onClick = {
                onInteraction.onInteraction(
                    UiInteraction.FormSubmitted(
                        componentId = component.id,
                        values = formValues.toMap()
                    )
                )
            },
            modifier = Modifier.align(Alignment.End),
            enabled = validateForm(component, formValues)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(component.submitLabel ?: "Submit")
        }
    }
}

@Composable
private fun FormFieldRenderer(
    field: FormComponent.FormField,
    value: Any?,
    onValueChange: (Any?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = buildString {
                append(field.label)
                if (field.required) append(" *")
            },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        when (field.type) {
            "text", "email", "phone", "url" -> {
                OutlinedTextField(
                    value = (value as? String) ?: "",
                    onValueChange = { onValueChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = field.placeholder?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (field.type) {
                            "email" -> KeyboardType.Email
                            "phone" -> KeyboardType.Phone
                            "url" -> KeyboardType.Uri
                            else -> KeyboardType.Text
                        }
                    ),
                    singleLine = true
                )
            }
            
            "number" -> {
                OutlinedTextField(
                    value = (value as? Number)?.toString() ?: "",
                    onValueChange = { input ->
                        val number = input.toDoubleOrNull()
                        onValueChange(number)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = field.placeholder?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            
            "textarea" -> {
                OutlinedTextField(
                    value = (value as? String) ?: "",
                    onValueChange = { onValueChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = field.placeholder?.let { { Text(it) } },
                    maxLines = 5
                )
            }
            
            "select" -> {
                var expanded by remember { mutableStateOf(false) }
                val selectedOption = field.options?.find { it.value == value }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOption?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = field.placeholder?.let { { Text(it) } },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        field.options?.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    onValueChange(option.value)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            "checkbox" -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = (value as? Boolean) ?: false,
                        onCheckedChange = { onValueChange(it) }
                    )
                    field.placeholder?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun validateForm(
    component: FormComponent,
    values: Map<String, Any?>
): Boolean {
    return component.fields.all { field ->
        if (field.required) {
            val value = values[field.id]
            when (value) {
                null -> false
                is String -> value.isNotBlank()
                else -> true
            }
        } else {
            true
        }
    }
}
