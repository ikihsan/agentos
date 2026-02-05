# Capability Manifest Specification

> **Status:** Draft  
> **Version:** 0.1.0  
> **Phase:** 0

---

## Overview

A **Capability Manifest** declares what an app can do. It allows Agent OS to discover, understand, and invoke app functionality programmatically.

Apps transition from UI-centric applications to **capability providers**.

---

## Design Principles

1. **Declarative** — Apps declare capabilities, not how to invoke them
2. **Discoverable** — OS can index and search capabilities
3. **Versioned** — Breaking changes require version bumps
4. **Permission-aware** — Capabilities declare required permissions
5. **AI-friendly** — Natural language descriptions for intent matching

---

## Manifest Location

```
<app_package>/
├── AndroidManifest.xml
└── agentos/
    └── capabilities.json      ← Capability Manifest
```

---

## Manifest Schema (JSON Schema)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://agentos.dev/schemas/capability-manifest/v1",
  "title": "CapabilityManifest",
  "type": "object",
  "required": ["manifestVersion", "package", "capabilities"],
  "properties": {
    "manifestVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$",
      "description": "Manifest schema version"
    },
    "package": {
      "type": "string",
      "description": "Android package name"
    },
    "displayName": {
      "type": "string",
      "description": "Human-readable app name"
    },
    "capabilities": {
      "type": "array",
      "items": { "$ref": "#/$defs/Capability" }
    }
  },
  "$defs": {
    "Capability": {
      "type": "object",
      "required": ["id", "intents", "description", "handler"],
      "properties": {
        "id": {
          "type": "string",
          "pattern": "^[a-z][a-z0-9_]*$",
          "description": "Unique capability identifier within the app"
        },
        "intents": {
          "type": "array",
          "items": { "type": "string" },
          "description": "List of intent patterns this capability handles"
        },
        "description": {
          "type": "string",
          "description": "Natural language description for AI matching"
        },
        "keywords": {
          "type": "array",
          "items": { "type": "string" },
          "description": "Additional keywords for discovery"
        },
        "inputs": {
          "type": "array",
          "items": { "$ref": "#/$defs/InputParameter" }
        },
        "outputs": {
          "type": "array",
          "items": { "$ref": "#/$defs/OutputParameter" }
        },
        "handler": {
          "$ref": "#/$defs/Handler"
        },
        "permissions": {
          "type": "array",
          "items": { "type": "string" }
        },
        "uiRequired": {
          "type": "boolean",
          "default": false,
          "description": "Whether capability requires user-visible UI"
        }
      }
    },
    "InputParameter": {
      "type": "object",
      "required": ["name", "type"],
      "properties": {
        "name": { "type": "string" },
        "type": { "$ref": "#/$defs/DataType" },
        "required": { "type": "boolean", "default": true },
        "description": { "type": "string" },
        "vaultKey": {
          "type": "string",
          "description": "Personal Data Vault key for auto-fill"
        }
      }
    },
    "OutputParameter": {
      "type": "object",
      "required": ["name", "type"],
      "properties": {
        "name": { "type": "string" },
        "type": { "$ref": "#/$defs/DataType" },
        "description": { "type": "string" }
      }
    },
    "DataType": {
      "type": "string",
      "enum": [
        "string", "number", "boolean", "date", "datetime",
        "contact", "contacts", "media", "location", "address",
        "currency", "phone", "email", "url", "file", "object", "array"
      ]
    },
    "Handler": {
      "type": "object",
      "required": ["type"],
      "properties": {
        "type": {
          "type": "string",
          "enum": ["intent", "service", "broadcast", "deeplink"]
        },
        "target": {
          "type": "string",
          "description": "Intent action, service class, or deeplink template"
        },
        "extras": {
          "type": "object",
          "description": "Static extras to include"
        }
      }
    }
  }
}
```

---

## Example Manifest: WhatsApp

```json
{
  "manifestVersion": "1.0",
  "package": "com.whatsapp",
  "displayName": "WhatsApp",
  "capabilities": [
    {
      "id": "send_message",
      "intents": ["messaging.send_text", "messaging.send_media"],
      "description": "Send a text message or media to a contact via WhatsApp",
      "keywords": ["whatsapp", "message", "text", "photo", "video", "send"],
      "inputs": [
        {
          "name": "recipient",
          "type": "contact",
          "required": true,
          "description": "The person to send the message to",
          "vaultKey": "contacts"
        },
        {
          "name": "text",
          "type": "string",
          "required": false,
          "description": "Text content of the message"
        },
        {
          "name": "media",
          "type": "media",
          "required": false,
          "description": "Photos or videos to attach"
        }
      ],
      "outputs": [
        {
          "name": "messageId",
          "type": "string",
          "description": "ID of the sent message"
        }
      ],
      "handler": {
        "type": "intent",
        "target": "com.whatsapp.SEND_MESSAGE"
      },
      "permissions": ["contacts", "media.read"],
      "uiRequired": false
    },
    {
      "id": "start_call",
      "intents": ["messaging.start_call", "messaging.video_call"],
      "description": "Start a voice or video call with a contact",
      "keywords": ["call", "video", "voice", "phone"],
      "inputs": [
        {
          "name": "recipient",
          "type": "contact",
          "required": true,
          "vaultKey": "contacts"
        },
        {
          "name": "callType",
          "type": "string",
          "required": false,
          "description": "voice or video"
        }
      ],
      "handler": {
        "type": "deeplink",
        "target": "whatsapp://call?phone={{recipient.phone}}&video={{callType == 'video'}}"
      },
      "permissions": ["contacts", "microphone", "camera"],
      "uiRequired": true
    }
  ]
}
```

---

## Capability Discovery

### Registration

Apps register capabilities at:
- Install time
- Update time
- Runtime re-registration

### Indexing

Agent OS maintains a **Capability Index**:

```
Intent Pattern → [Capability, Capability, ...]
```

### Resolution

When a Task needs execution:

1. Match intent to capabilities
2. Rank by user preference, recency, and AI confidence
3. Select capability (or ask user)
4. Invoke handler

---

## Capability Invocation Protocol

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│  Task       │ ───► │  Capability  │ ───► │    App      │
│  Runtime    │      │  Resolver    │      │   Handler   │
└─────────────┘      └──────────────┘      └─────────────┘
       │                                          │
       │◄─────────────── Result ◄────────────────┘
```

### Invocation Contract

```kotlin
interface CapabilityHandler {
    suspend fun execute(
        capabilityId: String,
        inputs: Map<String, Any>,
        context: ExecutionContext
    ): CapabilityResult
}
```

---

## Open Questions

- [ ] Capability versioning and deprecation strategy?
- [ ] Fallback when preferred app unavailable?
- [ ] Capability composition (chaining multiple)?
- [ ] Sandbox/permission model for capability invocation?
