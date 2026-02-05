package dev.agentos.uirenderer.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.TableComponent

/**
 * Renders a TableComponent.
 */
@Composable
fun TableRenderer(
    component: TableComponent,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Title
        if (component.title != null) {
            Text(
                text = component.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Scrollable table
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    component.columns.forEach { column ->
                        TableCell(
                            text = column.label,
                            width = column.width ?: 120,
                            isHeader = true,
                            align = column.align ?: "start"
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Data rows
                component.rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        component.columns.forEach { column ->
                            val value = row[column.id]
                            TableCell(
                                text = formatCellValue(value, column.format),
                                width = column.width ?: 120,
                                isHeader = false,
                                align = column.align ?: "start"
                            )
                        }
                    }
                    
                    if (index < component.rows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Empty state
                if (component.rows.isEmpty()) {
                    Text(
                        text = "No data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: Int,
    isHeader: Boolean,
    align: String
) {
    val textAlign = when (align) {
        "center" -> TextAlign.Center
        "end" -> TextAlign.End
        else -> TextAlign.Start
    }

    Text(
        text = text,
        style = if (isHeader) {
            MaterialTheme.typography.labelMedium
        } else {
            MaterialTheme.typography.bodySmall
        },
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(width.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private fun formatCellValue(value: Any?, format: String?): String {
    if (value == null) return "—"
    
    return when (format) {
        "currency" -> {
            val number = (value as? Number)?.toDouble() ?: return value.toString()
            "$${String.format("%.2f", number)}"
        }
        "percent" -> {
            val number = (value as? Number)?.toDouble() ?: return value.toString()
            "${String.format("%.1f", number * 100)}%"
        }
        "date" -> {
            // Simple date formatting
            value.toString()
        }
        "number" -> {
            val number = (value as? Number) ?: return value.toString()
            String.format("%,d", number.toLong())
        }
        else -> value.toString()
    }
}
