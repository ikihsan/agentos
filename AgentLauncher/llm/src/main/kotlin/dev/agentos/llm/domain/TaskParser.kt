package dev.agentos.llm.domain

import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.Result
import dev.agentos.core.domain.model.*
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses LLM responses into Task objects.
 */
@Singleton
class TaskParser @Inject constructor(
    private val json: Json
) {
    /**
     * Parses a JSON string response from the LLM into a Task object.
     */
    fun parse(jsonResponse: String, context: TaskContext): Result<Task> {
        return try {
            // Clean up the response - LLMs sometimes wrap in markdown
            val cleanJson = cleanJsonResponse(jsonResponse)
            
            AgentLogger.d(AgentLogger.TAG_LLM, "Parsing JSON: $cleanJson")
            
            val parsed = json.decodeFromString<ParsedIntentResponse>(cleanJson)
            
            val task = Task(
                intent = Intent(
                    domain = parsed.intent.domain,
                    action = parsed.intent.action,
                    confidence = parsed.intent.confidence
                ),
                slots = parsed.slots.mapValues { (_, slot) ->
                    Slot(
                        name = slot.name,
                        type = parseSlotType(slot.type),
                        required = slot.required,
                        value = slot.value,
                        resolved = slot.resolved,
                        description = slot.description
                    )
                },
                context = context,
                status = TaskStatus.PENDING
            )
            
            Result.success(task)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_LLM, "Failed to parse task JSON", e)
            Result.error(e)
        }
    }

    /**
     * Cleans up JSON response from LLM.
     */
    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()
        
        // Remove markdown code blocks if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        
        return cleaned.trim()
    }

    /**
     * Parses string slot type to enum.
     */
    private fun parseSlotType(typeStr: String): SlotType {
        return when (typeStr.lowercase()) {
            "string" -> SlotType.STRING
            "number" -> SlotType.NUMBER
            "boolean" -> SlotType.BOOLEAN
            "date" -> SlotType.DATE
            "datetime" -> SlotType.DATETIME
            "contact" -> SlotType.CONTACT
            "contacts" -> SlotType.CONTACTS
            "media" -> SlotType.MEDIA
            "location" -> SlotType.LOCATION
            "address" -> SlotType.ADDRESS
            "currency" -> SlotType.CURRENCY
            "enum" -> SlotType.ENUM
            "object" -> SlotType.OBJECT
            "array" -> SlotType.ARRAY
            else -> SlotType.STRING
        }
    }
}

/**
 * Internal model for parsing LLM response.
 */
@kotlinx.serialization.Serializable
internal data class ParsedIntentResponse(
    val intent: ParsedIntent,
    val slots: Map<String, ParsedSlot>
)

@kotlinx.serialization.Serializable
internal data class ParsedIntent(
    val domain: String,
    val action: String,
    val confidence: Float = 1.0f
)

@kotlinx.serialization.Serializable
internal data class ParsedSlot(
    val name: String,
    val type: String,
    val required: Boolean = true,
    val value: @kotlinx.serialization.Serializable(with = AnySerializer::class) Any? = null,
    val resolved: Boolean = false,
    val description: String? = null
)
