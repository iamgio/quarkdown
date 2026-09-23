package com.quarkdown.core

import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.ParameterType
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.binding.Unbound
import com.quarkdown.core.function.call.binding.coerce
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.core.function.error.MismatchingArgumentTypeException
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.factory.ValueFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Unit tests for [coerce]: the single conversion entry point used by generated native function bodies.
 */
class ArgumentCoercionTest {
    private fun param(
        name: String = "p",
        isNullable: Boolean = false,
    ) = FunctionParameter(name, ParameterType.Static("String"), index = 0, isNullable = isNullable)

    private fun call(): FunctionCall<StringValue> {
        val function = SimpleFunction(name = "test", parameters = emptyList()) { _, _ -> StringValue("") }
        return FunctionCall(function, arguments = emptyList())
    }

    @Test
    fun `already the expected type is returned untouched`() {
        val raw = DynamicValue("anything")
        val result = coerce<DynamicValue>(raw, param(), call()) { DynamicValue(it) }
        assertEquals(raw, result)
    }

    @Test
    fun `wrapped value is unwrapped before the factory runs`() {
        val result = coerce<String>(StringValue("hello"), param(), call()) { error("factory must not run") }
        assertEquals("hello", result)
    }

    @Test
    fun `dynamic value is converted through the factory`() {
        val result = coerce<Size>(DynamicValue("10px"), param(), call()) { ValueFactory.size(it) }
        assertEquals(10.0, result.value)
    }

    @Test
    fun `none becomes null for a nullable parameter`() {
        val result = coerce<Size?>(NoneValue, param(isNullable = true), call()) { ValueFactory.size(it) }
        assertNull(result)
    }

    @Test
    fun `none on a non-nullable parameter fails`() {
        assertFailsWith<InvalidFunctionCallException> {
            coerce<Size>(NoneValue, param(), call()) { ValueFactory.size(it) }
        }
    }

    @Test
    fun `an invalid raw value keeps the conversion's own explanation`() {
        val exception =
            assertFailsWith<InvalidFunctionCallException> {
                coerce<Size>(DynamicValue("3kg"), param(name = "width"), call()) { ValueFactory.size(it) }
            }
        assert(exception.message!!.contains("3kg"))
    }

    @Test
    fun `a null factory result fails`() {
        // The target type is Size rather than String, so that the factory is actually reached:
        // a DynamicValue already wrapping a String satisfies a String parameter without conversion.
        assertFailsWith<MismatchingArgumentTypeException> {
            coerce<Size>(DynamicValue("x"), param(), call()) { null }
        }
    }

    @Test
    fun `the unbound sentinel never reaches coercion`() {
        assertFailsWith<IllegalArgumentException> {
            coerce<String>(Unbound, param(), call()) { ValueFactory.string(it) }
        }
    }

    @Test
    fun `an unknown enum entry is attributed to the call`() {
        val values = NodeStyle.Alignment.entries.toTypedArray<Enum<*>>()
        val exception =
            assertFailsWith<InvalidFunctionCallException> {
                coerce<NodeStyle.Alignment>(DynamicValue("nope"), param(), call()) {
                    ValueFactory.enum(it, values)
                }
            }
        assert(exception.message!!.contains("nope"))
    }

    @Test
    fun `a number value reaching a string parameter is stringified`() {
        val result = coerce<String>(NumberValue(3), param(), call()) { ValueFactory.string(it) }
        assertEquals("3", result)
    }
}
