package com.quarkdown.stdlib.internal

import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.StringValue
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * [JsonValueDeserializer] tests.
 */
class JsonValueDeserializerTest {
    private fun deserialize(source: String) = JsonValueDeserializer.deserialize(Json.parseToJsonElement(source))

    @Test
    fun `string primitive`() {
        val value = deserialize(""""hello"""")
        assertEquals(StringValue("hello"), value)
    }

    @Test
    fun `empty string primitive`() {
        val value = deserialize("\"\"")
        assertEquals(StringValue(""), value)
    }

    @Test
    fun `boolean primitives`() {
        assertEquals(BooleanValue(true), deserialize("true"))
        assertEquals(BooleanValue(false), deserialize("false"))
    }

    @Test
    fun `integer primitive`() {
        val value = deserialize("42")
        assertIs<NumberValue>(value)
        assertEquals(42, value.unwrappedValue)
    }

    @Test
    fun `negative integer primitive`() {
        val value = deserialize("-7")
        assertIs<NumberValue>(value)
        assertEquals(-7, value.unwrappedValue)
    }

    @Test
    fun `double primitive`() {
        val value = deserialize("3.14")
        assertIs<NumberValue>(value)
        assertEquals(3.14, value.unwrappedValue)
    }

    @Test
    fun `integer just beyond Int MAX_VALUE stays a Long`() {
        val raw = Int.MAX_VALUE.toLong() + 1L
        val value = deserialize(raw.toString())
        assertIs<NumberValue>(value)
        assertEquals(raw, value.unwrappedValue)
        assertIs<Long>(value.unwrappedValue)
    }

    @Test
    fun `Long MAX_VALUE stays a Long without precision loss`() {
        val value = deserialize(Long.MAX_VALUE.toString())
        assertIs<NumberValue>(value)
        assertEquals(Long.MAX_VALUE, value.unwrappedValue)
        assertIs<Long>(value.unwrappedValue)
    }

    @Test
    fun `Long MIN_VALUE stays a Long without precision loss`() {
        val value = deserialize(Long.MIN_VALUE.toString())
        assertIs<NumberValue>(value)
        assertEquals(Long.MIN_VALUE, value.unwrappedValue)
        assertIs<Long>(value.unwrappedValue)
    }

    @Test
    fun `null primitive`() {
        assertEquals(NoneValue, deserialize("null"))
    }

    @Test
    fun `numeric-looking string stays a string`() {
        val value = deserialize(""""42"""")
        assertEquals(StringValue("42"), value)
    }

    @Test
    fun `empty object`() {
        val value = deserialize("{}")
        assertEquals(DictionaryValue(mutableMapOf()), value)
    }

    @Test
    fun `empty array`() {
        val value = deserialize("[]")
        assertIs<GeneralCollectionValue<*>>(value)
        assertTrue(value.unwrappedValue.toList().isEmpty())
    }

    @Test
    fun `flat object with mixed primitives`() {
        val value =
            deserialize(
                """
                {"name": "Alice", "age": 30, "active": true, "notes": null}
                """.trimIndent(),
            )

        assertEquals(
            DictionaryValue(
                mutableMapOf(
                    "name" to StringValue("Alice"),
                    "age" to NumberValue(30),
                    "active" to BooleanValue(true),
                    "notes" to NoneValue,
                ),
            ),
            value,
        )
    }

    @Test
    fun `array of primitives preserves order and types`() {
        val value = deserialize("""["text", 42, 3.14, true, null]""")
        assertIs<GeneralCollectionValue<*>>(value)

        val items = value.unwrappedValue.toList()
        assertEquals(5, items.size)
        assertEquals(StringValue("text"), items[0])
        assertEquals(NumberValue(42), items[1])
        assertIs<NumberValue>(items[2])
        assertEquals(3.14, (items[2] as NumberValue).unwrappedValue)
        assertEquals(BooleanValue(true), items[3])
        assertEquals(NoneValue, items[4])
    }

    @Test
    fun `nested object`() {
        val value =
            deserialize(
                """
                {"outer": {"inner": {"leaf": "value"}}}
                """.trimIndent(),
            )

        assertEquals(
            DictionaryValue(
                mutableMapOf(
                    "outer" to
                        DictionaryValue(
                            mutableMapOf(
                                "inner" to
                                    DictionaryValue(
                                        mutableMapOf(
                                            "leaf" to StringValue("value"),
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
            value,
        )
    }

    @Test
    fun `object containing array`() {
        val value =
            deserialize(
                """
                {"tags": ["a", "b", "c"]}
                """.trimIndent(),
            )

        assertIs<DictionaryValue<*>>(value)
        val tags = value.unwrappedValue["tags"]
        assertIs<GeneralCollectionValue<*>>(tags)
        assertEquals(
            listOf(StringValue("a"), StringValue("b"), StringValue("c")),
            tags.unwrappedValue.toList(),
        )
    }

    @Test
    fun `array of objects`() {
        val value =
            deserialize(
                """
                [{"id": 1}, {"id": 2}]
                """.trimIndent(),
            )

        assertIs<GeneralCollectionValue<*>>(value)
        val items = value.unwrappedValue.toList()
        assertEquals(
            DictionaryValue(mutableMapOf("id" to NumberValue(1))),
            items[0],
        )
        assertEquals(
            DictionaryValue(mutableMapOf("id" to NumberValue(2))),
            items[1],
        )
    }

    @Test
    fun `nested empty structures`() {
        val value = deserialize("""{"a": {}, "b": []}""")
        assertIs<DictionaryValue<*>>(value)
        assertEquals(DictionaryValue(mutableMapOf()), value.unwrappedValue["a"])
        val b = value.unwrappedValue["b"]
        assertIs<GeneralCollectionValue<*>>(b)
        assertTrue(b.unwrappedValue.toList().isEmpty())
    }

    @Test
    fun `object preserves insertion order`() {
        val value = deserialize("""{"c": 3, "a": 1, "b": 2}""")
        assertIs<DictionaryValue<*>>(value)
        assertEquals(listOf("c", "a", "b"), value.unwrappedValue.keys.toList())
    }
}
