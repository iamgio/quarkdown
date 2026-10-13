package com.quarkdown.pm.resolve

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.core.packages.ResolvedPackage
import com.quarkdown.pm.layout.PackageLayout
import com.quarkdown.pm.manifest.ManifestParseException
import com.quarkdown.pm.manifest.ManifestParser
import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageReference

/**
 * A package directory inside a project, which may or may not currently hold an installed package.
 * @param directory the `@<scope>/<name>/` directory
 * @param manifestParser parser for the installed manifest
 */
internal class InstalledPackage(
    val directory: FsEntry,
    private val manifestParser: ManifestParser,
) {
    private val manifestFile: FsEntry
        get() = PackageLayout.manifestOf(directory)

    /**
     * @param reference the version being looked up
     * @return the package, or `null` if nothing is installed here or the installed version differs
     * @throws PackageResolutionException if the installed manifest is malformed or its entry point is missing
     */
    fun resolve(reference: PackageReference): ResolvedPackage? {
        if (!manifestFile.exists) return null
        val atom = readManifest(reference)
        if (atom.version != reference.version) return null

        val entrypoint = directory.resolve(atom.entrypoint)
        if (!entrypoint.exists) {
            throw PackageResolutionException(
                reference.toString(),
                "entry point '${atom.entrypoint}' is missing from the installed package at ${directory.fullPath}",
            )
        }
        return ResolvedPackage(directory, entrypoint)
    }

    private fun readManifest(reference: PackageReference): AtomManifest =
        try {
            manifestParser.parse(manifestFile.readText()).atom
        } catch (e: ManifestParseException) {
            throw PackageResolutionException(
                reference.toString(),
                "the installed manifest at ${manifestFile.fullPath} is malformed (${e.message}). " +
                    "Delete the package directory to reinstall it.",
                e,
            )
        }
}
