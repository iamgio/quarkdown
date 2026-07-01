package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.EmptyModule
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** `@file:QModule` file with no `@QFunction`. */
class EmptyModuleTest {
    @Test
    fun `empty @QModule file exports an empty Module value`() {
        assertEquals(emptySet(), EmptyModule.Module.toSet())
    }

    @Test
    fun `empty module still emits the object wrapper with no functions inside it`() {
        val source = GeneratedFiles.sourceOf("EmptyModule")

        assertContains(source, "object EmptyModule {")
        assertContains(source, "moduleOf(")
        assertTrue("this::" !in source, "empty module still references some function")
        assertTrue("public fun" !in source, "empty module still emits a wrapper")
    }
}
