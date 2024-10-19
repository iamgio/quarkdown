package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.inline.CheckBox
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.ast.quarkdown.block.Aligned
import eu.iamgio.quarkdown.ast.quarkdown.block.Box
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
import eu.iamgio.quarkdown.function.library.LibraryRegistrant
import eu.iamgio.quarkdown.function.library.loader.MultiFunctionLibraryLoader
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.util.toPlainText
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for functions called from a Quarkdown source.
 * For independent function call tests see [StandaloneFunctionTest].
 */
class FunctionNodeExpansionTest {
    private lateinit var context: MutableContext
    private lateinit var expander: FunctionCallNodeExpander

    @Suppress("MemberVisibilityCanBePrivate")
    fun sum(
        a: Number,
        b: Number,
    ) = NumberValue(a.toFloat() + b.toFloat())

    @Name("customfunction")
    fun myFunction(x: String) = StringValue(x)

    @Suppress("MemberVisibilityCanBePrivate")
    fun echoBoolean(value: Boolean) = BooleanValue(value)

    @Suppress("MemberVisibilityCanBePrivate")
    fun echoEnum(value: Aligned.Alignment) = StringValue(value.name)

    @Suppress("MemberVisibilityCanBePrivate")
    fun resourceContent(path: String) = StringValue(javaClass.getResourceAsStream("/function/$path")!!.reader().readText())

    @Suppress("MemberVisibilityCanBePrivate")
    fun setAndEchoDocumentName(
        @Injected context: Context,
        name: String,
    ): StringValue {
        context.documentInfo.name = name
        return StringValue(context.documentInfo.name!!)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun makeQuote(body: MarkdownContent) =
        NodeValue(
            BlockQuote(children = body.children),
        )

    @BeforeTest
    fun setup() {
        context = MutableContext(QuarkdownFlavor)

        // Initialization attaches the pipeline to the context.
        // This is used to parse Markdown content in arguments.
        context.attachMockPipeline()

        val library =
            MultiFunctionLibraryLoader("lib").load(
                setOf(
                    ::sum,
                    ::myFunction,
                    ::echoBoolean,
                    ::echoEnum,
                    ::resourceContent,
                    ::setAndEchoDocumentName,
                    ::makeQuote,
                ),
            )

        LibraryRegistrant(context).registerAll(listOf(library))
        expander = FunctionCallNodeExpander(context, BasePipelineErrorHandler())
    }

    @Test
    fun `sum expansion`() {
        val node =
            FunctionCallNode(
                context,
                "sum",
                listOf(
                    FunctionCallArgument(DynamicValue("2")),
                    FunctionCallArgument(DynamicValue("3")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertNodeEquals(Text("5"), node.children.first())
    }

    @Test
    fun `sum expansion, failing`() {
        val node =
            FunctionCallNode(
                context,
                "sum",
                listOf(
                    FunctionCallArgument(DynamicValue("2")),
                    FunctionCallArgument(DynamicValue("a")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Box>(this) // Error box
            assertTrue("sum(" in (this.children.first() as TextNode).text.toPlainText()) // Error message
        }
    }

    @Test
    fun `custom function name`() {
        val node =
            FunctionCallNode(
                context,
                "customfunction",
                listOf(
                    FunctionCallArgument(DynamicValue("abc")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertNodeEquals(Text("abc"), node.children.first())
    }

    @Test
    fun `custom function name, failing`() {
        val node =
            FunctionCallNode(
                context,
                "myFunction",
                listOf(
                    FunctionCallArgument(DynamicValue("abc")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Box>(this) // Error box
            assertTrue("reference" in (this.children.first() as TextNode).text.toPlainText()) // Unresolved reference error message
        }
    }

    @Test
    fun `resource content expansion, failing`() {
        val node =
            FunctionCallNode(
                context,
                "resourceContent",
                listOf(
                    FunctionCallArgument(DynamicValue("non-existant-resource")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Box>(this) // Error box
        }
    }

    @Test
    fun `resource content expansion as inline`() {
        val node =
            FunctionCallNode(
                context,
                "resourceContent",
                listOf(
                    FunctionCallArgument(DynamicValue("hello.txt")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertNodeEquals(Text("Hello Quarkdown!"), node.children.first())
    }

    @Test
    fun `resource content expansion as block`() {
        val node =
            FunctionCallNode(
                context,
                "resourceContent",
                listOf(
                    FunctionCallArgument(DynamicValue("hello.txt")),
                ),
                isBlock = true,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        // The function call is block, so the output is wrapped in a paragraph.
        assertNodeEquals(Paragraph(listOf(Text("Hello Quarkdown!"))), node.children.first())
    }

    @Test
    fun `boolean expansion`() {
        val node =
            FunctionCallNode(
                context,
                "echoBoolean",
                listOf(
                    FunctionCallArgument(DynamicValue("yes")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertNodeEquals(CheckBox(isChecked = true), node.children.first())
    }

    @Test
    fun `enum lookup, failing`() {
        val node =
            FunctionCallNode(
                context,
                "echoEnum",
                listOf(
                    FunctionCallArgument(DynamicValue("non-existant-value")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Box>(this) // Error box
            assertTrue("No such element" in (this.children.first() as TextNode).text.toPlainText()) // Error message
        }
    }

    @Test
    fun `context injection`() {
        val node =
            FunctionCallNode(
                context,
                "setAndEchoDocumentName",
                listOf(
                    FunctionCallArgument(DynamicValue("New name")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())
        assertNull(context.documentInfo.name)

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertEquals("New name", context.documentInfo.name)
        assertNodeEquals(Text("New name"), node.children.first())
    }

    @Test
    fun `markdown argument`() {
        val node =
            FunctionCallNode(
                context,
                "makeQuote",
                listOf(
                    FunctionCallArgument(DynamicValue("Hello **world**")),
                ),
                isBlock = false,
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertNodeEquals(
            BlockQuote(
                children =
                    listOf(
                        Paragraph(
                            listOf(Text("Hello "), Strong(listOf(Text("world")))),
                        ),
                    ),
            ),
            node.children.first(),
        )
    }
}
