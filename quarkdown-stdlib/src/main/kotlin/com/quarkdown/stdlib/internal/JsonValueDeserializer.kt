package com.quarkdown.stdlib.internal

import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.wrappedAsValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

/**
 * Deserializes a [JsonElement] into an [OutputValue].
 */
internal object JsonValueDeserializer {
    fun deserialize(json: JsonElement): OutputValue<*>? =
        when (json) {
            is JsonPrimitive -> deserialize(json)
            is JsonObject -> deserialize(json)
            is JsonArray -> deserialize(json)
        }

    private fun deserialize(json: JsonPrimitive): OutputValue<*>? =
        when {
            json is JsonNull -> NoneValue
            json.isString -> json.content.wrappedAsValue()
            json.booleanOrNull != null -> json.boolean.wrappedAsValue()
            json.intOrNull != null -> json.int.wrappedAsValue()
            // longOrNull must precede doubleOrNull: integers beyond Int.MAX_VALUE would otherwise
            // be narrowed to Double and lose precision.
            json.longOrNull != null -> json.long.wrappedAsValue()
            json.doubleOrNull != null -> json.double.wrappedAsValue()
            json.floatOrNull != null -> json.float.wrappedAsValue()
            else -> null
        }

    private fun deserialize(json: JsonObject): OutputValue<*> {
        val dictionary =
            json
                .mapNotNull { (key, value) ->
                    deserialize(value)?.let { key to it }
                }.toMap()
                .toMutableMap()

        return DictionaryValue(dictionary)
    }

    private fun deserialize(json: JsonArray): IterableValue<*> {
        val values = json.mapNotNull(::deserialize)
        return GeneralCollectionValue(values)
    }
}
