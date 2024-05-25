package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.SizeUnit
import eu.iamgio.quarkdown.document.page.Sizes
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.error.InvalidLambdaArgumentCountException
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.LambdaValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.data.Range
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
        assertEquals(16.3F, ValueFactory.number("16.3")?.unwrappedValue)
        assertNull(ValueFactory.number("num"))
        assertNull(ValueFactory.number("16.3.2"))
    }

    @Test
    fun boolean() {
        assertEquals(BooleanValue(false), ValueFactory.boolean("false"))
        assertEquals(BooleanValue(false), ValueFactory.boolean("no"))
        assertEquals(BooleanValue(true), ValueFactory.boolean("true"))
        assertEquals(BooleanValue(true), ValueFactory.boolean("yes"))
        assertNull(ValueFactory.boolean("y"))
    }

    @Test
    fun range() {
        // TODO x..y should actually map to Range(x, y) instead of Range(x - 1, y - 1)
        assertEquals(Range(0, 8), ValueFactory.range("1..9").unwrappedValue)
        assertEquals(Range(null, 10), ValueFactory.range("..11").unwrappedValue)
        assertEquals(Range(13, null), ValueFactory.range("14..").unwrappedValue)
        assertEquals(Range(null, null), ValueFactory.range("..").unwrappedValue)
    }

    @Test
    fun size() {
        assertEquals(Size(10.0, SizeUnit.PX), ValueFactory.size("10px").unwrappedValue)
        assertEquals(Size(8.0, SizeUnit.PT), ValueFactory.size("8pt").unwrappedValue)
        assertEquals(Size(16.2, SizeUnit.CM), ValueFactory.size("16.2cm").unwrappedValue)
        assertEquals(Size(1.4, SizeUnit.MM), ValueFactory.size("1.4mm").unwrappedValue)
        assertEquals(Size(8.2, SizeUnit.IN), ValueFactory.size("8.2in").unwrappedValue)
        assertEquals(Size(32.95, SizeUnit.PX), ValueFactory.size("32.95").unwrappedValue)
        assertFails { ValueFactory.size("px") }
        assertFails { ValueFactory.size("abc") }
        assertFails { ValueFactory.size("10.10.2cm") }
        assertFails { ValueFactory.size("") }
    }

    @Test
    fun sizes() {
        assertEquals(
            Sizes(
                Size(10.0, SizeUnit.PX),
                Size(10.0, SizeUnit.PX),
                Size(10.0, SizeUnit.PX),
                Size(10.0, SizeUnit.PX),
            ),
            ValueFactory.sizes("10px").unwrappedValue,
        )
        assertEquals(
            Sizes(all = Size(10.0, SizeUnit.PX)),
            ValueFactory.sizes("10px").unwrappedValue,
        )
        assertEquals(
            Sizes(all = Size(13.2, SizeUnit.CM)),
            ValueFactory.sizes("13.2cm").unwrappedValue,
        )
        assertEquals(
            Sizes(
                Size(9.2, SizeUnit.CM),
                Size(3.8, SizeUnit.MM),
                Size(9.2, SizeUnit.CM),
                Size(3.8, SizeUnit.MM),
            ),
            ValueFactory.sizes("9.2cm 3.8mm").unwrappedValue,
        )
        assertEquals(
            Sizes(
                vertical = Size(9.2, SizeUnit.CM),
                horizontal = Size(3.8, SizeUnit.MM),
            ),
            ValueFactory.sizes("9.2cm 3.8mm").unwrappedValue,
        )

        assertFails { ValueFactory.sizes("10px 12px 8px") }
    }

    @Test
    fun enum() {
        @Suppress("UNCHECKED_CAST")
        val values = SizeUnit.values() as Array<Enum<*>>

        assertEquals(SizeUnit.PX, ValueFactory.enum("px", values)!!.unwrappedValue)
        assertEquals(SizeUnit.CM, ValueFactory.enum("CM", values)!!.unwrappedValue)
        assertEquals(SizeUnit.MM, ValueFactory.enum("mM", values)!!.unwrappedValue)
        assertNull(ValueFactory.enum("abc", values))
    }

    @Test
    fun lambda() {
        val context = MutableContext(QuarkdownFlavor)
        context.attachMockPipeline()

        // No arguments.
        with(ValueFactory.lambda("hello", context)) {
            assertIs<LambdaValue>(this)
            assertEquals("hello", unwrappedValue.invoke<String, StringValue>(context).unwrappedValue)
        }

        // Two implicit arguments.
        assertEquals(
            "hello world from iamgio",
            ValueFactory.lambda("hello .1 from .2", context).unwrappedValue.invoke<String, StringValue>(
                context,
                StringValue("world"),
                StringValue("iamgio"),
            ).unwrappedValue,
        )

        // Two explicit arguments.
        assertEquals(
            "hello world from iamgio",
            ValueFactory.lambda(
                "to from: hello .to from .from",
                context,
            ).unwrappedValue.invoke<String, StringValue>(
                context,
                StringValue("world"),
                StringValue("iamgio"),
            ).unwrappedValue,
        )

        // Mixing explicit and implicit arguments are not allowed.
        assertFailsWith<InvalidLambdaArgumentCountException> {
            ValueFactory.lambda("to: hello .to from .2", context).unwrappedValue.invoke<String, StringValue>(
                context,
                StringValue("world"),
                StringValue("iamgio"),
            ).unwrappedValue
        }
    }

    // TODO others that require context
}
