package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes
import eu.iamgio.quarkdown.document.size.cm
import eu.iamgio.quarkdown.document.size.mm
import eu.iamgio.quarkdown.document.size.px
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.error.InvalidLambdaArgumentCountException
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.LambdaValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.factory.IllegalRawValueException
import eu.iamgio.quarkdown.function.value.factory.ValueFactory
import eu.iamgio.quarkdown.misc.color.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tests of retrieval of value wrappers from raw strings.
 */
class ValueFactoryTest {
    @Test
    fun string() {
        assertEquals(StringValue("Hello, world!"), ValueFactory.string("Hello, world!"))
    }

    @Test
    fun number() {
        assertEquals(NumberValue(42), ValueFactory.number("42"))
        assertEquals(16.3F, ValueFactory.number("16.3").unwrappedValue)
        assertFails { ValueFactory.number("num") }
        assertFails { ValueFactory.number("16.3.2") }
    }

    @Test
    fun boolean() {
        assertEquals(BooleanValue(false), ValueFactory.boolean("false"))
        assertEquals(BooleanValue(false), ValueFactory.boolean("no"))
        assertEquals(BooleanValue(true), ValueFactory.boolean("true"))
        assertEquals(BooleanValue(true), ValueFactory.boolean("yes"))
        assertFails { ValueFactory.boolean("y") }
    }

    @Test
    fun range() {
        assertEquals(Range(1, 9), ValueFactory.range("1..9").unwrappedValue)
        assertEquals(Range(null, 11), ValueFactory.range("..11").unwrappedValue)
        assertEquals(Range(14, null), ValueFactory.range("14..").unwrappedValue)
        assertEquals(Range(null, null), ValueFactory.range("..").unwrappedValue)
    }

    @Test
    fun size() {
        assertEquals(Size(10.0, Size.Unit.PX), ValueFactory.size("10px").unwrappedValue)
        assertEquals(Size(8.0, Size.Unit.PT), ValueFactory.size("8pt").unwrappedValue)
        assertEquals(Size(16.2, Size.Unit.CM), ValueFactory.size("16.2cm").unwrappedValue)
        assertEquals(Size(1.4, Size.Unit.MM), ValueFactory.size("1.4mm").unwrappedValue)
        assertEquals(Size(8.2, Size.Unit.IN), ValueFactory.size("8.2in").unwrappedValue)
        assertEquals(Size(32.95, Size.Unit.PX), ValueFactory.size("32.95").unwrappedValue)
        assertEquals(32.95.px, ValueFactory.size("32.95").unwrappedValue)
        assertFails { ValueFactory.size("px") }
        assertFails { ValueFactory.size("abc") }
        assertFails { ValueFactory.size("10.10.2cm") }
        assertFails { ValueFactory.size("") }
    }

    @Test
    fun sizes() {
        assertEquals(
            Sizes(
                Size(10.0, Size.Unit.PX),
                Size(10.0, Size.Unit.PX),
                Size(10.0, Size.Unit.PX),
                Size(10.0, Size.Unit.PX),
            ),
            ValueFactory.sizes("10px").unwrappedValue,
        )
        assertEquals(
            Sizes(all = Size(10.0, Size.Unit.PX)),
            ValueFactory.sizes("10px").unwrappedValue,
        )
        assertEquals(
            Sizes(all = Size(13.2, Size.Unit.CM)),
            ValueFactory.sizes("13.2cm").unwrappedValue,
        )
        assertEquals(
            Sizes(
                9.2.cm,
                3.8.mm,
                9.2.cm,
                3.8.mm,
            ),
            ValueFactory.sizes("9.2cm 3.8mm").unwrappedValue,
        )
        assertEquals(
            Sizes(
                vertical = Size(9.2, Size.Unit.CM),
                horizontal = Size(3.8, Size.Unit.MM),
            ),
            ValueFactory.sizes("9.2cm 3.8mm").unwrappedValue,
        )

        assertFails { ValueFactory.sizes("10px 12px 8px") }
    }

    @Test
    fun color() {
        assertEquals(Color(255, 0, 0), ValueFactory.color("#FF0000").unwrappedValue)
        assertEquals(Color(255, 0, 0), ValueFactory.color("red").unwrappedValue)
        assertEquals(Color(0, 0, 0), ValueFactory.color("#000000").unwrappedValue)
        assertEquals(Color(0, 0, 0), ValueFactory.color("BLACK").unwrappedValue)
        assertEquals(Color(145, 168, 50), ValueFactory.color("#91a832").unwrappedValue)
        assertEquals(Color(145, 168, 50), ValueFactory.color("rgb(145, 168, 50)").unwrappedValue)
        assertEquals(Color(120, 111, 93), ValueFactory.color("rgb(120,111,93)").unwrappedValue)
        assertEquals(Color(120, 111, 93, 0.5), ValueFactory.color("rgba(120, 111, 93, 0.5)").unwrappedValue)
        assertEquals(Color(50, 113, 168), ValueFactory.color("hsv(208, 70, 66)").unwrappedValue)
        assertEquals(Color(50, 113, 168), ValueFactory.color("hsv(568, 70, 66)").unwrappedValue)
        assertEquals(Color(50, 113, 168), ValueFactory.color("hsl(208, 54, 43)").unwrappedValue)
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("abc") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("#hello") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("rgb(300, 200, 200)") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("rgb(300, 200, 200, 0.8)") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("rgba(100, 200, 200, 1.5)") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("hsl(120, 105, 20)") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("hsv(120, 10,200)") }
        assertFailsWith<IllegalRawValueException> { ValueFactory.color("hsv(20, 10, 50, 10)") }
    }

    @Test
    fun enum() {
        @Suppress("UNCHECKED_CAST")
        val values = Size.Unit.entries.toTypedArray() as Array<Enum<*>>

        assertEquals(Size.Unit.PX, ValueFactory.enum("px", values)!!.unwrappedValue)
        assertEquals(Size.Unit.CM, ValueFactory.enum("CM", values)!!.unwrappedValue)
        assertEquals(Size.Unit.MM, ValueFactory.enum("mM", values)!!.unwrappedValue)
        assertNull(ValueFactory.enum("abc", values))
    }

    @Test
    fun lambda() {
        val context = MutableContext(QuarkdownFlavor)
        context.attachMockPipeline()

        // No arguments.
        with(ValueFactory.lambda("hello", context)) {
            assertIs<LambdaValue>(this)
            assertEquals("hello", unwrappedValue.invoke<String, StringValue>().unwrappedValue)
        }

        // Two implicit arguments.
        assertEquals(
            "hello world from iamgio",
            ValueFactory
                .lambda("hello .1 from .2", context)
                .unwrappedValue
                .invoke<String, StringValue>(
                    StringValue("world"),
                    StringValue("iamgio"),
                ).unwrappedValue,
        )

        // Two explicit arguments.
        assertEquals(
            "hello world from iamgio",
            ValueFactory
                .lambda(
                    "to from: hello .to from .from",
                    context,
                ).unwrappedValue
                .invoke<String, StringValue>(
                    StringValue("world"),
                    StringValue("iamgio"),
                ).unwrappedValue,
        )

        // An optional parameter, passed.
        assertEquals(
            "hello world from iamgio",
            ValueFactory
                .lambda(
                    "to?: hello .to from iamgio",
                    context,
                ).unwrappedValue
                .invoke<String, StringValue>(
                    StringValue("world"),
                ).unwrappedValue,
        )

        // An optional parameter, not passed.
        assertEquals(
            "hello None from iamgio",
            ValueFactory
                .lambda(
                    "to?: hello .to from iamgio",
                    context,
                ).unwrappedValue
                .invoke<String, StringValue>()
                .unwrappedValue,
        )

        // Two optional parameter, one passed.
        assertEquals(
            "hello world from None",
            ValueFactory
                .lambda(
                    "to from?: hello .to from .from",
                    context,
                ).unwrappedValue
                .invoke<String, StringValue>(
                    StringValue("world"),
                ).unwrappedValue,
        )

        // Mixing explicit and implicit arguments is not allowed.
        assertFailsWith<InvalidLambdaArgumentCountException> {
            ValueFactory
                .lambda("to: hello .to from .2", context)
                .unwrappedValue
                .invoke<String, StringValue>(
                    StringValue("world"),
                    StringValue("iamgio"),
                ).unwrappedValue
        }
    }

    @Test
    fun dictionary() {
        val context = MutableContext(QuarkdownFlavor)
        context.attachMockPipeline()

        assertEquals(
            DictionaryValue(
                mutableMapOf(
                    "abc" to DynamicValue("1"),
                    "def" to DynamicValue("2"),
                    "ghi" to DynamicValue("3"),
                ),
            ),
            ValueFactory.dictionary(
                """
                - abc: 1
                - def: 2
                - ghi: 3
                """.trimIndent(),
                context,
            ),
        )

        assertEquals(
            DictionaryValue(
                mutableMapOf(
                    "abc" to
                        DictionaryValue(
                            mutableMapOf(
                                "def" to DynamicValue("1"),
                                "ghi" to DynamicValue("2"),
                            ),
                        ),
                ),
            ),
            ValueFactory.dictionary(
                """
                - abc
                  - def: 1
                  - ghi: 2
                """.trimIndent(),
                context,
            ),
        )

        assertEquals(
            DictionaryValue(
                mutableMapOf(
                    "a" to DynamicValue("1"),
                    "b" to
                        DictionaryValue(
                            mutableMapOf(
                                "c" to DynamicValue("2"),
                                "d" to DynamicValue("3"),
                            ),
                        ),
                    "e" to DynamicValue("4"),
                    "f" to
                        DictionaryValue(
                            mutableMapOf(
                                "g" to
                                    DictionaryValue(
                                        mutableMapOf(
                                            "h" to DynamicValue("5"),
                                            "i" to
                                                DictionaryValue(
                                                    mutableMapOf(
                                                        "j" to DynamicValue("6"),
                                                    ),
                                                ),
                                        ),
                                    ),
                                "k" to DynamicValue("7"),
                            ),
                        ),
                    "l" to DynamicValue("8"),
                ),
            ),
            ValueFactory.dictionary(
                """
                - a: 1
                - b
                  - c: 2
                  - d: 3
                - e: 4
                - f
                    - g:
                      - h: 5
                      - i
                        - j: 6
                    - k: 7
                - l: 8
                """.trimIndent(),
                context,
            ),
        )
    }

    // TODO others that require context
}
