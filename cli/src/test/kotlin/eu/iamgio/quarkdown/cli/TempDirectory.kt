package eu.iamgio.quarkdown.cli

import java.io.File

/**
 * Base class for tests that require a temporary directory.
 */
open class TempDirectory {
    protected val directory: File =
        kotlin.io.path
            .createTempDirectory()
            .toFile()
}
