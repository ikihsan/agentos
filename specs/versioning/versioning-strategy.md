# Versioning Strategy

> **Status:** Draft  
> **Version:** 0.1.0  
> **Phase:** 0

---

## Overview

This document defines the versioning strategy for all Agent OS components:
- Platform APIs
- Specification schemas
- SDK versions
- Capability manifests
- Runtime versions

---

## Versioning Scheme

### Semantic Versioning (SemVer)

All versions follow **Semantic Versioning 2.0.0**:

```
MAJOR.MINOR.PATCH
```

| Component | Increment When |
|-----------|----------------|
| MAJOR | Breaking changes, incompatible API changes |
| MINOR | New features, backward-compatible additions |
| PATCH | Bug fixes, backward-compatible fixes |

### Pre-release Versions

```
1.0.0-alpha.1
1.0.0-beta.2
1.0.0-rc.1
```

---

## Component Version Matrix

### Platform Versions

| Component | Current | Status |
|-----------|---------|--------|
| Task Schema | 0.1.0 | Draft |
| Capability Manifest | 0.1.0 | Draft |
| Personal Data Vault | 0.1.0 | Draft |
| Dynamic UI Schema | 0.1.0 | Draft |
| Permissions Model | 0.1.0 | Draft |
| Agent OS Runtime | — | Not started |
| Agent OS SDK | — | Not started |

### Version Lifecycle

```
Draft (0.x) → Alpha → Beta → RC → Stable (1.0)
```

| Stage | Stability | Changes Allowed |
|-------|-----------|-----------------|
| Draft | Unstable | Any changes |
| Alpha | Unstable | Breaking changes with notice |
| Beta | Semi-stable | Minor breaking changes |
| RC | Stable | Bug fixes only |
| Stable | Stable | SemVer rules apply |

---

## Schema Versioning

### Schema URLs

Every schema has a versioned URL:

```
https://agentos.dev/schemas/<component>/<version>
```

Examples:
```
https://agentos.dev/schemas/task/v1
https://agentos.dev/schemas/capability-manifest/v1
https://agentos.dev/schemas/vault/contact/v1
https://agentos.dev/schemas/ui/request/v1
```

### Schema Evolution Rules

#### Backward-Compatible Changes (Minor Version)

✅ Allowed:
- Add optional fields
- Add new enum values
- Relax validation (e.g., increase max length)
- Add new component types

#### Breaking Changes (Major Version)

🔴 Requires major version:
- Remove fields
- Rename fields
- Change field types
- Add required fields
- Restrict validation

### Multi-Version Support

The runtime supports multiple schema versions simultaneously:

```json
{
  "$schema": "https://agentos.dev/schemas/task/v1",
  ...
}
```

```json
{
  "$schema": "https://agentos.dev/schemas/task/v2",
  ...
}
```

---

## API Versioning

### REST APIs

URL-based versioning:

```
https://api.agentos.dev/v1/tasks
https://api.agentos.dev/v2/tasks
```

### SDK APIs

Package versioning:

```kotlin
// v1
import dev.agentos.sdk.v1.Task

// v2
import dev.agentos.sdk.v2.Task
```

### Deprecation Policy

1. **Announce** deprecation in release notes
2. **Mark** APIs with `@Deprecated` annotation
3. **Support** deprecated version for minimum 12 months
4. **Remove** in next major version

---

## Capability Manifest Versioning

### Manifest Version

Each app declares the manifest schema version:

```json
{
  "manifestVersion": "1.0",
  "package": "com.example.app",
  ...
}
```

### Capability Versioning

Individual capabilities can have versions:

```json
{
  "id": "send_message",
  "version": "2.0",
  "deprecates": "1.0",
  ...
}
```

### Compatibility Matrix

| Manifest Version | Runtime Version | Status |
|------------------|-----------------|--------|
| 1.0 | 1.x, 2.x | Supported |
| 2.0 | 2.x | Supported |
| 0.x | Any | Development only |

---

## Runtime Versioning

### Version Components

```
AgentOS Runtime <MAJOR>.<MINOR>.<PATCH>+<build>
```

Example:
```
AgentOS Runtime 1.2.3+456
```

### Compatibility Guarantees

| Runtime | SDK | Schemas | Apps |
|---------|-----|---------|------|
| 1.x | 1.x | v1 | manifest 1.x |
| 2.x | 1.x, 2.x | v1, v2 | manifest 1.x, 2.x |

---

## Migration Strategy

### Automated Migration

Provide migration tools for schema upgrades:

```bash
agentos migrate --from v1 --to v2 capability.json
```

### Migration Guides

Every major version includes:
- Breaking changes list
- Migration steps
- Code examples
- Compatibility notes

---

## Release Process

### Release Cadence

| Release Type | Frequency | Contents |
|--------------|-----------|----------|
| Patch | As needed | Bug fixes |
| Minor | Monthly | New features |
| Major | Annually | Breaking changes |

### Release Channels

| Channel | Stability | Audience |
|---------|-----------|----------|
| `stable` | Production-ready | All users |
| `beta` | Feature-complete | Early adopters |
| `alpha` | Experimental | Developers |
| `nightly` | Bleeding edge | Contributors |

---

## Version Negotiation

### Capability Resolution

When invoking capabilities:

1. App declares supported versions
2. Runtime requests highest compatible version
3. Fallback to lower version if needed

```json
{
  "request": {
    "capability": "messaging.send",
    "preferredVersion": "2.0",
    "minVersion": "1.0"
  },
  "resolved": {
    "version": "2.0",
    "handler": "..."
  }
}
```

---

## Changelog Format

### Keep a Changelog

Follow [Keep a Changelog](https://keepachangelog.com/) format:

```markdown
# Changelog

## [1.1.0] - 2026-03-15

### Added
- New `TableEditor` dynamic UI component
- Support for compound tasks

### Changed
- Improved contact resolution algorithm

### Deprecated
- `legacy_contact_picker` component

### Fixed
- Permission scope inheritance bug
```

---

## Open Questions

- [ ] Long-term support (LTS) version policy?
- [ ] Feature flags for gradual rollout?
- [ ] A/B testing framework for schema changes?
- [ ] Telemetry for version adoption tracking?
