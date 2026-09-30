package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.SubdocumentContext
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.pipeline.Pipeline
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for attaching and detaching pipelines to and from contexts.
 */
class PipelineAttachmentTest {
    @Test
    fun `close detaches the attached pipeline`() {
        val context = MutableContext()
        context.attachMockPipeline()
        assertNotNull(context.attachedPipeline)

        context.close()

        assertNull(context.attachedPipeline)
    }

    @Test
    fun `close allows attaching again`() {
        val context = MutableContext()
        context.attachMockPipeline()
        context.close()
        context.attachMockPipeline()
        assertNotNull(context.attachedPipeline)
    }

    /**
     * Registers a new subdocument context of [this] in the shared subdocument data, with its own mock pipeline attached.
     */
    private fun MutableContext.attachSubdocumentContext(name: String): SubdocumentContext {
        val subdocument = Subdocument.Resource(name = name, path = "$name.qd", workingDirectory = null, content = "")
        val subContext = SubdocumentContext(parent = this, subdocument = subdocument)
        sharedSubdocumentsData = sharedSubdocumentsData.addContext(subdocument, subContext)
        subContext.attachMockPipeline()
        return subContext
    }

    @Test
    fun `close detaches subdocument contexts`() {
        val context = MutableContext()
        context.attachMockPipeline()
        val subContext = context.attachSubdocumentContext("sub")

        context.close()

        assertNull(subContext.attachedPipeline)
    }

    @Test
    fun `closing a subdocument context detaches only its own pipeline`() {
        val context = MutableContext()
        val rootPipeline = context.attachMockPipeline()
        val subContext = context.attachSubdocumentContext("sub")
        val sibling = context.attachSubdocumentContext("sibling")
        val siblingPipeline = sibling.attachedPipeline

        subContext.close()

        // The subdocument context falls back to the root's pipeline once its own is detached.
        assertSame(rootPipeline, subContext.attachedPipeline)
        assertSame(rootPipeline, context.attachedPipeline)
        assertSame(siblingPipeline, sibling.attachedPipeline)
    }

    @Test
    fun `attaching the same pipeline again is a no-op`() {
        val context = MutableContext()
        val pipeline = context.attachMockPipeline()
        context.open(pipeline)
        assertSame(pipeline, context.attachedPipeline)
    }

    @Test
    fun `attaching a different pipeline fails`() {
        val context = MutableContext()
        val pipeline = context.attachMockPipeline()
        assertFailsWith<IllegalStateException> {
            context.open(
                Pipeline(context, pipeline.options, emptySet(), renderer = {
                    _,
                    _,
                    ->
                    throw UnsupportedOperationException()
                }),
            )
        }
    }
}
