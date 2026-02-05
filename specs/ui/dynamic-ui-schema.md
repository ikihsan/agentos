# Dynamic UI Schema Specification

> **Status:** Draft  
> **Version:** 0.1.0  
> **Phase:** 0

---

## Overview

The **Dynamic UI Engine** generates temporary, context-aware UI surfaces when user input is required during task execution.

UI is owned by the OS, not by individual apps.

---

## Design Principles

1. **Task-driven** — UI appears only when needed
2. **Temporary** — UI disappears after input is collected
3. **Consistent** — Same look and feel across all tasks
4. **Declarative** — Components declared, not coded
5. **Accessible** — Voice and touch input supported

---

## UI Flow Model

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│    Task      │ ───► │   UI Need    │ ───► │   Generate   │
│   Runtime    │      │   Detected   │      │     UI       │
└──────────────┘      └──────────────┘      └──────────────┘
                                                   │
                                                   ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│    Resume    │ ◄─── │   Collect    │ ◄─── │   Display    │
│    Task      │      │   Input      │      │     UI       │
└──────────────┘      └──────────────┘      └──────────────┘
```

---

## Component Library

### Core Components

| Component | Purpose | Input Type |
|-----------|---------|------------|
| `ContactPicker` | Select one or more contacts | `contact`, `contacts` |
| `MediaPicker` | Select photos, videos, files | `media`, `file` |
| `DatePicker` | Select date or time | `date`, `datetime` |
| `LocationPicker` | Select or enter location | `location`, `address` |
| `TextInput` | Enter text | `string` |
| `NumberInput` | Enter number | `number`, `currency` |
| `ChoiceList` | Select from options | `enum` |
| `Confirmation` | Confirm action | `boolean` |
| `TableEditor` | Edit structured data | `table` |
| `RichEditor` | Format text content | `richtext` |

---

## UI Request Schema

```json
{
  "$id": "https://agentos.dev/schemas/ui/request/v1",
  "type": "object",
  "required": ["taskId", "component", "field"],
  "properties": {
    "taskId": {
      "type": "string",
      "description": "Associated task ID"
    },
    "component": {
      "type": "string",
      "enum": [
        "ContactPicker", "MediaPicker", "DatePicker", 
        "LocationPicker", "TextInput", "NumberInput",
        "ChoiceList", "Confirmation", "TableEditor", "RichEditor"
      ]
    },
    "field": {
      "type": "string",
      "description": "Task input field to populate"
    },
    "config": {
      "$ref": "#/$defs/ComponentConfig"
    },
    "context": {
      "$ref": "#/$defs/UIContext"
    }
  },
  "$defs": {
    "ComponentConfig": {
      "type": "object",
      "properties": {
        "title": { "type": "string" },
        "prompt": { "type": "string" },
        "multiSelect": { "type": "boolean" },
        "minItems": { "type": "integer" },
        "maxItems": { "type": "integer" },
        "filter": { "type": "object" },
        "suggestions": { "type": "array" },
        "defaultValue": {},
        "validation": { "$ref": "#/$defs/Validation" }
      }
    },
    "UIContext": {
      "type": "object",
      "properties": {
        "priority": {
          "type": "string",
          "enum": ["low", "normal", "high", "critical"]
        },
        "timeout": { "type": "integer" },
        "voiceEnabled": { "type": "boolean", "default": true },
        "style": {
          "type": "string",
          "enum": ["sheet", "dialog", "fullscreen", "inline"]
        }
      }
    },
    "Validation": {
      "type": "object",
      "properties": {
        "required": { "type": "boolean" },
        "pattern": { "type": "string" },
        "minLength": { "type": "integer" },
        "maxLength": { "type": "integer" },
        "min": { "type": "number" },
        "max": { "type": "number" }
      }
    }
  }
}
```

---

## UI Response Schema

```json
{
  "$id": "https://agentos.dev/schemas/ui/response/v1",
  "type": "object",
  "required": ["taskId", "field", "status"],
  "properties": {
    "taskId": { "type": "string" },
    "field": { "type": "string" },
    "status": {
      "type": "string",
      "enum": ["completed", "cancelled", "timeout", "error"]
    },
    "value": {
      "description": "The collected input value"
    },
    "metadata": {
      "type": "object",
      "properties": {
        "inputMethod": {
          "type": "string",
          "enum": ["touch", "voice", "keyboard"]
        },
        "duration": { "type": "integer" },
        "selections": { "type": "integer" }
      }
    }
  }
}
```

---

## Component Specifications

### ContactPicker

```json
{
  "component": "ContactPicker",
  "field": "recipient",
  "config": {
    "title": "Who do you want to message?",
    "prompt": "Select a contact",
    "multiSelect": false,
    "filter": {
      "hasPhone": true,
      "appHandle": "com.whatsapp"
    },
    "suggestions": ["recent", "frequent"]
  }
}
```

**Output:**
```json
{
  "value": {
    "id": "contact_123",
    "displayName": "Nani",
    "phone": "+1234567890"
  }
}
```

---

### MediaPicker

```json
{
  "component": "MediaPicker",
  "field": "photos",
  "config": {
    "title": "Select photos to send",
    "multiSelect": true,
    "maxItems": 10,
    "filter": {
      "mediaType": ["image"],
      "recency": "7d"
    },
    "preselect": {
      "count": 2,
      "order": "newest"
    }
  }
}
```

**Output:**
```json
{
  "value": [
    { "uri": "content://media/123", "type": "image/jpeg" },
    { "uri": "content://media/124", "type": "image/jpeg" }
  ]
}
```

---

### ChoiceList

```json
{
  "component": "ChoiceList",
  "field": "rideType",
  "config": {
    "title": "What type of ride?",
    "options": [
      { "value": "economy", "label": "Economy", "sublabel": "₹150" },
      { "value": "premium", "label": "Premium", "sublabel": "₹250" },
      { "value": "xl", "label": "XL", "sublabel": "₹300" }
    ],
    "defaultValue": "economy"
  }
}
```

---

### Confirmation

```json
{
  "component": "Confirmation",
  "field": "confirmed",
  "config": {
    "title": "Send message?",
    "prompt": "Send 2 photos to Nani on WhatsApp",
    "confirmLabel": "Send",
    "cancelLabel": "Cancel",
    "destructive": false
  }
}
```

---

### TableEditor

```json
{
  "component": "TableEditor",
  "field": "groceryList",
  "config": {
    "title": "Grocery List",
    "columns": [
      { "key": "item", "label": "Item", "type": "string" },
      { "key": "quantity", "label": "Qty", "type": "number" },
      { "key": "bought", "label": "✓", "type": "boolean" }
    ],
    "initialRows": [
      { "item": "Milk", "quantity": 2, "bought": false },
      { "item": "Eggs", "quantity": 12, "bought": false }
    ],
    "allowAdd": true,
    "allowDelete": true
  }
}
```

---

## Voice Integration

Each component supports voice input:

| Component | Voice Example |
|-----------|---------------|
| ContactPicker | "Select Nani" |
| MediaPicker | "The first two" / "All of them" |
| DatePicker | "Tomorrow at 3pm" |
| ChoiceList | "Premium" |
| Confirmation | "Yes" / "Send it" |
| TextInput | Dictation |

### Voice Grammar

```json
{
  "component": "Confirmation",
  "voiceGrammar": {
    "confirm": ["yes", "ok", "sure", "send", "do it", "confirm"],
    "cancel": ["no", "cancel", "never mind", "stop"]
  }
}
```

---

## Presentation Styles

| Style | Description | Use Case |
|-------|-------------|----------|
| `sheet` | Bottom sheet overlay | Contact picker, choices |
| `dialog` | Center dialog | Confirmation, simple input |
| `fullscreen` | Full screen UI | Media picker, table editor |
| `inline` | Embedded in conversation | Quick text input |

---

## Open Questions

- [ ] Custom component registration by apps?
- [ ] Theming and branding options?
- [ ] Animation and transition standards?
- [ ] Accessibility requirements (screen readers)?
- [ ] Landscape and tablet layouts?
