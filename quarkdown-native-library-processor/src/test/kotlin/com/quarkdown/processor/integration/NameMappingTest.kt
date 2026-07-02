package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.NamedFunction
import com.quarkdown.processor.fixtures.NamedParameter
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** `@Name` rewrites at the function and parameter level. */
class NameMappingTest {
    @Test
    fun `named function is exported under its @Name value, not the source name`() {
        val exported = NamedFunction.Module.map { it.name }.toSet()
        assertEquals(setOf("renamedLog"), exported)
    }

    @Test
    fun `function rename affects the wrapper and the module ref, delegation still uses source name`() {
        val source = GeneratedFiles.sourceOf("NamedFunction")
        assertContains(source, "this::`renamedLog`,")
        assertContains(source, "public fun `renamedLog`(`message`: kotlin.String)")
        assertContains(source, "com.quarkdown.processor.fixtures.`logInternal`(`message` = `message`)")
        // The source-level name should not appear as an exposed wrapper or a module reference,
        // otherwise callers could still reach it under its pre-rename identity.
        assertTrue("public fun `logInternal`(" !in source, "wrapper still uses original function name")
        assertTrue("this::`logInternal`," !in source, "moduleOf still references original function name")
    }

    @Test
    fun `parameter rename does not change the exported function name`() {
        val exported = NamedParameter.Module.map { it.name }.toSet()
        assertEquals(setOf("logWithNamedParam"), exported)
    }

    @Test
    fun `parameter rename changes the wrapper param but the delegation keeps the source keyword`() {
        val source = GeneratedFiles.sourceOf("NamedParameter")
        assertContains(source, "public fun `logWithNamedParam`(`renamedText`: kotlin.String)")
        assertContains(source, "com.quarkdown.processor.fixtures.`logWithNamedParam`(`text` = `renamedText`)")
    }
}
