package dev.agentos.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Serializer for Any type to support dynamic slot values.
 * 
 * Supports: String, Number, Boolean, List, Map
 */
object AnySerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")

    override fun serialize(encoder: Encoder, value: Any?) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw IllegalStateException("AnySerializer only works with JSON")

        val jsonElement = when (value) {
            null -> JsonNull
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is List<*> -> JsonArray(value.map { serializeToJsonElement(it) })
            is Map<*, *> -> JsonObject(value.entries.associate { 
                it.key.toString() to serializeToJsonElement(it.value) 
            })
            else -> JsonPrimitive(value.toString())
        }
        jsonEncoder.encodeJsonElement(jsonElement)
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("AnySerializer only works with JSON")

        return deserializeJsonElement(jsonDecoder.decodeJsonElement())
    }

    private fun serializeToJsonElement(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is List<*> -> JsonArray(value.map { serializeToJsonElement(it) })
        is Map<*, *> -> JsonObject(value.entries.associate { 
            it.key.toString() to serializeToJsonElement(it.value) 
        })
        else -> JsonPrimitive(value.toString())
    }

    private fun deserializeJsonElement(element: JsonElement): Any? = when (element) {
        is JsonNull -> null
        is JsonPrimitive -> {
            when {
                element.isString -> element.content
                element.booleanOrNull != null -> element.boolean
                element.longOrNull != null -> element.long
                element.doubleOrNull != null -> element.double
                else -> element.content
            }
        }
        is JsonArray -> element.map { deserializeJsonElement(it) }
        is JsonObject -> element.mapValues { deserializeJsonElement(it.value) }
    }
}
