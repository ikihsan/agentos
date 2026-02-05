package dev.agentos.uirenderer.domain

import dev.agentos.core.model.UiComponent

/**
 * Renders dynamic UI components to Compose.
 * 
 * This is the main entry point for the dynamic UI system.
 */
interface UiRenderer {
    
    /**
     * Handles user interaction with a component.
     */
    fun onInteraction(interaction: UiInteraction)
}

/**
 * Represents a user interaction with a dynamic UI component.
 */
sealed class UiInteraction {
    data class TextInput(
        val componentId: String,
        val value: String
    ) : UiInteraction()
    
    data class Choice(
        val componentId: String,
        val selectedId: String,
        val selectedLabel: String
    ) : UiInteraction()
    
    data class MultiChoice(
        val componentId: String,
        val selectedIds: List<String>
    ) : UiInteraction()
    
    data class Confirmation(
        val componentId: String,
        val confirmed: Boolean
    ) : UiInteraction()
    
    data class ContactPicked(
        val componentId: String,
        val contactId: String,
        val name: String,
        val phone: String?
    ) : UiInteraction()
    
    data class MediaPicked(
        val componentId: String,
        val uri: String,
        val mimeType: String
    ) : UiInteraction()
    
    data class DateTimePicked(
        val componentId: String,
        val timestamp: Long
    ) : UiInteraction()
    
    data class FormSubmitted(
        val componentId: String,
        val values: Map<String, Any?>
    ) : UiInteraction()
    
    data class Action(
        val componentId: String,
        val actionId: String
    ) : UiInteraction()
}

/**
 * Callback for UI interactions.
 */
fun interface UiInteractionCallback {
    fun onInteraction(interaction: UiInteraction)
}
