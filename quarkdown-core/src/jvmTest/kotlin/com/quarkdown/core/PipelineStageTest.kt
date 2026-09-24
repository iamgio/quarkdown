package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.pipeline.stage.then
import com.quarkdown.core.pipeline.stage.thenOptionally
import kotlin.test.Test
import kotlin.test.assertEquals

private object DoubleNumberStage : PipelineStage<Int, Int> {
    override val hook = null

    override fun process(
        input: Int,
        data: SharedPipelineData,
    ): Int = input * 2
}

private object ToStringStage : PipelineStage<Int, String> {
    override val hook = null

    override fun process(
        input: Int,
        data: SharedPipelineData,
    ): String = input.toString()
}

private const val INPUT = 3
private const val DOUBLED_ONCE = INPUT * 2
private const val DOUBLED_TWICE = DOUBLED_ONCE * 2

/**
 * Tests for the [PipelineStage] composition and processing.
 */
class PipelineStageTest {
    private val context = MutableContext(QuarkdownFlavor)
    private val pipeline = context.attachMockPipeline()
    private val data = SharedPipelineData(pipeline, context)

    @Test
    fun `stage processed once`() {
        val output = DoubleNumberStage.process(INPUT, data)
        assertEquals(DOUBLED_ONCE, output)
    }

    @Test
    fun `stage processed twice`() {
        val stage = DoubleNumberStage
        val firstOutput = stage.process(INPUT, data)
        val secondOutput = stage.process(firstOutput, data)
        assertEquals(DOUBLED_TWICE, secondOutput)
    }

    @Test
    fun `composed twice`() {
        val chain = DoubleNumberStage then DoubleNumberStage
        val output = chain.process(INPUT, data)
        assertEquals(DOUBLED_TWICE, output)
    }

    @Test
    fun `composed three times`() {
        val chain = DoubleNumberStage then DoubleNumberStage then DoubleNumberStage
        val output = chain.process(INPUT, data)
        assertEquals(24, output)
    }

    @Test
    fun `composed to different output type`() {
        val chain = DoubleNumberStage then ToStringStage
        val output = chain.process(INPUT, data)
        assertEquals("6", output)
    }

    @Test
    fun `composed optionally, not null`() {
        val chain = DoubleNumberStage thenOptionally DoubleNumberStage
        val output = chain.process(INPUT, data)
        assertEquals(DOUBLED_TWICE, output)
    }

    @Test
    fun `composed optionally, null`() {
        val chain = DoubleNumberStage thenOptionally null
        val output = chain.process(INPUT, data)
        assertEquals(DOUBLED_ONCE, output)
    }
}
