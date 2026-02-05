package dev.agentos.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an application capability that can be invoked by the Agent OS runtime.
 * 
 * Capabilities are the bridge between user intent and application functionality.
 * Each capability declares its interface contract: what inputs it needs,
 * what outputs it produces, and how it should be invoked.
 */
@Serializable
data class Capability(
    val id: String,
    val intents: List<String>,
    val description: String,
    val keywords: List<String> = emptyList(),
    val inputs: List<CapabilityInput> = emptyList(),
    val outputs: List<CapabilityOutput> = emptyList(),
    val handler: CapabilityHandler,
    val permissions: List<String> = emptyList(),
    val uiRequired: Boolean = false
)

/**
 * Input parameter definition for a capability.
 */
@Serializable
data class CapabilityInput(
    val name: String,
    val type: SlotType,
    val required: Boolean = true,
    val description: String? = null,
    val vaultKey: String? = null,
    val defaultValue: @Serializable(with = AnySerializer::class) Any? = null
)

/**
 * Output definition for a capability.
 */
@Serializable
data class CapabilityOutput(
    val name: String,
    val type: SlotType,
    val description: String? = null
)

/**
 * Handler definition for invoking a capability.
 */
@Serializable
data class CapabilityHandler(
    val type: HandlerType,
    val target: String,
    val extras: Map<String, String> = emptyMap()
)

/**
 * Types of capability handlers.
 */
@Serializable
enum class HandlerType {
    @SerialName("intent")
    INTENT,

    @SerialName("service")
    SERVICE,

    @SerialName("broadcast")
    BROADCAST,

    @SerialName("deeplink")
    DEEPLINK,

    @SerialName("internal")
    INTERNAL
}

/**
 * Capability manifest for an application.
 */
@Serializable
data class CapabilityManifest(
    val manifestVersion: String,
    @SerialName("package")
    val packageName: String,
    val displayName: String? = null,
    val capabilities: List<Capability>
)
