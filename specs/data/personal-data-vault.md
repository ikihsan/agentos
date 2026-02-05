# Personal Data Vault Schema

> **Status:** Draft  
> **Version:** 0.1.0  
> **Phase:** 0

---

## Overview

The **Personal Data Vault** is a shared, permission-controlled data layer that serves as the single source of truth for user data.

Apps request access to vault data instead of storing duplicates.

---

## Design Principles

1. **User-owned** — All data belongs to the user
2. **Permission-gated** — Apps must request access
3. **Structured** — Consistent schema across the platform
4. **Sync-capable** — Supports cloud backup and cross-device sync
5. **Privacy-first** — Minimal exposure, granular permissions

---

## Data Categories

### Core Categories

| Category | Description | Examples |
|----------|-------------|----------|
| `identity` | User profile information | Name, photo, birthday |
| `contacts` | People the user knows | Friends, family, colleagues |
| `addresses` | Physical locations | Home, work, favorites |
| `payments` | Payment methods | Cards, wallets, UPI |
| `preferences` | User settings and choices | Language, defaults, habits |
| `history` | Interaction history | Recent contacts, places |
| `credentials` | Auth tokens and passwords | App logins (encrypted) |

---

## Schema Definitions

### Identity

```json
{
  "$id": "https://agentos.dev/schemas/vault/identity/v1",
  "type": "object",
  "properties": {
    "firstName": { "type": "string" },
    "lastName": { "type": "string" },
    "displayName": { "type": "string" },
    "photo": { "type": "string", "format": "uri" },
    "birthday": { "type": "string", "format": "date" },
    "email": { "type": "string", "format": "email" },
    "phone": { "type": "string" },
    "pronouns": { "type": "string" }
  }
}
```

### Contact

```json
{
  "$id": "https://agentos.dev/schemas/vault/contact/v1",
  "type": "object",
  "required": ["id", "displayName"],
  "properties": {
    "id": { "type": "string", "format": "uuid" },
    "displayName": { "type": "string" },
    "firstName": { "type": "string" },
    "lastName": { "type": "string" },
    "nicknames": { 
      "type": "array", 
      "items": { "type": "string" },
      "description": "Alternative names (Mom, Nani, Boss)"
    },
    "phones": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "number": { "type": "string" },
          "type": { "type": "string", "enum": ["mobile", "home", "work", "other"] },
          "primary": { "type": "boolean" }
        }
      }
    },
    "emails": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "address": { "type": "string", "format": "email" },
          "type": { "type": "string", "enum": ["personal", "work", "other"] },
          "primary": { "type": "boolean" }
        }
      }
    },
    "addresses": {
      "type": "array",
      "items": { "$ref": "#/$defs/Address" }
    },
    "relationship": { 
      "type": "string",
      "description": "Relationship to user (friend, family, colleague)"
    },
    "photo": { "type": "string", "format": "uri" },
    "appHandles": {
      "type": "object",
      "description": "App-specific identifiers",
      "additionalProperties": { "type": "string" }
    },
    "metadata": {
      "type": "object",
      "properties": {
        "source": { "type": "string" },
        "createdAt": { "type": "string", "format": "date-time" },
        "updatedAt": { "type": "string", "format": "date-time" },
        "lastContactedAt": { "type": "string", "format": "date-time" },
        "contactFrequency": { "type": "number" }
      }
    }
  }
}
```

### Address

```json
{
  "$id": "https://agentos.dev/schemas/vault/address/v1",
  "type": "object",
  "properties": {
    "id": { "type": "string", "format": "uuid" },
    "label": { "type": "string", "description": "Home, Work, Mom's place" },
    "street": { "type": "string" },
    "apartment": { "type": "string" },
    "city": { "type": "string" },
    "state": { "type": "string" },
    "postalCode": { "type": "string" },
    "country": { "type": "string" },
    "coordinates": {
      "type": "object",
      "properties": {
        "latitude": { "type": "number" },
        "longitude": { "type": "number" }
      }
    },
    "instructions": { "type": "string", "description": "Delivery instructions" }
  }
}
```

### Payment Method

```json
{
  "$id": "https://agentos.dev/schemas/vault/payment/v1",
  "type": "object",
  "properties": {
    "id": { "type": "string", "format": "uuid" },
    "type": { 
      "type": "string", 
      "enum": ["card", "upi", "wallet", "bank", "other"] 
    },
    "label": { "type": "string" },
    "isDefault": { "type": "boolean" },
    "card": {
      "type": "object",
      "properties": {
        "lastFour": { "type": "string" },
        "network": { "type": "string" },
        "expiryMonth": { "type": "integer" },
        "expiryYear": { "type": "integer" }
      }
    },
    "upi": {
      "type": "object",
      "properties": {
        "handle": { "type": "string" }
      }
    }
  }
}
```

### Preferences

```json
{
  "$id": "https://agentos.dev/schemas/vault/preferences/v1",
  "type": "object",
  "properties": {
    "language": { "type": "string" },
    "region": { "type": "string" },
    "timezone": { "type": "string" },
    "defaults": {
      "type": "object",
      "description": "Default app choices by intent domain",
      "additionalProperties": { "type": "string" }
    },
    "habits": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "pattern": { "type": "string" },
          "preference": { "type": "string" },
          "confidence": { "type": "number" }
        }
      }
    }
  }
}
```

---

## Access Control

### Permission Scopes

```
vault.identity.read
vault.identity.write
vault.contacts.read
vault.contacts.write
vault.addresses.read
vault.payments.read
vault.preferences.read
vault.preferences.write
vault.history.read
```

### Permission Request

```json
{
  "package": "com.example.app",
  "requested": ["vault.contacts.read", "vault.addresses.read"],
  "reason": "To help you send messages and deliveries",
  "retention": "session"
}
```

### Permission Levels

| Level | Description |
|-------|-------------|
| `session` | Valid only during active task |
| `temporary` | Valid for limited time (e.g., 1 hour) |
| `persistent` | Valid until revoked |

---

## Vault API

### Query Interface

```kotlin
interface PersonalDataVault {
    // Contacts
    suspend fun getContact(id: String): Contact?
    suspend fun findContacts(query: ContactQuery): List<Contact>
    suspend fun resolveContact(name: String): List<Contact>
    
    // Addresses
    suspend fun getAddress(id: String): Address?
    suspend fun getAddressByLabel(label: String): Address?
    
    // Preferences
    suspend fun getPreference(key: String): Any?
    suspend fun setPreference(key: String, value: Any)
    suspend fun getDefaultApp(intentDomain: String): String?
    
    // History
    suspend fun getRecentContacts(limit: Int): List<Contact>
    suspend fun recordInteraction(contactId: String, type: String)
}
```

### Resolution Examples

**"Send to Mom"**
```kotlin
val contacts = vault.resolveContact("Mom")
// Returns contacts with nickname "Mom" or relationship "mother"
```

**"My home address"**
```kotlin
val home = vault.getAddressByLabel("Home")
```

**"The usual payment"**
```kotlin
val payment = vault.getPaymentMethods().first { it.isDefault }
```

---

## Data Synchronization

### Sync Strategy

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Device    │ ◄──► │    Cloud    │ ◄──► │   Device    │
│   Vault     │      │    Sync     │      │   Vault     │
└─────────────┘      └─────────────┘      └─────────────┘
```

### Conflict Resolution

1. **Last-write-wins** for simple fields
2. **Merge** for arrays (contacts, addresses)
3. **User prompt** for critical conflicts

---

## Open Questions

- [ ] Encryption strategy for sensitive data?
- [ ] Offline-first sync protocol?
- [ ] Data export/portability format?
- [ ] Retention and deletion policies?
- [ ] Third-party vault provider support?
