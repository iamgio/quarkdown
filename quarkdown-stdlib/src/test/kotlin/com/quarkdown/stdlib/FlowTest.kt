package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.error.InvalidLambdaArgumentCountException
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.function.value.output.node.BlockNodeOutputValueVisitor
import com.quarkdown.core.function.value.output.node.InlineNodeOutputValueVisitor
import com.quarkdown.core.function.value.wrappedAsValue
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
        context.attachMockPipeline()
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
            body = Lambda(context, explicitParameters = emptyList()) { _, _ -> "Hello Quarkdown".wrappedAsValue() },
        )

        assertEquals(
            StringValue("Hello Quarkdown"),
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

            // Block node conversion
            val blockNode = BlockNodeOutputValueVisitor(context).visit(it)
            assertIs<MarkdownContent>(blockNode)
            assertEquals(1, blockNode.children.size)

            // Inline node conversion
            val inlineNode = InlineNodeOutputValueVisitor(context).visit(it)
            assertIs<InlineMarkdownContent>(inlineNode)
            assertEquals(3, inlineNode.children.size)

            val list = blockNode.children.first()
            assertIs<UnorderedList>(list)
            assertEquals(2, list.children.size)
            assertIs<ListItem>(list.children[0])
            assertIs<ListItem>(list.children[1])
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
                Lambda(context, explicitParameters = emptyList()) { _, _ -> "Hello Quarkdown".wrappedAsValue() },
            )
        assertEquals("Hello Quarkdown", control1.unwrappedValue)

        val control2 =
            `if`(
                isGreater(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = emptyList()) { _, _ -> "Hello Quarkdown".wrappedAsValue() },
            )
        assertEquals(VoidValue, control2)

        val control3 =
            ifNot(
                isGreater(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = emptyList()) { _, _ -> "Hello Quarkdown".wrappedAsValue() },
            )
        assertEquals("Hello Quarkdown", control3.unwrappedValue)

        assertFailsWith<InvalidLambdaArgumentCountException> {
            `if`(
                isLower(2, 4).unwrappedValue,
                Lambda(context, explicitParameters = listOf(LambdaParameter("a"))) { _, _ -> "Hello Quarkdown".wrappedAsValue() },
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
                body =
                    Lambda(context, explicitParameters = emptyList()) { args, _ ->
                        "**${args.first().unwrappedValue}**".wrappedAsValue()
                    },
            )

        assertEquals(
            listOf(
                StringValue("**Hello**"),
                StringValue("**Quarkdown**"),
            ),
            loop1.unwrappedValue,
        )

        val loop2 =
            forEach(
                Range(start = 2, end = 4),
                // Explicit lambda placeholder
                body = ValueFactory.lambda("n: \nN: .n", context).unwrappedValue,
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
                DynamicValue("N: 1"),
                DynamicValue("N: 2"),
                DynamicValue("N: 3"),
                DynamicValue("N: 4"),
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
