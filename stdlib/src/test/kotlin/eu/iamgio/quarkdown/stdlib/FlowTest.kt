package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.BaseListItem
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.UnorderedList
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.error.InvalidLambdaArgumentCountException
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.output.NodeOutputValueVisitor
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.options.MutablePipelineOptions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * [Flow] module tests.
 */
class FlowTest {
    private val context = MutableContext(QuarkdownFlavor)

    @BeforeTest
    fun setup() {
        // Mock attached pipeline to parse nested Markdown.
        Pipeline(context, MutablePipelineOptions(), emptySet(), renderer = { _, _ -> throw UnsupportedOperationException() })
    }

    private fun call(
        functionName: String,
        arguments: List<FunctionCallArgument>,
    ): OutputValue<*> {
        with(context.getFunctionByName(functionName)) {
            assertNotNull(this)
            assertEquals(functionName, name)
            assertEquals(arguments.size, parameters.size)

            val call = FunctionCall(this, arguments, context)
            return call.execute()
        }
    }

    @Test
    fun `custom function`() {
        function(
            context,
            name = "myfunc1",
            body = Lambda(context, explicitParameters = emptyList()) { "Hello Quarkdown" },
        )

        assertEquals(
            DynamicValue("Hello Quarkdown"),
            call("myfunc1", arguments = emptyList()),
        )

        function(
            context,
            name = "myfunc2",
            body = ValueFactory.lambda("- Hello **Quarkdown**\n- Hello", context).unwrappedValue,
        )

        call("myfunc2", arguments = emptyList()).let {
            assertIs<DynamicValue>(it)
            assertEquals("- Hello **Quarkdown**\n- Hello", it.unwrappedValue)

            // Node conversion
            val node = NodeOutputValueVisitor(context).visit(it)
            assertIs<MarkdownContent>(node)
            assertEquals(1, node.children.size)

            val list = node.children.first()
            assertIs<UnorderedList>(list)
            assertEquals(2, list.children.size)
            assertIs<BaseListItem>(list.children[0])
            assertIs<BaseListItem>(list.children[1])
        }

        function(
            context,
            name = "myfunc3",
            body = ValueFactory.lambda("to from: Hello **.to** from _.from_", context).unwrappedValue,
        )

        assertEquals(
            DynamicValue("Hello **Quarkdown** from _iamgio_"),
            call(
                "myfunc3",
                arguments =
                    listOf(
                        FunctionCallArgument(DynamicValue("Quarkdown")),
                        FunctionCallArgument(DynamicValue("iamgio")),
                    ),
            ),
        )
    }

    @Test
    fun `control flow`() {
        val control1 =
            `if`(
                isLower(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = emptyList()) { "Hello Quarkdown" },
            )
        assertEquals("Hello Quarkdown", control1.unwrappedValue)

        val control2 =
            `if`(
                isGreater(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = emptyList()) { "Hello Quarkdown" },
            )
        assertEquals(VoidValue, control2)

        val control3 =
            ifNot(
                isGreater(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = emptyList()) { "Hello Quarkdown" },
            )
        assertEquals("Hello Quarkdown", control3.unwrappedValue)

        assertFailsWith<InvalidLambdaArgumentCountException> {
            `if`(
                isLower(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = listOf("a")) { "Hello Quarkdown" },
            )
        }
    }

    @Test
    fun `loop flow`() {
        val loop1 =
            forEach(
                listOf(
                    StringValue("Hello"),
                    StringValue("Quarkdown"),
                ),
                body = Lambda(context, explicitParameters = emptyList()) { "**${it.first().unwrappedValue}**" },
            )

        assertEquals(
            listOf(
                DynamicValue("**Hello**"),
                DynamicValue("**Quarkdown**"),
            ),
            loop1.unwrappedValue,
        )

        val loop2 =
            forEach(
                Range(start = 2, end = 4),
                body = ValueFactory.lambda("n: \nN: .n", context).unwrappedValue, // Explicit lambda placeholder
            )

        assertEquals(
            listOf(
                DynamicValue("N: 2"),
                DynamicValue("N: 3"),
                DynamicValue("N: 4"),
            ),
            loop2.unwrappedValue,
        )

        val loop3 =
            forEach(
                ValueFactory.range("..4").unwrappedValue,
                body = ValueFactory.lambda("N\\: .1", context).unwrappedValue,
            )

        assertEquals(
            listOf(
                DynamicValue("N: 0"),
                DynamicValue("N: 1"),
                DynamicValue("N: 2"),
                DynamicValue("N: 3"),
            ),
            loop3.unwrappedValue,
        )

        // Iterating ranges with indefinite right end is not allowed.
        assertFails {
            forEach(
                ValueFactory.range("1..").unwrappedValue,
                body = ValueFactory.lambda("N\\: .1", context).unwrappedValue,
            )
        }
    }
}
