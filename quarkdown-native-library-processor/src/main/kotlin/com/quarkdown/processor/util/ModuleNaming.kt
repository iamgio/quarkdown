package com.quarkdown.processor.util

/**
 * Central home for the naming conventions the processor applies to module sources and their
 * generated wrappers. Every rule is a one-liner but they used to live spread across the
 * describer and the code generator; collecting them here means a single edit changes the
 * convention everywhere.
 */
internal object ModuleNaming {
    /** File extension of Kotlin sources, without the leading dot. */
    const val KOTLIN_EXTENSION: String = "kt"

    private const val KOTLIN_EXTENSION_WITH_DOT: String = ".$KOTLIN_EXTENSION"
    private const val MODULE_SUFFIX: String = "Module"

    /** Strips the `.kt` extension off a source file name to derive the module's simple name. */
    fun moduleNameOf(fileName: String): String = fileName.removeSuffix(KOTLIN_EXTENSION_WITH_DOT)

    /** Appends the `Module` suffix used for every generated wrapper file. */
    fun generatedFileNameOf(moduleName: String): String = "$moduleName$MODULE_SUFFIX"
}
