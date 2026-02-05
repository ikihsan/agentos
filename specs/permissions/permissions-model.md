# Permissions Model Specification

> **Status:** Draft  
> **Version:** 0.1.0  
> **Phase:** 0

---

## Overview

The Agent OS permissions model governs how apps, capabilities, and the AI access user data and system resources.

The model is designed for an AI-first world where:
- Tasks may span multiple apps
- Data flows through the OS, not directly between apps
- User intent should reduce friction while maintaining security

---

## Design Principles

1. **Intent-aware** — Permissions granted in context of user intent
2. **Minimal exposure** — Apps see only what they need
3. **Transparent** — Users understand what's being accessed
4. **Revocable** — Permissions can be withdrawn at any time
5. **Auditable** — All access is logged

---

## Permission Categories

### System Permissions

Traditional Android-style permissions for hardware and OS features.

| Permission | Description |
|------------|-------------|
| `camera` | Access device camera |
| `microphone` | Access device microphone |
| `location` | Access device location |
| `storage` | Access device storage |
| `notifications` | Send notifications |
| `background` | Run in background |
| `overlay` | Display over other apps |

### Vault Permissions

Access to Personal Data Vault categories.

| Permission | Description |
|------------|-------------|
| `vault.identity.read` | Read user identity |
| `vault.contacts.read` | Read contacts |
| `vault.contacts.write` | Modify contacts |
| `vault.addresses.read` | Read addresses |
| `vault.payments.read` | Read payment methods |
| `vault.preferences.read` | Read preferences |
| `vault.preferences.write` | Modify preferences |
| `vault.history.read` | Read interaction history |

### Capability Permissions

Permissions required to invoke specific capabilities.

| Permission | Description |
|------------|-------------|
| `capability.messaging` | Send messages |
| `capability.calls` | Make calls |
| `capability.payments` | Process payments |
| `capability.transport` | Book transport |

---

## Permission Scopes

### Temporal Scopes

| Scope | Duration | Use Case |
|-------|----------|----------|
| `once` | Single use | Sensitive one-time action |
| `session` | Current task session | Multi-step task |
| `temporary` | Time-limited (1h, 24h) | Trusted app, limited time |
| `persistent` | Until revoked | Core functionality |

### Data Scopes

| Scope | Access Level |
|-------|--------------|
| `specific` | Specific records only (e.g., one contact) |
| `filtered` | Subset matching criteria |
| `full` | All records in category |

---

## Permission Request Flow

### Standard Flow

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│     App      │ ───► │   Request    │ ───► │    User      │
│   Requests   │      │   Evaluated  │      │   Prompted   │
└──────────────┘      └──────────────┘      └──────────────┘
                                                   │
                             ┌─────────────────────┴─────────────────────┐
                             ▼                                           ▼
                      ┌──────────────┐                            ┌──────────────┐
                      │   Granted    │                            │    Denied    │
                      └──────────────┘                            └──────────────┘
```

### Intent-Aware Flow (Agent OS Enhancement)

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│    User      │ ───► │    Task      │ ───► │  Permission  │
│   Intent     │      │   Created    │      │   Inferred   │
└──────────────┘      └──────────────┘      └──────────────┘
                                                   │
                                                   ▼
                                            ┌──────────────┐
                                            │   Implicit   │
                                            │   Consent?   │
                                            └──────┬───────┘
                             ┌─────────────────────┴─────────────────────┐
                             ▼                                           ▼
                      ┌──────────────┐                            ┌──────────────┐
                      │   Auto-Grant │                            │   Explicit   │
                      │   (scoped)   │                            │   Prompt     │
                      └──────────────┘                            └──────────────┘
```

---

## Implicit Consent Model

When user intent implies permission, reduce friction.

### Example: "Send the last two photos to Nani"

**Intent implies:**
- Read contact "Nani" (specific record)
- Read last 2 photos (specific records)
- Send via messaging app (capability)

**Implicit consent:**
- Contact access → Granted (specific: Nani)
- Photo access → Granted (specific: last 2)
- Messaging capability → Granted (this task)

**No prompt needed** — user intent is the consent.

### Implicit Consent Rules

| Condition | Auto-Grant |
|-----------|------------|
| User explicitly named entity | Access to that specific entity |
| User specified action | Capability for that action |
| User confirmed task | Execution permissions |
| User has prior pattern | Matching preferences |

### Explicit Consent Required

| Condition | Prompt Required |
|-----------|-----------------|
| Payment or financial action | Always |
| Destructive action (delete) | Always |
| Broad data access | Always |
| First-time app | Always |
| Sensitive data categories | Configurable |

---

## Permission Request Schema

```json
{
  "$id": "https://agentos.dev/schemas/permission/request/v1",
  "type": "object",
  "required": ["requestId", "requester", "permissions"],
  "properties": {
    "requestId": { "type": "string" },
    "requester": {
      "type": "object",
      "properties": {
        "type": { "enum": ["app", "capability", "system"] },
        "id": { "type": "string" },
        "name": { "type": "string" }
      }
    },
    "taskId": { 
      "type": "string",
      "description": "Associated task for context"
    },
    "permissions": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "permission": { "type": "string" },
          "scope": {
            "type": "object",
            "properties": {
              "temporal": { "enum": ["once", "session", "temporary", "persistent"] },
              "data": { "enum": ["specific", "filtered", "full"] },
              "filter": { "type": "object" }
            }
          },
          "reason": { "type": "string" }
        }
      }
    }
  }
}
```

---

## Permission Grant Schema

```json
{
  "$id": "https://agentos.dev/schemas/permission/grant/v1",
  "type": "object",
  "properties": {
    "grantId": { "type": "string" },
    "requestId": { "type": "string" },
    "grantee": { "type": "string" },
    "permission": { "type": "string" },
    "scope": {
      "type": "object",
      "properties": {
        "temporal": { "type": "string" },
        "data": { "type": "string" },
        "filter": { "type": "object" },
        "expiresAt": { "type": "string", "format": "date-time" }
      }
    },
    "grantedAt": { "type": "string", "format": "date-time" },
    "grantedBy": { "enum": ["user", "implicit", "system"] },
    "taskContext": { "type": "string" }
  }
}
```

---

## Permission UI

### Prompt Design

```
┌─────────────────────────────────────┐
│                                     │
│   📸 Allow access to photos?        │
│                                     │
│   "Send last 2 photos to Nani"      │
│   needs access to your photos.      │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🖼️  🖼️  (2 photos selected) │   │
│   └─────────────────────────────┘   │
│                                     │
│   [ Only these ]  [ All photos ]    │
│                                     │
│         [ Don't allow ]             │
│                                     │
└─────────────────────────────────────┘
```

### Permission Levels in Prompt

1. **Specific** — Only the items shown
2. **Filtered** — Items matching criteria
3. **Full** — All items in category
4. **Deny** — No access

---

## Audit Log

All permission checks are logged:

```json
{
  "timestamp": "2026-02-06T10:30:00Z",
  "action": "permission_check",
  "requester": "com.whatsapp",
  "permission": "vault.contacts.read",
  "scope": { "data": "specific", "filter": { "name": "Nani" } },
  "taskId": "task_01HXYZ",
  "result": "granted",
  "grantType": "implicit"
}
```

---

## Permission Management API

```kotlin
interface PermissionManager {
    // Check
    suspend fun check(permission: String, scope: Scope): PermissionStatus
    
    // Request
    suspend fun request(request: PermissionRequest): PermissionResult
    
    // Revoke
    suspend fun revoke(grantId: String)
    suspend fun revokeAll(grantee: String)
    
    // Query
    suspend fun getGrants(grantee: String): List<PermissionGrant>
    suspend fun getAuditLog(filter: AuditFilter): List<AuditEntry>
}
```

---

## Open Questions

- [ ] How to handle permission inheritance in compound tasks?
- [ ] Permission delegation between apps?
- [ ] Parental controls integration?
- [ ] Enterprise/MDM policy support?
- [ ] Permission analytics for users?
