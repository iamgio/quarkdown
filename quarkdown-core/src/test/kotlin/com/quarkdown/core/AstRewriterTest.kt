package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.iterator.AstRewriter
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.library.LibraryRegistrant
import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.util.node.toPlainText
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [AstRewriter]: ensures Markdown primitives implementing
 * [com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode]
 * are wrapped into [FunctionCallNode]s exactly when their backing function has been
 * marked as extended in the context, and that the surrounding tree is preserved.
 */
class AstRewriterTest {
    private lateinit var context: MutableContext
    private lateinit var rewriter: AstRewriter

    @Suppress("MemberVisibilityCanBePrivate")
    fun heading(
        @Body content: InlineMarkdownContent,
        depth: Int,
        ref: String? = null,
        numbered: Boolean = true,
        indexed: Boolean = true,
        breakpage: Boolean = true,
    ): NodeValue =
        NodeValue(
            BlockQuote(
                content =
                    listOf(
                        Heading(
                            depth = depth,
                            text = content.children,
                            customId = ref,
                            canBreakPage = breakpage,
                            canTrackLocation = numbered,
                            excludeFromTableOfContents = !indexed,
                        ),
                    ),
            ),
        )

    @BeforeTest
    fun setup() {
        context = MutableContext(QuarkdownFlavor)
        // Required by FunctionCallNodeExpander.errorHandler!! within AstRewriter.
        context.attachMockPipeline()

        val library = MultiFunctionLibraryLoader("lib").load(moduleOf(::heading))
        LibraryRegistrant(context).registerAll(listOf(library))

        rewriter = AstRewriter(context)
    }

    private fun newHeading() = Heading(depth = 2, text = listOf(Text("Hello")))

    @Test
    fun `no extensions leaves all primitives untouched`() {
        val original = AstRoot(listOf(newHeading()))
        val result = rewriter.traverse(original)

        // No "heading" has been marked extended, so the rewriter must not swap the primitive.
        assertEquals(1, result.children.size)
        val child = result.children.single()
        assertIs<Heading>(child)
        assertEquals("Hello", child.text.toPlainText())
    }

    @Test
    fun `non-primitive nodes are preserved`() {
        val paragraph = Paragraph(listOf(Text("Just text")))
        val original = AstRoot(listOf(paragraph))
        val result = rewriter.traverse(original)

        val child = assertIs<Paragraph>(result.children.single())
        assertEquals("Just text", child.text.toPlainText())
    }

    @Test
    fun `extended primitive at root is wrapped and expanded`() {
        context.markFunctionAsExtended("heading")

        val original = AstRoot(listOf(newHeading()))
        val result = rewriter.traverse(original)

        // The primitive must have been replaced by a FunctionCallNode for "heading"...
        val call = assertIs<FunctionCallNode>(result.children.single())
        assertEquals("heading", call.name)

        // ...and that call must have been expanded by the rewriter (i.e. children populated).
        val expanded = assertIs<BlockQuote>(call.children.single())
        val wrappedHeading = assertIs<Heading>(expanded.content.single())
        assertEquals(2, wrappedHeading.depth)
        assertEquals("Hello", wrappedHeading.text.toPlainText())
    }

    @Test
    fun `unextended primitive remains even when other functions are extended`() {
        context.markFunctionAsExtended("someOtherFunction")

        val original = AstRoot(listOf(newHeading()))
        val result = rewriter.traverse(original)

        // The "heading" function isn't extended here, so the primitive must be left alone.
        assertIs<Heading>(result.children.single())
    }

    @Test
    fun `extension reaches primitives nested inside a container`() {
        context.markFunctionAsExtended("heading")

        val original =
            AstRoot(
                listOf(
                    Container(children = listOf(newHeading())),
                ),
            )
        val result = rewriter.traverse(original)

        // The outer Container is preserved...
        val container = assertIs<Container>(result.children.single())
        // ...but the nested Heading must have been wrapped and expanded.
        val call = assertIs<FunctionCallNode>(container.children.single())
        assertEquals("heading", call.name)
        assertTrue(call.children.isNotEmpty(), "Nested call must be expanded too")
    }

    @Test
    fun `extension reaches deeply nested primitives`() {
        context.markFunctionAsExtended("heading")

        val original =
            AstRoot(
                listOf(
                    Container(
                        children =
                            listOf(
                                Container(children = listOf(newHeading())),
                            ),
                    ),
                ),
            )
        val result = rewriter.traverse(original)

        val outer = assertIs<Container>(result.children.single())
        val inner = assertIs<Container>(outer.children.single())
        val call = assertIs<FunctionCallNode>(inner.children.single())
        assertEquals("heading", call.name)
        assertTrue(call.children.isNotEmpty())
    }

    @Test
    fun `mixed extended and non-primitive children are handled independently`() {
        context.markFunctionAsExtended("heading")

        val paragraph = Paragraph(listOf(Text("Plain")))
        val heading = newHeading()
        val original = AstRoot(listOf(paragraph, heading))
        val result = rewriter.traverse(original)

        assertEquals(2, result.children.size)
        // The paragraph passes through unchanged (no nested primitives, no function call).
        val rewrittenParagraph = assertIs<Paragraph>(result.children[0])
        assertEquals("Plain", rewrittenParagraph.text.toPlainText())
        // The heading is wrapped into a function call.
        val call = assertIs<FunctionCallNode>(result.children[1])
        assertEquals("heading", call.name)
    }

    @Test
    fun `unextended primitive is preserved structurally when other functions are extended`() {
        // Extend some unrelated name so that the rewriter still runs the traversal.
        context.markFunctionAsExtended("someOtherFunction")

        val heading = newHeading()
        val original = AstRoot(listOf(heading))
        val result = rewriter.traverse(original)

        val child: Node = result.children.single()
        // The rewriter may rebuild nodes via withChildren, so identity is not guaranteed,
        // but the underlying primitive type and its data must be preserved verbatim.
        val rewritten = assertIs<Heading>(child)
        assertEquals(heading.depth, rewritten.depth)
        assertEquals(heading.text.toPlainText(), rewritten.text.toPlainText())
    }
}
