package dev.agentos.uirenderer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.agentos.core.model.*
import dev.agentos.uirenderer.domain.UiInteractionCallback
import dev.agentos.uirenderer.ui.components.*

/**
 * Main dynamic UI renderer.
 * 
 * Routes UiComponent types to their specific renderers.
 */
@Composable
fun DynamicUiRenderer(
    components: List<UiComponent>,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    if (components.isEmpty()) {
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = components,
            key = { it.id }
        ) { component ->
            ComponentRenderer(
                component = component,
                onInteraction = onInteraction
            )
        }
    }
}

/**
 * Routes a single component to its renderer.
 */
@Composable
fun ComponentRenderer(
    component: UiComponent,
    onInteraction: UiInteractionCallback,
    modifier: Modifier = Modifier
) {
    when (component) {
        is TextInputComponent -> TextInputRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is ChoiceListComponent -> ChoiceListRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is ConfirmationComponent -> ConfirmationRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is DateTimePickerComponent -> DateTimePickerRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is ResultComponent -> ResultRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is FormComponent -> FormRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is ContactPickerComponent -> ContactPickerRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is MediaPickerComponent -> MediaPickerRenderer(
            component = component,
            onInteraction = onInteraction,
            modifier = modifier
        )
        
        is TableComponent -> TableRenderer(
            component = component,
            modifier = modifier
        )
        
        else -> UnsupportedComponentRenderer(
            component = component,
            modifier = modifier
        )
    }
}

/**
 * Fallback for unsupported component types.
 */
@Composable
private fun UnsupportedComponentRenderer(
    component: UiComponent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Unsupported component: ${component.type}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Loading indicator while components are being generated.
 */
@Composable
fun DynamicUiLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Preparing...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
