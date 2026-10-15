package com.quarkdown.pm.resolve

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.log.Log
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.core.packages.PackageResolver
import com.quarkdown.core.packages.ResolvedPackage
import com.quarkdown.pm.install.PackageWriter
import com.quarkdown.pm.layout.PackageLayout
import com.quarkdown.pm.manifest.ManifestParser
import com.quarkdown.pm.manifest.TomlManifestParser
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.repo.PackageRepositories

/**
 * [PackageResolver] that keeps packages inside the project at `@<scope>/<name>/`.
 * @param repositories repositories by scope
 * @param writer strategy that materializes downloaded files
 * @param manifestParser parser for installed manifests
 * @see PackageLayout
 */
class LocalPackageResolver(
    private val repositories: PackageRepositories = PackageRepositories.default(),
    private val writer: PackageWriter,
    private val manifestParser: ManifestParser = TomlManifestParser,
) : PackageResolver {
    override fun locate(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage? =
        resolvingPackage(reference) { parsed ->
            installedPackage(projectRoot, parsed).resolve(parsed)
        }

    override fun install(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage =
        resolvingPackage(reference) { parsed ->
            Log.info("Installing $reference...")

            val pkg = repositories.fetch(parsed)

            val installed = installedPackage(projectRoot, parsed)
            writer.write(pkg, installed.directory)

            Log.info("Installed $reference (${pkg.files.size} files)")

            installed.resolve(parsed)
                ?: throw PackageResolutionException(
                    reference,
                    "the downloaded manifest declares version '${pkg.manifest.atom.version}', which does not match the reference",
                )
        }

    private fun installedPackage(
        projectRoot: FsEntry,
        reference: PackageReference,
    ) = InstalledPackage(PackageLayout.directoryOf(projectRoot, reference.identifier), manifestParser)

    companion object
}
