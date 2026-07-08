package com.quarkdown.processor.integration

import java.io.File

/**
 * Test-side helper for locating and reading the `*.kt` files produced by the KSP round in the
 * current test-source compilation.
 *
 * Every integration test needs to answer the same two questions - "was file X emitted at all?"
 * and "what does its source look like?" - so both live here rather than duplicated per test class.
 * Paths are relative to the module's working directory, which is Gradle's convention when it
 * invokes the test task.
 */
internal object GeneratedFiles {
    /**
     * KSP's test-round output directory for the fixtures package, relative to the module dir.
     * The fixtures live in `com.quarkdown.processor.fixtures`, so generated files land at
     * `<pkg-as-dir>/<SourceName>Module.kt` under this root.
     */
    private const val OUTPUT_DIR = "build/generated/ksp/test/kotlin/com/quarkdown/processor/fixtures"

    /**
     * Returns the source text of the `<sourceModuleName>Module.kt` file produced for the given
     * fixture. Throws with the search path baked into the message if the file is not present -
     * the most common cause is a fixture that never triggered generation (missing `@file:QModule`)
     * or a validator error that failed the KSP round before the emitter ran.
     */
    fun sourceOf(sourceModuleName: String): String =
        find(sourceModuleName)?.readText()
            ?: error("No generated file for '$sourceModuleName'. Looked at $OUTPUT_DIR")

    /**
     * Returns the [File] for `<sourceModuleName>Module.kt`, or `null` when it wasn't produced.
     * Used by output-layout assertions that want to verify existence without reading the content.
     */
    fun find(sourceModuleName: String): File? {
        val file = File(OUTPUT_DIR, "${sourceModuleName}Module.kt")
        return file.takeIf { it.exists() }
    }
}
