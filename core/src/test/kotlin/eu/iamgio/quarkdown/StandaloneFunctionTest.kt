package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.MismatchingArgumentTypeException
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.library.loader.MultiFunctionLibraryLoader
import eu.iamgio.quarkdown.function.reflect.KFunctionAdapter
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * Function call tests.
 * For tests of function calls from Quarkdown sources see [FunctionNodeExpansionTest].
 */
class StandaloneFunctionTest {
    @BeforeTest
    fun setup() {
        // Throw exception on function call error.
        SystemProperties[SystemProperties.EXIT_ON_ERROR] = ""
    }

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
                        FunctionParameter("to", StringValue::class, index = 0),
                        FunctionParameter("from", StringValue::class, index = 1),
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

    @Test
    fun `with nested call arguments`() {
        val functionPerson =
            SimpleFunction(
                name = "person",
                parameters = emptyList(),
            ) {
                ValueFactory.string("A")
            }

        val functionGreet =
            SimpleFunction(
                name = "greet",
                parameters =
                    listOf(
                        FunctionParameter("to", StringValue::class, index = 0),
                        FunctionParameter("from", StringValue::class, index = 1),
                    ),
            ) {
                val to = arg<String>("to")
                val from = arg<String>("from")
                ValueFactory.string("Hello $to from $from")
            }

        val callPerson =
            FunctionCall(
                functionPerson,
                arguments = emptyList(),
            )

        val callGreet =
            FunctionCall(
                functionGreet,
                arguments =
                    listOf(
                        FunctionCallArgument(callPerson),
                        FunctionCallArgument(StringValue("B")),
                    ),
            )

        assertEquals("Hello A from B", callGreet.execute().unwrappedValue)
    }

    @Test
    fun `with composed arguments`() {
        val functionPerson =
            SimpleFunction(
                name = "person",
                parameters = emptyList(),
            ) {
                ValueFactory.string("A")
            }

        val functionGreet =
            SimpleFunction(
                name = "greet",
                parameters =
                    listOf(
                        FunctionParameter("to", StringValue::class, index = 0),
                        FunctionParameter("from", StringValue::class, index = 1),
                    ),
            ) {
                val to = arg<String>("to")
                val from = arg<String>("from")
                ValueFactory.string("Hello $to from $from")
            }

        val callPerson =
            FunctionCall(
                functionPerson,
                arguments = emptyList(),
            )

        val callGreet =
            FunctionCall(
                functionGreet,
                arguments =
                    listOf(
                        FunctionCallArgument(ComposedExpression(listOf(callPerson, StringValue("B")))),
                        FunctionCallArgument(StringValue("B")),
                    ),
            )

        assertEquals("Hello AB from B", callGreet.execute().unwrappedValue)
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
    fun greetWithOptionalArgs(
        to: String = "you",
        from: String = "me",
    ): StringValue = StringValue("Hello $to from $from")

    @Test
    fun `KFunction with optional arguments`() {
        val function = KFunctionAdapter(::greetWithOptionalArgs)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                    ),
            )

        assertEquals("Hello A from me", call.execute().unwrappedValue)
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

    @Test
    fun `KFunction wrong argument count`() {
        val function = KFunctionAdapter(::sum)

        val call1 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("5")),
                    ),
            )

        assertFailsWith<InvalidArgumentCountException> {
            call1.execute()
        }

        val call2 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("5")),
                        FunctionCallArgument(DynamicInputValue("1")),
                        FunctionCallArgument(DynamicInputValue("2")),
                    ),
            )

        assertFailsWith<InvalidArgumentCountException> {
            call2.execute()
        }

        val call3 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("5")),
                        FunctionCallArgument(DynamicInputValue("1")),
                    ),
            )

        assertEquals(6, call3.execute().unwrappedValue)
    }

    @Test
    fun `KFunction wrong argument types`() {
        val function = KFunctionAdapter(::sum)

        val call1 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("a")),
                        FunctionCallArgument(DynamicInputValue("b")),
                    ),
            )

        assertFailsWith<MismatchingArgumentTypeException> {
            call1.execute()
        }

        val call2 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("5")),
                        FunctionCallArgument(DynamicInputValue("abc")),
                    ),
            )

        assertFailsWith<MismatchingArgumentTypeException> {
            call2.execute()
        }

        val call3 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("abcde")),
                        FunctionCallArgument(DynamicInputValue("5")),
                    ),
            )

        assertFailsWith<MismatchingArgumentTypeException> {
            call3.execute()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun identity(x: Int) = NumberValue(x)

    @Test
    fun `KFunction with nested call arguments`() {
        val functionSum = KFunctionAdapter(::sum)
        val functionIdentity = KFunctionAdapter(::identity)

        val callIdentity =
            FunctionCall(
                functionIdentity,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("2")),
                    ),
            )

        val callSum =
            FunctionCall(
                functionSum,
                arguments =
                    listOf(
                        FunctionCallArgument(NumberValue(3)),
                        FunctionCallArgument(callIdentity),
                    ),
            )

        assertEquals(5, callSum.execute().unwrappedValue)
    }

    @Test
    fun `KFunction with composed arguments`() {
        val functionGreetWithArgs = KFunctionAdapter(::greetWithArgs)
        val functionGreetWithoutArgs = KFunctionAdapter(::greetNoArgs)

        val callWithoutArgs =
            FunctionCall(
                functionGreetWithoutArgs,
                arguments = emptyList(),
            )

        val callWithArgs =
            FunctionCall(
                functionGreetWithArgs,
                arguments =
                    listOf(
                        FunctionCallArgument(ComposedExpression(listOf(callWithoutArgs, StringValue(" dear")))),
                        FunctionCallArgument(StringValue("B")),
                    ),
            )

        assertEquals("Hello Hello dear from B", callWithArgs.execute().unwrappedValue)
    }

    @Test
    fun `KFunction with dynamic composed arguments`() {
        val functionGreetWithArgs = KFunctionAdapter(::greetWithArgs)
        val functionGreetWithoutArgs = KFunctionAdapter(::greetNoArgs)

        val callWithoutArgs =
            FunctionCall(
                functionGreetWithoutArgs,
                arguments = emptyList(),
            )

        val callWithArgs =
            FunctionCall(
                functionGreetWithArgs,
                arguments =
                    listOf(
                        FunctionCallArgument(ComposedExpression(listOf(callWithoutArgs, DynamicInputValue(" dear")))),
                        FunctionCallArgument(DynamicInputValue("B")),
                    ),
            )

        assertEquals("Hello Hello dear from B", callWithArgs.execute().unwrappedValue)
    }

    @Test
    fun `library loader`() {
        val library = MultiFunctionLibraryLoader("MyLib").load(setOf(::greetWithArgs, ::greetNoArgs, ::sum))

        assertEquals("MyLib", library.name)
        assertEquals(3, library.functions.size)

        val function = library.functions.first { it.name == "sum" }

        assertIs<Function<NumberValue>>(function)
        assertEquals(2, function.parameters.size)

        val staticCall =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(NumberValue(2)),
                        FunctionCallArgument(NumberValue(5)),
                    ),
            )

        assertEquals(7, staticCall.execute().unwrappedValue)

        val dynamicCall =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicInputValue("2")),
                        FunctionCallArgument(DynamicInputValue("5")),
                    ),
            )

        assertEquals(7, dynamicCall.execute().unwrappedValue)
    }
}
