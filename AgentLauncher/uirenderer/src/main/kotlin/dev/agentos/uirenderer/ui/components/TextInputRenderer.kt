package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.TextInputComponent
import dev.agentos.uirenderer.domain.UiInteraction
import dev.agentos.uirenderer.domain.UiInteractionCallback

/**
 * Renders a TextInputComponent.
 */
@Composable
fun TextInputRenderer(
    component: TextInputComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(component.defaultValue ?: "") }
    var isValid by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (component.label != null) {
            Text(
                text = component.label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                isValid = validateInput(newText, component)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = component.placeholder?.let { { Text(it) } },
            isError = !isValid,
            keyboardOptions = KeyboardOptions(
                keyboardType = mapKeyboardType(component.inputType),
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isValid && (text.isNotEmpty() || !component.required)) {
                        onInteraction.onInteraction(
                            UiInteraction.TextInput(component.id, text)
                        )
                    }
                }
            ),
            singleLine = !component.multiline,
            maxLines = if (component.multiline) 5 else 1,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (isValid && (text.isNotEmpty() || !component.required)) {
                            onInteraction.onInteraction(
                                UiInteraction.TextInput(component.id, text)
                            )
                        }
                    },
                    enabled = isValid && (text.isNotEmpty() || !component.required)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Submit")
                }
            }
        )
        
        if (!isValid && component.validation?.errorMessage != null) {
            Text(
                text = component.validation.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun mapKeyboardType(inputType: String?): KeyboardType {
    return when (inputType) {
        "number" -> KeyboardType.Number
        "decimal" -> KeyboardType.Decimal
        "phone" -> KeyboardType.Phone
        "email" -> KeyboardType.Email
        "uri" -> KeyboardType.Uri
        else -> KeyboardType.Text
    }
}

private fun validateInput(text: String, component: TextInputComponent): Boolean {
    val validation = component.validation ?: return true
    
    // Min length
    validation.minLength?.let { min ->
        if (text.length < min) return false
    }
    
    // Max length
    validation.maxLength?.let { max ->
        if (text.length > max) return false
    }
    
    // Pattern
    validation.pattern?.let { pattern ->
        if (!Regex(pattern).matches(text)) return false
    }
    
    return true
}
