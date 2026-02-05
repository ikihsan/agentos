# ADR-001: Android (AOSP) as Base Platform

> **Status:** Accepted  
> **Date:** 2026-02-06

---

## Context

Agent OS needs a mobile operating system foundation. Options include:

1. Build from scratch
2. Fork iOS (not possible - closed source)
3. Fork Android (AOSP)
4. Use alternative mobile OS (e.g., postmarketOS, Sailfish)

## Decision

We will build Agent OS on top of **Android Open Source Project (AOSP)**.

## Rationale

### Why Android (AOSP)

| Factor | Android | Alternatives |
|--------|---------|--------------|
| Open Source | ✅ Fully open | ❌ iOS closed |
| App Ecosystem | ✅ Millions of apps | ⚠️ Limited |
| Hardware Support | ✅ Vast | ⚠️ Limited |
| Customization | ✅ Deep system access | ⚠️ Varies |
| Developer Community | ✅ Huge | ⚠️ Small |
| Documentation | ✅ Extensive | ⚠️ Limited |

### Specific Android Advantages

1. **System Services** — Can create custom system services
2. **Accessibility API** — Rich automation capabilities
3. **Intent System** — Native inter-app communication
4. **Background Execution** — Supports background agents
5. **Custom ROMs** — Proven path to OS-level changes

### Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Google dependencies | Use pure AOSP, avoid GMS |
| Android fragmentation | Target specific API levels |
| Performance overhead | Optimize critical paths |
| Update complexity | Modular architecture |

## Implementation Path

```
Phase 1: Android App Layer
    └─► Accessibility Service + Overlays
    └─► Proof of concept

Phase 2: System App
    └─► Elevated permissions
    └─► System service integration

Phase 3: AOSP Fork
    └─► Custom ROM
    └─► Deep system integration

Phase 4: Custom ROM Release
    └─► Device partnerships
    └─► Public release
```

## Consequences

### Positive

- Immediate access to app ecosystem
- Faster development with existing tools
- Easier developer adoption
- Hardware compatibility

### Negative

- Constrained by Android architecture
- Must maintain Android compatibility
- Potential conflicts with Google ecosystem
- Update lag behind mainline Android

## Alternatives Considered

### Build from Scratch

- **Pros:** Complete control, clean slate
- **Cons:** Years of development, no apps, no hardware support
- **Decision:** Too risky and slow

### Alternative Mobile OS

- **Pros:** Potentially cleaner architecture
- **Cons:** No app ecosystem, limited hardware, small community
- **Decision:** Insufficient ecosystem

---

## References

- [AOSP Documentation](https://source.android.com/)
- [LineageOS](https://lineageos.org/) - Successful AOSP fork example
- [GrapheneOS](https://grapheneos.org/) - Security-focused AOSP fork
