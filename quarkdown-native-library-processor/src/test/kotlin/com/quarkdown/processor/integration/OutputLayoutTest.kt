package com.quarkdown.processor.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/** Filename convention: `<SourceName>.kt` -> `<SourceName>Module.kt` in the same package. */
class OutputLayoutTest {
    @Test
    fun `every @QModule fixture produces a Module file next to its source`() {
        assertNotNull(GeneratedFiles.find("SimpleLogger"))
        assertNotNull(GeneratedFiles.find("NamedFunction"))
        assertNotNull(GeneratedFiles.find("NamedParameter"))
        assertNotNull(GeneratedFiles.find("MultipleFunctions"))
        assertNotNull(GeneratedFiles.find("Generics"))
        assertNotNull(GeneratedFiles.find("EmptyModule"))
    }
}
