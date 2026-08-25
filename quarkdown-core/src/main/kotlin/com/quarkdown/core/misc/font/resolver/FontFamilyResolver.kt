package com.quarkdown.core.misc.font.resolver

import com.quarkdown.core.filesystem.FileSystem
import com.quarkdown.core.misc.font.FontFamily

/**
 * Resolver of a [FontFamily] by its name or path, from system fonts or media.
 */
interface FontFamilyResolver {
    /**
     * Resolves a [FontFamily] by its name or path.
     * @param nameOrPath the name of the system font or the path/URL to the font file
     * @param fileSystem the file system to resolve relative font file paths from
     * @return a new [FontFamily] if found
     */
    fun resolve(
        nameOrPath: String,
        fileSystem: FileSystem,
    ): FontFamily?

    companion object {
        /**
         * Default [FontFamilyResolver] implementation.
         */
        val SYSTEM: FontFamilyResolver = JVMFontFamilyResolver
    }
}
