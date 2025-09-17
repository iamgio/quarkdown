package com.quarkdown.core

import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.call.binding.ArgumentBindings
import com.quarkdown.core.function.call.validate.FunctionCallValidator
import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.core.function.error.NoSuchElementException
import com.quarkdown.core.function.error.ParameterAlreadyBoundException
import com.quarkdown.core.function.error.UnnamedArgumentAfterNamedException
import com.quarkdown.core.function.error.UnresolvedParameterException
import com.quarkdown.core.function.expression.ComposedExpression
import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.reflect.KFunctionAdapter
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.factory.ValueFactory
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Function call tests.
 * For tests of function calls from Quarkdown sources see [FunctionNodeExpansionTest].
 */
class StandaloneFunctionTest {
    /**
     * @param name name of the parameter to get the corresponding argument value for
     * @param T type of the value
     * @return the value of the argument by the given name
     * @throws NoSuchElementException if [name] does not match any parameter name
     */
    private inline fun <reified T> ArgumentBindings.arg(name: String): T =
        this.entries
            .first { it.key.name == name }
            .value // Map.Entry method: returns FunctionCallArgument
            .value // FunctionCallArgument method: returns InputValue<T>
            .unwrappedValue as T // InputValue<T> method: returns T

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
            ) { bindings ->
                val to = bindings.arg<String>("to")
                val from = bindings.arg<String>("from")
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
            ) { bindings ->
                val to = bindings.arg<String>("to")
                val from = bindings.arg<String>("from")
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
            ) { bindings ->
                val to = bindings.arg<String>("to")
                val from = bindings.arg<String>("from")
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

    @Test
    fun `with validator`() {
        var canCall = true

        val validator =
            object : FunctionCallValidator<StringValue> {
                override fun validate(call: FunctionCall<StringValue>) {
                    if (!canCall) throw IllegalStateException()
                }
            }

        val function =
            SimpleFunction(
                name = "greet",
                parameters = emptyList(),
                validators = listOf(validator),
            ) {
                canCall = false
                ValueFactory.string("Hello")
            }

        val call = FunctionCall(function, arguments = emptyList())

        assertEquals("Hello", call.execute().unwrappedValue)
        assertFailsWith<IllegalStateException> { call.execute() }
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

    @Test
    fun `KFunction with named arguments`() {
        val function = KFunctionAdapter(::greetWithArgs)

        val call1 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A"), name = "to"),
                        FunctionCallArgument(StringValue("B"), name = "from"),
                    ),
            )

        assertEquals("Hello A from B", call1.execute().unwrappedValue)

        val call2 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A"), name = "from"),
                        FunctionCallArgument(StringValue("B"), name = "to"),
                    ),
            )

        assertEquals("Hello B from A", call2.execute().unwrappedValue)

        val call3 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                        FunctionCallArgument(StringValue("B"), name = "from"),
                    ),
            )

        assertEquals("Hello A from B", call3.execute().unwrappedValue)

        val call4 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A"), name = "to"),
                        FunctionCallArgument(StringValue("B")),
                    ),
            )

        // Unnamed arguments cannot appear after a named argument.
        assertFailsWith<UnnamedArgumentAfterNamedException> {
            call4.execute()
        }

        val call5 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                        FunctionCallArgument(StringValue("B"), name = "other"),
                    ),
            )

        // Named reference to an unknown parameter.
        assertFailsWith<UnresolvedParameterException> {
            call5.execute()
        }
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

        val callNamed =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A"), name = "from"),
                    ),
            )

        assertEquals("Hello you from A", callNamed.execute().unwrappedValue)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun greetWithOptionalArgsInTheMiddle(
        to: String = "you",
        from: String = "me",
        content: String,
    ): StringValue = StringValue("Hello $to from $from: $content")

    @Test
    fun `KFunction with optional arguments in the middle`() {
        val function = KFunctionAdapter(::greetWithOptionalArgsInTheMiddle)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                        FunctionCallArgument(StringValue("hi!"), isBody = true),
                    ),
            )

        assertEquals("Hello A from me: hi!", call.execute().unwrappedValue)

        val invalidCall =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(StringValue("A")),
                        // Not marking the argument as body will associate it to the second parameter instead.
                        FunctionCallArgument(StringValue("hi!")),
                    ),
            )

        assertFailsWith<InvalidArgumentCountException> {
            invalidCall.execute()
        }
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
                        FunctionCallArgument(DynamicValue("2")),
                        FunctionCallArgument(DynamicValue("5")),
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
                        FunctionCallArgument(DynamicValue("5")),
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
                        FunctionCallArgument(DynamicValue("5")),
                        FunctionCallArgument(DynamicValue("1")),
                        FunctionCallArgument(DynamicValue("2")),
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
                        FunctionCallArgument(DynamicValue("5")),
                        FunctionCallArgument(DynamicValue("1")),
                    ),
            )

        assertEquals(6, call3.execute().unwrappedValue)
    }

    @Test
    fun `KFunction parameter bound twice`() {
        val function = KFunctionAdapter(::sum)

        val call1 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("5")),
                        FunctionCallArgument(DynamicValue("2"), name = "a"),
                    ),
            )

        assertFailsWith<ParameterAlreadyBoundException> {
            call1.execute()
        }
    }

    @Test
    fun `KFunction wrong argument types`() {
        val function = KFunctionAdapter(::sum)

        val call1 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("a")),
                        FunctionCallArgument(DynamicValue("b")),
                    ),
            )

        // Mismatching types
        assertFailsWith<InvalidFunctionCallException> {
            call1.execute()
        }

        val call2 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("5")),
                        FunctionCallArgument(DynamicValue("abc")),
                    ),
            )

        assertFailsWith<InvalidFunctionCallException> {
            call2.execute()
        }

        val call3 =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("abcde")),
                        FunctionCallArgument(DynamicValue("5")),
                    ),
            )

        assertFailsWith<InvalidFunctionCallException> {
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
                        FunctionCallArgument(DynamicValue("2")),
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
                        FunctionCallArgument(ComposedExpression(listOf(callWithoutArgs, DynamicValue(" dear")))),
                        FunctionCallArgument(DynamicValue("B")),
                    ),
            )

        assertEquals("Hello Hello dear from B", callWithArgs.execute().unwrappedValue)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun echoEnum(value: Container.Alignment) = StringValue(value.name)

    @Test
    fun `KFunction with enum`() {
        val function = KFunctionAdapter(::echoEnum)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("center")),
                    ),
            )

        assertEquals("CENTER", call.execute().unwrappedValue)
    }

    @Test
    fun `KFunction with invalid enum`() {
        val function = KFunctionAdapter(::echoEnum)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("something")),
                    ),
            )

        assertFailsWith<InvalidFunctionCallException> {
            call.execute()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setDocumentName(
        @Injected context: MutableContext,
        name: String,
    ): VoidValue {
        context.documentInfo = context.documentInfo.copy(name = name)
        return VoidValue
    }

    @Test
    fun `KFunction with injected context`() {
        val function = KFunctionAdapter(::setDocumentName)

        val context = MutableContext(QuarkdownFlavor)

        val call =
            FunctionCall(
                function,
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("New name")),
                    ),
                context,
            )

        assertNull(context.documentInfo.name)

        call.execute()

        assertEquals("New name", context.documentInfo.name)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun invalidInjection(
        @Injected x: String,
    ) = StringValue(x)

    @Test
    fun `KFunction with invalid injection`() {
        val function = KFunctionAdapter(::invalidInjection)

        val context = MutableContext(QuarkdownFlavor)

        val call =
            FunctionCall(
                function,
                arguments = emptyList(),
                context,
            )

        assertNull(context.documentInfo.name)

        // String isn't an injectable type.
        assertFailsWith<IllegalArgumentException> {
            call.execute()
        }
    }

    private fun <T : OutputValue<*>> createCallForDocumentType(
        function: KFunction<T>,
        documentType: DocumentType,
    ): FunctionCall<T> {
        val context = MutableContext(QuarkdownFlavor)
        context.documentInfo = context.documentInfo.copy(type = documentType)
        val adapter = KFunctionAdapter(function)
        return FunctionCall(adapter, emptyList(), context)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @OnlyForDocumentType(DocumentType.SLIDES)
    fun slidesOnlyGreet(): StringValue = StringValue("Hello")

    @Test
    fun `KFunction with whitelisted document type`() {
        val call = createCallForDocumentType(::slidesOnlyGreet, DocumentType.SLIDES)
        assertEquals("Hello", call.execute().unwrappedValue)
    }

    @Test
    fun `KFunction with non-whitelisted document type`() {
        val call = createCallForDocumentType(::slidesOnlyGreet, DocumentType.PLAIN)
        assertFailsWith<InvalidFunctionCallException> { call.execute() }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @NotForDocumentType(DocumentType.SLIDES)
    fun allButSlidesGreet(): StringValue = StringValue("Hello")

    @Test
    fun `KFunction with non-blacklisted document type`() {
        val call = createCallForDocumentType(::allButSlidesGreet, DocumentType.PAGED)
        assertEquals("Hello", call.execute().unwrappedValue)
    }

    @Test
    fun `KFunction with blacklisted document type`() {
        val call = createCallForDocumentType(::allButSlidesGreet, DocumentType.SLIDES)
        assertFailsWith<InvalidFunctionCallException> { call.execute() }
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
                        FunctionCallArgument(DynamicValue("2")),
                        FunctionCallArgument(DynamicValue("5")),
                    ),
            )

        assertEquals(7, dynamicCall.execute().unwrappedValue)
    }
}
