package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.ImportedDefault
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/** Source-file imports are copied into the generated wrapper. */
class ImportCopyingTest {
    @Test
    fun `wrapper carries the source's import list verbatim`() {
        val source = GeneratedFiles.sourceOf("ImportedDefault")
        // The default references NoneValue by its unqualified name; the import must survive.
        assertContains(source, "import com.quarkdown.core.function.value.NoneValue")
        assertContains(source, "= NoneValue")
    }

    @Test
    fun `wrapper compiles when a default references an imported symbol`() {
        // The mere existence of ImportedDefault.Module (referenced at compile time) proves the
        // generated wrapper compiled - which requires the copied import to resolve NoneValue.
        val exported = ImportedDefault.Module.map { it.name }.toSet()
        assertEquals(setOf("withImportedDefault"), exported)
    }
}
