package dev.agentos.llm.domain

/**
 * Prompt templates for LLM interactions.
 * 
 * These templates ensure consistent, structured output from the LLM.
 */
object PromptTemplates {

    /**
     * System prompt for intent parsing.
     */
    val INTENT_PARSING_SYSTEM = """
You are the Intent Understanding Engine for Agent OS, a task-driven mobile operating system.

Your job is to convert natural language user input into a structured Task JSON object.

## Output Format

You MUST respond with valid JSON only. No explanations, no markdown, just JSON.

```json
{
  "intent": {
    "domain": "<domain>",
    "action": "<action>",
    "confidence": <0.0-1.0>
  },
  "slots": {
    "<slot_name>": {
      "name": "<slot_name>",
      "type": "<slot_type>",
      "required": true|false,
      "value": <extracted_value or null>,
      "resolved": true|false
    }
  }
}
```

## Available Domains and Actions

- messaging: send_text, send_media, start_call, video_call
- notes: create, create_table, edit, delete, search
- transport: book_ride, get_directions, check_eta
- calendar: create_event, check_schedule, set_reminder
- media: play_music, take_photo, share
- settings: change_setting, toggle_feature
- apps: open_app, install_app, uninstall_app
- contacts: add_contact, find_contact, call_contact
- web: search, open_url

## Slot Types

- string, number, boolean
- contact, contacts (for people)
- media (for photos, videos)
- location, address
- date, datetime
- enum (for choices)

## Rules

1. Extract as much information as possible from the user input
2. Set "resolved": true only if the value is explicitly provided
3. Set "value": null if information is not provided
4. Use confidence < 0.7 if the intent is ambiguous
5. Always include required slots even if not resolved

## Examples

User: "Send hi to mom"
{
  "intent": {"domain": "messaging", "action": "send_text", "confidence": 0.95},
  "slots": {
    "recipient": {"name": "recipient", "type": "contact", "required": true, "value": "mom", "resolved": true},
    "message": {"name": "message", "type": "string", "required": true, "value": "hi", "resolved": true},
    "app": {"name": "app", "type": "string", "required": false, "value": null, "resolved": false}
  }
}

User: "Book a cab"
{
  "intent": {"domain": "transport", "action": "book_ride", "confidence": 0.9},
  "slots": {
    "destination": {"name": "destination", "type": "address", "required": true, "value": null, "resolved": false},
    "pickup": {"name": "pickup", "type": "address", "required": false, "value": null, "resolved": false},
    "rideType": {"name": "rideType", "type": "enum", "required": false, "value": null, "resolved": false}
  }
}
    """.trimIndent()

    /**
     * Template for slot question generation.
     */
    fun slotQuestionPrompt(taskDescription: String, slotName: String, slotType: String): String = """
Generate a brief, natural question to ask the user for the missing information.

Task: $taskDescription
Missing slot: $slotName
Slot type: $slotType

Rules:
- Keep it conversational and brief
- Don't mention technical terms like "slot"
- Make it sound like a helpful assistant

Respond with ONLY the question, nothing else.
    """.trimIndent()

    /**
     * Template for confirmation message generation.
     */
    fun confirmationPrompt(taskSummary: String): String = """
Generate a brief confirmation message for the user.

Task: $taskSummary

Rules:
- Summarize what will be done
- Keep it under 50 words
- End with asking for confirmation
- Be conversational

Respond with ONLY the confirmation message, nothing else.
    """.trimIndent()

    /**
     * Template for completion message generation.
     */
    fun completionPrompt(taskSummary: String, success: Boolean): String = """
Generate a brief completion message for the user.

Task: $taskSummary
Success: $success

Rules:
- If successful, confirm what was done
- If failed, briefly explain and suggest next steps
- Keep it under 30 words
- Be conversational

Respond with ONLY the message, nothing else.
    """.trimIndent()

    /**
     * Template for intent extraction with context.
     */
    fun intentParsingUserPrompt(userInput: String, context: IntentContext): String {
        val contextInfo = buildString {
            if (context.conversationHistory.isNotEmpty()) {
                appendLine("Previous conversation:")
                context.conversationHistory.takeLast(3).forEach { msg ->
                    appendLine("${msg.role}: ${msg.content}")
                }
                appendLine()
            }
        }
        
        return """
$contextInfo
Current user input: "$userInput"

Parse this into a Task JSON object.
        """.trimIndent()
    }
}
