package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.SimpleLogger
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/** Baseline wrapper shape when no `@Name` rewrites are involved. */
class WrapperGenerationTest {
    @Test
    fun `module exports the source function under its original name`() {
        val exported = SimpleLogger.Module.map { it.name }.toSet()
        assertEquals(setOf("logSimple"), exported)
    }

    @Test
    fun `moduleOf references the wrapper via this-qualification`() {
        val source = GeneratedFiles.sourceOf("SimpleLogger")
        assertContains(source, "moduleOf(")
        assertContains(source, "this::`logSimple`,")
    }

    @Test
    fun `wrapper delegates to the source function via FQN and a named argument`() {
        val source = GeneratedFiles.sourceOf("SimpleLogger")
        assertContains(source, "public fun `logSimple`(`message`: kotlin.String)")
        assertContains(source, "com.quarkdown.processor.fixtures.`logSimple`(`message` = `message`)")
    }
}
