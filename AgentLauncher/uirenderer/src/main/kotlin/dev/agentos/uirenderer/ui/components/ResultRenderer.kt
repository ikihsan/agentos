package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.ResultComponent
import dev.agentos.uirenderer.domain.UiInteraction
import dev.agentos.uirenderer.domain.UiInteractionCallback

/**
 * Renders a ResultComponent.
 */
@Composable
fun ResultRenderer(
    component: ResultComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, containerColor) = getResultStyle(component.type)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = component.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Message
            Text(
                text = component.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = if (component.data != null || component.actions != null) 12.dp else 0.dp)
            )
            
            // Data if provided
            component.data?.let { data ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (component.actions != null) 12.dp else 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        data.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = formatValue(value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(0.6f)
                                )
                            }
                            if (key != data.keys.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Actions if provided
            component.actions?.let { actions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    actions.forEach { action ->
                        when (action.style) {
                            "primary" -> Button(
                                onClick = {
                                    onInteraction.onInteraction(
                                        UiInteraction.Action(component.id, action.id)
                                    )
                                }
                            ) {
                                Text(action.label)
                            }
                            "secondary" -> OutlinedButton(
                                onClick = {
                                    onInteraction.onInteraction(
                                        UiInteraction.Action(component.id, action.id)
                                    )
                                }
                            ) {
                                Text(action.label)
                            }
                            else -> TextButton(
                                onClick = {
                                    onInteraction.onInteraction(
                                        UiInteraction.Action(component.id, action.id)
                                    )
                                }
                            ) {
                                Text(action.label)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ResultStyle(
    val icon: ImageVector,
    val iconColor: Color,
    val containerColor: Color
)

@Composable
private fun getResultStyle(type: String): ResultStyle {
    return when (type) {
        "success" -> ResultStyle(
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF4CAF50),
            containerColor = Color(0xFF4CAF50)
        )
        "error" -> ResultStyle(
            icon = Icons.Default.Error,
            iconColor = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.error
        )
        "warning" -> ResultStyle(
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFFFA000),
            containerColor = Color(0xFFFFA000)
        )
        else -> ResultStyle(
            icon = Icons.Default.Info,
            iconColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatValue(value: Any?): String {
    return when (value) {
        null -> "—"
        is Number -> value.toString()
        is Boolean -> if (value) "Yes" else "No"
        is List<*> -> value.joinToString(", ")
        else -> value.toString()
    }
}
