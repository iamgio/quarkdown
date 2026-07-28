package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.SimpleLogger
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Baseline wrapper shape when no `@Name` rewrites are involved.
 */
class WrapperGenerationTest {
    @Test
    fun `module exports the source function under its original name`() {
        val exported = SimpleLogger.Module.map { it.name }.toSet()
        assertEquals(setOf("logSimple"), exported)
    }

    @Test
    fun `moduleOf references the pre-built function value`() {
        val source = GeneratedFiles.sourceOf("SimpleLogger")
        assertContains(source, "moduleOf(")
        // Pre-built Function values replace the `this::name` KFunction references so dispatch
        // no longer needs KFunctionAdapter at load time.
        assertContains(source, "`logSimple__function`,")
    }

    @Test
    fun `wrapper delegates to the source function via FQN and a named argument`() {
        val source = GeneratedFiles.sourceOf("SimpleLogger")
        assertContains(source, "public fun `logSimple`(`message`: kotlin.String)")
        assertContains(source, "com.quarkdown.processor.fixtures.`logSimple`(`message` = `message`)")
    }
}
