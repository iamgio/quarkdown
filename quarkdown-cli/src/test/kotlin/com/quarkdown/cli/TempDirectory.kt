package com.quarkdown.cli

import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Base class for tests that require a temporary directory.
 */
open class TempDirectory {
    protected val directory: File =
        createTempDirectory()
            .toFile()

    protected fun reset() {
        directory.deleteRecursively()
        directory.mkdirs()
    }
}
