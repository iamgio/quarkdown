package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.function.FunctionCall
import eu.iamgio.quarkdown.function.FunctionCallArgument
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.reflect.KFunctionAdapter
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Function call tests.
 */
class FunctionTest {
    @Test
    fun `no arguments`() {
        val function =
            SimpleFunction(
                name = "greet",
                parameters = emptyList(),
            ) {
                ValueFactory.string("Hello")
            }

        val call = FunctionCall(function, arguments = emptyList())

        assertEquals("Hello", call.execute().unwrappedValue)
    }

    @Test
    fun `with arguments`() {
        val function =
            SimpleFunction(
                name = "greet",
                parameters =
                    listOf(
                        FunctionParameter("to", StringValue::class),
                        FunctionParameter("from", StringValue::class),
                    ),
            ) {
                val to = arg<String>("to")
                val from = arg<String>("from")
                ValueFactory.string("Hello $to from $from")
            }

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                        FunctionCallArgument(StringValue("B")),
                    ),
            )

        assertEquals("Hello A from B", call.execute().unwrappedValue)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun greetNoArgs(): StringValue = StringValue("Hello")

    @Test
    fun `KFunction without arguments`() {
        val function = KFunctionAdapter(::greetNoArgs)
        val call = FunctionCall(function, arguments = emptyList())

        assertEquals("Hello", call.execute().unwrappedValue)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun greetWithArgs(
        to: String,
        from: String,
    ): StringValue = StringValue("Hello $to from $from")

    @Test
    fun `KFunction with arguments`() {
        val function = KFunctionAdapter(::greetWithArgs)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                        FunctionCallArgument(StringValue("B")),
                    ),
            )

        assertEquals("Hello A from B", call.execute().unwrappedValue)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun sum(
        a: Int,
        b: Int,
    ): NumberValue = NumberValue(a + b)

    @Test
    fun `KFunction with auto arguments`() {
        val function = KFunctionAdapter(::sum)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("2")),
                        FunctionCallArgument(DynamicInputValue("5")),
                    ),
            )

        assertEquals(7, call.execute().unwrappedValue)
    }
}
