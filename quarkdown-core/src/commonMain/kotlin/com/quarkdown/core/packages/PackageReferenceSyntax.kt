package com.quarkdown.core.packages

/**
 * Syntax marker of package references accepted by include-like functions, in the form of `@scope/package:version`.
 */
object PackageReferenceSyntax {
    /**
     * Character that opens a package reference and distinguishes it from a file path.
     */
    const val PREFIX = '@'

    /**
     * @param path path or reference as written in the document
     * @return whether [path] is meant as a package reference, regardless of its validity
     */
    fun isReference(path: String): Boolean = path.startsWith(PREFIX)
}
