package com.quarkdown.stdlib

import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.pipeline.PipelineOptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProcessTest {
    private fun context() = MutableContext().apply { attachMockPipeline(PipelineOptions(permissions = Permission.ALL)) }

    @Test
    fun `reads an existing variable`() {
        val value = env(context(), "PATH")
        assertIs<StringValue>(value)
        assertEquals(System.getenv("PATH"), value.unwrappedValue)
    }

    @Test
    fun `missing variable is none`() {
        assertEquals(NoneValue, env(context(), "QUARKDOWN_SURELY_UNSET_${System.nanoTime()}"))
    }
}
