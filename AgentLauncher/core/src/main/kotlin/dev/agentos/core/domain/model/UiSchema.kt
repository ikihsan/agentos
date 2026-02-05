package dev.agentos.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * UI schema models for the Dynamic UI Engine.
 * 
 * These models define the structure of dynamically generated UI components
 * that the platform renders when user input is required.
 */

/**
 * Root UI schema that can contain any UI component.
 */
@Serializable
data class UiSchema(
    val id: String,
    val taskId: String,
    val component: UiComponent,
    val context: UiContext = UiContext()
)

/**
 * Context for UI rendering.
 */
@Serializable
data class UiContext(
    val priority: UiPriority = UiPriority.NORMAL,
    val timeout: Long? = null,
    val voiceEnabled: Boolean = true,
    val style: UiStyle = UiStyle.SHEET
)

@Serializable
enum class UiPriority {
    @SerialName("low")
    LOW,

    @SerialName("normal")
    NORMAL,

    @SerialName("high")
    HIGH,

    @SerialName("critical")
    CRITICAL
}

@Serializable
enum class UiStyle {
    @SerialName("sheet")
    SHEET,

    @SerialName("dialog")
    DIALOG,

    @SerialName("fullscreen")
    FULLSCREEN,

    @SerialName("inline")
    INLINE
}

/**
 * Base sealed class for all UI components.
 */
@Serializable
sealed class UiComponent {
    abstract val slotName: String
    abstract val title: String?
    abstract val description: String?
}

/**
 * Text input component.
 */
@Serializable
@SerialName("text_input")
data class TextInputComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val placeholder: String? = null,
    val defaultValue: String? = null,
    val multiline: Boolean = false,
    val maxLength: Int? = null,
    val inputType: TextInputType = TextInputType.TEXT,
    val validation: TextValidation? = null
) : UiComponent()

@Serializable
enum class TextInputType {
    @SerialName("text")
    TEXT,

    @SerialName("email")
    EMAIL,

    @SerialName("phone")
    PHONE,

    @SerialName("number")
    NUMBER,

    @SerialName("password")
    PASSWORD,

    @SerialName("url")
    URL
}

@Serializable
data class TextValidation(
    val required: Boolean = true,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val errorMessage: String? = null
)

/**
 * Choice list / picker component.
 */
@Serializable
@SerialName("choice_list")
data class ChoiceListComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val options: List<ChoiceOption>,
    val multiSelect: Boolean = false,
    val defaultValue: List<String> = emptyList(),
    val searchable: Boolean = false
) : UiComponent()

@Serializable
data class ChoiceOption(
    val value: String,
    val label: String,
    val sublabel: String? = null,
    val icon: String? = null,
    val disabled: Boolean = false
)

/**
 * Confirmation dialog component.
 */
@Serializable
@SerialName("confirmation")
data class ConfirmationComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val message: String,
    val confirmLabel: String = "Confirm",
    val cancelLabel: String = "Cancel",
    val destructive: Boolean = false
) : UiComponent()

/**
 * Contact picker component.
 */
@Serializable
@SerialName("contact_picker")
data class ContactPickerComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val multiSelect: Boolean = false,
    val filter: ContactFilter? = null,
    val suggestions: List<String> = emptyList()
) : UiComponent()

@Serializable
data class ContactFilter(
    val hasPhone: Boolean? = null,
    val hasEmail: Boolean? = null,
    val group: String? = null
)

/**
 * Media picker component.
 */
@Serializable
@SerialName("media_picker")
data class MediaPickerComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val mediaTypes: List<MediaType> = listOf(MediaType.IMAGE),
    val multiSelect: Boolean = true,
    val maxItems: Int? = null,
    val preselect: MediaPreselect? = null
) : UiComponent()

@Serializable
enum class MediaType {
    @SerialName("image")
    IMAGE,

    @SerialName("video")
    VIDEO,

    @SerialName("audio")
    AUDIO,

    @SerialName("document")
    DOCUMENT
}

@Serializable
data class MediaPreselect(
    val count: Int,
    val order: String = "newest"
)

/**
 * Date/time picker component.
 */
@Serializable
@SerialName("datetime_picker")
data class DateTimePickerComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val mode: DateTimeMode = DateTimeMode.DATE,
    val minDate: String? = null,
    val maxDate: String? = null,
    val defaultValue: String? = null
) : UiComponent()

@Serializable
enum class DateTimeMode {
    @SerialName("date")
    DATE,

    @SerialName("time")
    TIME,

    @SerialName("datetime")
    DATETIME
}

/**
 * Form component containing multiple fields.
 */
@Serializable
@SerialName("form")
data class FormComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val fields: List<UiComponent>,
    val submitLabel: String = "Submit"
) : UiComponent()

/**
 * Table editor component.
 */
@Serializable
@SerialName("table")
data class TableComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val columns: List<TableColumn>,
    val rows: List<Map<String, @Serializable(with = AnySerializer::class) Any?>> = emptyList(),
    val allowAdd: Boolean = true,
    val allowDelete: Boolean = true,
    val allowEdit: Boolean = true
) : UiComponent()

@Serializable
data class TableColumn(
    val key: String,
    val label: String,
    val type: SlotType = SlotType.STRING,
    val width: Float? = null,
    val editable: Boolean = true
)

/**
 * Result display component.
 */
@Serializable
@SerialName("result")
data class ResultComponent(
    override val slotName: String,
    override val title: String? = null,
    override val description: String? = null,
    val success: Boolean,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val actions: List<ResultAction> = emptyList()
) : UiComponent()

@Serializable
data class ResultAction(
    val id: String,
    val label: String,
    val primary: Boolean = false
)

/**
 * Response from UI interaction.
 */
@Serializable
data class UiResponse(
    val schemaId: String,
    val slotName: String,
    val status: UiResponseStatus,
    val value: @Serializable(with = AnySerializer::class) Any? = null,
    val metadata: UiResponseMetadata? = null
)

@Serializable
enum class UiResponseStatus {
    @SerialName("completed")
    COMPLETED,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("timeout")
    TIMEOUT,

    @SerialName("error")
    ERROR
}

@Serializable
data class UiResponseMetadata(
    val inputMethod: String? = null,
    val duration: Long? = null
)
