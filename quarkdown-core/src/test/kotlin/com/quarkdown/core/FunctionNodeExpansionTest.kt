package com.quarkdown.core

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.call.FunctionCallNodeExpander
import com.quarkdown.core.function.library.LibraryRegistrant
import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.core.util.toPlainText
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
    @NotForDocumentType(DocumentType.SLIDES)
    fun myFunction(x: String) = StringValue(x)

    @Suppress("MemberVisibilityCanBePrivate")
    fun echoBoolean(value: Boolean) = BooleanValue(value)

    @Suppress("MemberVisibilityCanBePrivate")
    fun echoEnum(value: Container.Alignment) = StringValue(value.name)

    @Suppress("MemberVisibilityCanBePrivate")
    fun resourceContent(path: String) = StringValue(javaClass.getResourceAsStream("/function/$path")!!.reader().readText())

    @Suppress("MemberVisibilityCanBePrivate")
    fun setAndEchoDocumentName(
        @Injected context: MutableContext,
        name: String,
    ): StringValue {
        context.documentInfo = context.documentInfo.copy(name = name)
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
                moduleOf(
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

    @Test
    fun `invalid document type`() {
        context.documentInfo = DocumentInfo(type = DocumentType.SLIDES)
        val node =
            FunctionCallNode(
                context,
                "myFunction",
                listOf(FunctionCallArgument(DynamicValue("abc"))),
                isBlock = false,
            )

        context.register(node)

        expander.expandAll()

        with(node.children.first()) {
            assertIs<Box>(this)
            assertEquals(Box.Type.ERROR, this.type)
        }
    }
}
