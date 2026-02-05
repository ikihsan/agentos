package dev.agentos.uirenderer.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.DateTimePickerComponent
import dev.agentos.uirenderer.domain.UiInteraction
import dev.agentos.uirenderer.domain.UiInteractionCallback
import java.text.SimpleDateFormat
import java.util.*

/**
 * Renders a DateTimePickerComponent.
 */
@Composable
fun DateTimePickerRenderer(
    component: DateTimePickerComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember { 
        Calendar.getInstance().apply {
            component.defaultValue?.let { timeInMillis = it }
        }
    }
    
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var dateSet by remember { mutableStateOf(false) }
    var timeSet by remember { mutableStateOf(component.mode == "date") }

    val dateFormat = remember {
        when (component.mode) {
            "date" -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            "time" -> SimpleDateFormat("hh:mm a", Locale.getDefault())
            else -> SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        }
    }

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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date picker button
            if (component.mode == "date" || component.mode == "datetime") {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                selectedDate = calendar.time
                                dateSet = true
                                
                                // If date-only mode, submit immediately
                                if (component.mode == "date") {
                                    onInteraction.onInteraction(
                                        UiInteraction.DateTimePicked(
                                            component.id,
                                            calendar.timeInMillis
                                        )
                                    )
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            component.minValue?.let { 
                                datePicker.minDate = it 
                            }
                            component.maxValue?.let { 
                                datePicker.maxDate = it 
                            }
                        }.show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (dateSet) {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(selectedDate)
                        } else {
                            "Select Date"
                        }
                    )
                }
            }

            // Time picker button
            if (component.mode == "time" || component.mode == "datetime") {
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                calendar.set(Calendar.MINUTE, minute)
                                selectedDate = calendar.time
                                timeSet = true
                                
                                // If time-only mode or datetime with date set, submit
                                if (component.mode == "time" || (component.mode == "datetime" && dateSet)) {
                                    onInteraction.onInteraction(
                                        UiInteraction.DateTimePicked(
                                            component.id,
                                            calendar.timeInMillis
                                        )
                                    )
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false // 12-hour format
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = component.mode == "time" || dateSet
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (timeSet && component.mode != "date") {
                            SimpleDateFormat("hh:mm a", Locale.getDefault())
                                .format(selectedDate)
                        } else {
                            "Select Time"
                        }
                    )
                }
            }
        }

        // Selected value display
        if (dateSet || timeSet) {
            Text(
                text = dateFormat.format(selectedDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
