package com.quarkdown.pm.layout

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.packages.PackageReferenceSyntax
import com.quarkdown.pm.model.PackageIdentifier
import com.quarkdown.pm.model.PackageManifest

/**
 * Where packages live inside a project: `<project>/@<scope>/<name>/`.
 */
object PackageLayout {
    /**
     * @param projectRoot directory of the main document
     * @param identifier the package
     * @return the directory the package is installed in, whether it exists yet
     */
    fun directoryOf(
        projectRoot: FsEntry,
        identifier: PackageIdentifier,
    ): FsEntry = projectRoot.resolve("${PackageReferenceSyntax.PREFIX}${identifier.scope}").resolve(identifier.name)

    /**
     * @param directory an installed package's directory
     * @return the manifest file of that package
     */
    fun manifestOf(directory: FsEntry): FsEntry = directory.resolve(PackageManifest.FILE_NAME)

    /**
     * Tells whether [entry] belongs to an installed package, i.e. lies under a top-level `@<scope>/` directory of [projectRoot].
     * @param projectRoot directory of the main document
     * @param entry file or directory to classify
     * @return whether [entry] is inside a package directory
     */
    fun isPackagePath(
        projectRoot: FsEntry,
        entry: FsEntry,
    ): Boolean {
        val segments = entry.relativeTo(projectRoot)?.invariantSeparatorsPath?.split('/') ?: return false
        return segments.size >= 2 && segments.first().startsWith(PackageReferenceSyntax.PREFIX)
    }
}
