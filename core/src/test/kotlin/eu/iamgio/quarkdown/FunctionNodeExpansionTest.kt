package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.CheckBox
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
import eu.iamgio.quarkdown.function.library.LibraryRegistrant
import eu.iamgio.quarkdown.function.library.loader.MultiFunctionLibraryLoader
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
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
            BlockQuote(body.children),
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
        expander = FunctionCallNodeExpander(context)
    }

    @Test
    fun `sum expansion`() {
        val node =
            FunctionCallNode(
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
        assertEquals(Text("5"), node.children.first())
    }

    @Test
    fun `sum expansion, failing`() {
        val node =
            FunctionCallNode(
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
            assertIs<Text>(this)
            assertTrue("sum(" in text) // Error message
        }
    }

    @Test
    fun `custom function name`() {
        val node =
            FunctionCallNode(
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
        assertEquals(Text("abc"), node.children.first())
    }

    @Test
    fun `custom function name, failing`() {
        val node =
            FunctionCallNode(
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
            assertIs<Text>(this)
            println(text)
            assertTrue("reference" in text) // Unresolved reference error.
        }
    }

    @Test
    fun `resource content expansion, failing`() {
        val node =
            FunctionCallNode(
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
            assertIs<Text>(this)
            assertTrue("error" in text)
        }
    }

    @Test
    fun `resource content expansion as inline`() {
        val node =
            FunctionCallNode(
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
        assertEquals(Text("Hello Quarkdown!"), node.children.first())
    }

    @Test
    fun `resource content expansion as block`() {
        val node =
            FunctionCallNode(
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
        assertEquals(Paragraph(listOf(Text("Hello Quarkdown!"))), node.children.first())
    }

    @Test
    fun `boolean expansion`() {
        val node =
            FunctionCallNode(
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
        assertEquals(CheckBox(isChecked = true), node.children.first())
    }

    @Test
    fun `enum lookup, failing`() {
        val node =
            FunctionCallNode(
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
            assertIs<Text>(this)
            assertTrue("No such element" in text)
        }
    }

    @Test
    fun `context injection`() {
        val node =
            FunctionCallNode(
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
        assertEquals(Text("New name"), node.children.first())
    }

    @Test
    fun `markdown argument`() {
        val node =
            FunctionCallNode(
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
        assertEquals(
            BlockQuote(
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
