package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.pipeline.stage.then
import kotlin.test.Test
import kotlin.test.assertEquals

private class DoubleNumberStage : PipelineStage<Int, Int> {
    override val hook = null

    override fun process(
        input: Int,
        data: SharedPipelineData,
    ): Int = input * 2
}

private class ToStringStage : PipelineStage<Int, String> {
    override val hook = null

    override fun process(
        input: Int,
        data: SharedPipelineData,
    ): String = input.toString()
}

/**
 * Tests for the [PipelineStage] composition and processing.
 */
class PipelineStageTest {
    private val context = MutableContext(QuarkdownFlavor)
    private val pipeline = context.attachMockPipeline()
    private val data = SharedPipelineData(pipeline, context)

    @Test
    fun `stage processed once`() {
        val output = DoubleNumberStage().process(3, data)
        assertEquals(6, output)
    }

    @Test
    fun `stage processed twice`() {
        val stage = DoubleNumberStage()
        val firstOutput = stage.process(3, data)
        val secondOutput = stage.process(firstOutput, data)
        assertEquals(12, secondOutput)
    }

    @Test
    fun `composed twice`() {
        val chain = DoubleNumberStage() then DoubleNumberStage()
        val output = chain.process(3, data)
        assertEquals(12, output)
    }

    @Test
    fun `composed three times`() {
        val chain = DoubleNumberStage() then DoubleNumberStage() then DoubleNumberStage()
        val output = chain.process(3, data)
        assertEquals(24, output)
    }

    @Test
    fun `composed to different output type`() {
        val chain = DoubleNumberStage() then ToStringStage()
        val output = chain.process(3, data)
        assertEquals("6", output)
    }
}
