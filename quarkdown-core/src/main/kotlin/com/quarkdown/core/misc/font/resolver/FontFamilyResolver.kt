package com.quarkdown.core.misc.font.resolver

import com.quarkdown.core.misc.font.FontFamily
import java.io.File

/**
 * Resolver of a [FontFamily] by its name or path, from system fonts or media.
 */
interface FontFamilyResolver {
    /**
     * Resolves a [FontFamily] by its name or path.
     * @param nameOrPath the name of the system font or the path/URL to the font file
     * @param workingDirectory the working directory to resolve relative paths
     * @return a new [FontFamily] if found
     */
    fun resolve(
        nameOrPath: String,
        workingDirectory: File?,
    ): FontFamily?

    companion object {
        /**
         * Default [FontFamilyResolver] implementation.
         */
        val SYSTEM: FontFamilyResolver = JVMFontFamilyResolver
    }
}
