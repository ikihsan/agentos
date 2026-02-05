# System Architecture Overview

> **Status:** Draft  
> **Phase:** 0

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER INTERACTION LAYER                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │    Voice     │  │    Text      │  │   Gesture    │              │
│  │    Input     │  │    Input     │  │    Input     │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
└─────────┼──────────────────┼──────────────────┼─────────────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      INTENT UNDERSTANDING LAYER                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    AI Intent Classifier                       │  │
│  │  • Speech-to-Text  • NLU  • Entity Extraction  • Context     │  │
│  └──────────────────────────────────┬───────────────────────────┘  │
└─────────────────────────────────────┼───────────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         TASK RUNTIME LAYER                           │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐        │
│  │  Task Manager  │  │  Input Resolver│  │  Task Executor │        │
│  │                │  │                │  │                │        │
│  │  • Lifecycle   │  │  • Vault Query │  │  • Capability  │        │
│  │  • State       │  │  • UI Trigger  │  │    Invocation  │        │
│  │  • Queue       │  │  • Validation  │  │  • Result      │        │
│  └────────┬───────┘  └────────┬───────┘  └────────┬───────┘        │
└───────────┼───────────────────┼───────────────────┼─────────────────┘
            │                   │                   │
            └───────────────────┼───────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         PLATFORM SERVICES                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   Dynamic    │  │   Personal   │  │  Permission  │              │
│  │  UI Engine   │  │  Data Vault  │  │   Manager    │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │  Capability  │  │  Preference  │  │    Audit     │              │
│  │   Registry   │  │   Engine     │  │     Log      │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CAPABILITY ECOSYSTEM                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   WhatsApp   │  │    Uber      │  │    Notes     │   • • •      │
│  │  Capability  │  │  Capability  │  │  Capability  │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         ANDROID (AOSP)                               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Component Descriptions

### User Interaction Layer

The entry point for all user interactions.

| Component | Responsibility |
|-----------|----------------|
| Voice Input | Captures spoken commands via microphone |
| Text Input | Handles typed commands and chat |
| Gesture Input | Interprets touch gestures and shortcuts |

### Intent Understanding Layer

Transforms raw input into structured tasks.

| Component | Responsibility |
|-----------|----------------|
| Speech-to-Text | Converts audio to text |
| NLU Engine | Parses natural language into structured intent |
| Entity Extractor | Identifies entities (contacts, dates, locations) |
| Context Manager | Maintains conversation and task context |

### Task Runtime Layer

The core orchestration engine.

| Component | Responsibility |
|-----------|----------------|
| Task Manager | Creates, tracks, and manages task lifecycle |
| Input Resolver | Resolves missing inputs from vault, context, or UI |
| Task Executor | Invokes capabilities and handles results |

### Platform Services

Shared services used across the platform.

| Service | Responsibility |
|---------|----------------|
| Dynamic UI Engine | Generates temporary UI components |
| Personal Data Vault | Stores and retrieves user data |
| Permission Manager | Handles permission requests and grants |
| Capability Registry | Indexes and discovers app capabilities |
| Preference Engine | Learns and applies user preferences |
| Audit Log | Records all system actions |

### Capability Ecosystem

Third-party and system apps exposing capabilities.

Each app:
- Publishes a Capability Manifest
- Implements capability handlers
- Receives invocations from the Task Executor

---

## Data Flow

### Example: "Send last two photos to Nani on WhatsApp"

```
1. Voice Input
   └─► "Send last two photos to Nani on WhatsApp"

2. Intent Understanding
   └─► {
         intent: messaging.send_media,
         entities: {
           contact: "Nani",
           media: "last 2 photos",
           app: "WhatsApp"
         }
       }

3. Task Creation
   └─► Task {
         id: "task_123",
         status: "pending",
         missingInputs: ["media"]
       }

4. Input Resolution
   ├─► Vault: Resolve "Nani" → Contact record
   ├─► Vault: Resolve "WhatsApp" → com.whatsapp
   └─► UI: Show photo picker (last 2 pre-selected)

5. User Interaction
   └─► User confirms photo selection

6. Task Ready
   └─► Task {
         status: "ready",
         inputs: { contact, media, app }
       }

7. Capability Invocation
   └─► WhatsApp.send_message(contact, media)

8. Result
   └─► Task {
         status: "completed",
         result: { success: true, messageId: "..." }
       }

9. Learning
   └─► Preference Engine records:
       "Nani" + "photos" → WhatsApp
```

---

## Android Integration Points

### System Services

Agent OS registers as Android system services:

| Service | Purpose |
|---------|---------|
| `AgentTaskService` | Task management and execution |
| `AgentVaultService` | Personal data vault access |
| `AgentUIService` | Dynamic UI rendering |
| `AgentCapabilityService` | Capability discovery and invocation |

### Integration Methods

| Phase | Integration Level |
|-------|-------------------|
| Phase 1 | Accessibility Service + Overlay |
| Phase 2 | System App with elevated permissions |
| Phase 3 | AOSP modification / Custom ROM |

---

## Security Boundaries

```
┌─────────────────────────────────────────────────┐
│              TRUSTED ZONE (OS)                  │
│  ┌─────────────────────────────────────────┐   │
│  │  Task Runtime  │  Vault  │  Permissions │   │
│  └─────────────────────────────────────────┘   │
│                      │                          │
│              Permission Gate                    │
│                      │                          │
└──────────────────────┼──────────────────────────┘
                       │
┌──────────────────────┼──────────────────────────┐
│              UNTRUSTED ZONE (Apps)              │
│  ┌─────────────────────────────────────────┐   │
│  │    App A    │    App B    │    App C    │   │
│  └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

---

## Open Questions

- [ ] How to handle offline operation?
- [ ] Multi-device sync architecture?
- [ ] Plugin architecture for AI models?
- [ ] Performance benchmarks and targets?
