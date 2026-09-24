package com.quarkdown.core.filesystem

import okio.Path.Companion.toPath

/**
 * Utilities for raw path strings, backing checks that do not require a [FileSystem].
 */
object FsPaths {
    /**
     * Matches a Windows drive-letter prefix (e.g. `C:\` or `C:/`).
     */
    private val WINDOWS_DRIVE_REGEX = Regex("""^[A-Za-z]:[/\\]""")

    /**
     * @param path a raw path string
     * @return whether [path] is absolute (Unix or Windows style, with either separator)
     */
    fun isAbsolute(path: String): Boolean = WINDOWS_DRIVE_REGEX.containsMatchIn(path) || path.toPath().isAbsolute
}
