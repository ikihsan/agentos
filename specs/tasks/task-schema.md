# Task & Intent Schema Specification

> **Status:** Draft  
> **Version:** 0.1.0  
> **Phase:** 0

---

## Overview

A **Task** is the fundamental unit of work in Agent OS. It represents a structured user intent that the system can understand, validate, and execute.

---

## Task Lifecycle

```
┌─────────────┐
│   CREATED   │  Task object instantiated
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   PENDING   │  Awaiting required inputs
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    READY    │  All inputs satisfied
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  EXECUTING  │  Capability invoked
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  COMPLETED  │  Success / Failed / Cancelled
└─────────────┘
```

---

## Task Schema (JSON Schema)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://agentos.dev/schemas/task/v1",
  "title": "Task",
  "type": "object",
  "required": ["id", "intent", "status", "createdAt"],
  "properties": {
    "id": {
      "type": "string",
      "format": "uuid",
      "description": "Unique task identifier"
    },
    "intent": {
      "$ref": "#/$defs/Intent"
    },
    "status": {
      "type": "string",
      "enum": ["created", "pending", "ready", "executing", "completed", "failed", "cancelled"]
    },
    "inputs": {
      "type": "object",
      "description": "Resolved input parameters",
      "additionalProperties": true
    },
    "missingInputs": {
      "type": "array",
      "items": { "type": "string" },
      "description": "List of required inputs not yet provided"
    },
    "result": {
      "$ref": "#/$defs/TaskResult"
    },
    "context": {
      "$ref": "#/$defs/TaskContext"
    },
    "createdAt": {
      "type": "string",
      "format": "date-time"
    },
    "updatedAt": {
      "type": "string",
      "format": "date-time"
    }
  },
  "$defs": {
    "Intent": {
      "type": "object",
      "required": ["domain", "action"],
      "properties": {
        "domain": {
          "type": "string",
          "description": "Category domain (e.g., messaging, notes, transport)"
        },
        "action": {
          "type": "string",
          "description": "Specific action (e.g., send, create, book)"
        },
        "confidence": {
          "type": "number",
          "minimum": 0,
          "maximum": 1,
          "description": "AI confidence score for intent classification"
        }
      }
    },
    "TaskResult": {
      "type": "object",
      "properties": {
        "success": { "type": "boolean" },
        "data": { "type": "object" },
        "error": { "$ref": "#/$defs/TaskError" }
      }
    },
    "TaskError": {
      "type": "object",
      "properties": {
        "code": { "type": "string" },
        "message": { "type": "string" },
        "recoverable": { "type": "boolean" }
      }
    },
    "TaskContext": {
      "type": "object",
      "properties": {
        "source": {
          "type": "string",
          "enum": ["voice", "text", "gesture", "automation"]
        },
        "rawInput": { "type": "string" },
        "sessionId": { "type": "string" },
        "parentTaskId": { "type": "string" }
      }
    }
  }
}
```

---

## Intent Namespace Convention

Intents follow a hierarchical namespace:

```
<domain>.<action>[.<variant>]
```

### Standard Domains

| Domain | Description |
|--------|-------------|
| `messaging` | Send messages, media, calls |
| `notes` | Create, edit, organize notes |
| `transport` | Book rides, navigation |
| `media` | Photos, videos, music |
| `calendar` | Events, reminders |
| `contacts` | Contact management |
| `settings` | Device configuration |
| `apps` | App installation, management |
| `payments` | Transactions, transfers |

### Example Intents

```
messaging.send_text
messaging.send_media
messaging.start_call
notes.create
notes.create_table
transport.book_ride
media.share
calendar.create_event
```

---

## Input Resolution

Inputs can be resolved from multiple sources:

1. **Extracted from utterance** — AI parses user speech
2. **Personal Data Vault** — Contacts, addresses, preferences
3. **Context inference** — Time, location, recent activity
4. **User prompt** — Dynamic UI requests missing input

### Input Priority

```
1. Explicit user input (highest)
2. Recent context
3. Personal Data Vault
4. Default values (lowest)
```

---

## Example Task Object

```json
{
  "id": "task_01HXYZ",
  "intent": {
    "domain": "messaging",
    "action": "send_media",
    "confidence": 0.94
  },
  "status": "pending",
  "inputs": {
    "contact": {
      "resolved": true,
      "value": { "name": "Nani", "phone": "+1234567890" }
    },
    "media": {
      "resolved": false,
      "constraint": { "type": "photo", "count": 2, "recency": "last" }
    },
    "app": {
      "resolved": true,
      "value": "com.whatsapp"
    }
  },
  "missingInputs": ["media"],
  "context": {
    "source": "voice",
    "rawInput": "Send the last two photos to Nani on WhatsApp",
    "sessionId": "session_abc123"
  },
  "createdAt": "2026-02-06T10:30:00Z"
}
```

---

## Open Questions

- [ ] How to handle compound tasks (multi-step)?
- [ ] Task dependency graph for complex workflows?
- [ ] Rollback strategy for failed multi-step tasks?
- [ ] Task template inheritance model?
