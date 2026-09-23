package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.Pipelines
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class PipelinesTest {
    @Test
    fun `detach clears the attached pipeline`() {
        val context = MutableContext()
        context.attachMockPipeline()
        assertNotNull(context.attachedPipeline)

        Pipelines.detach(context)

        assertNull(context.attachedPipeline)
    }

    @Test
    fun `detach allows attaching again`() {
        val context = MutableContext()
        context.attachMockPipeline()
        Pipelines.detach(context)
        context.attachMockPipeline()
        assertNotNull(context.attachedPipeline)
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
