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
        resolving(reference) { parsed ->
            installedPackage(projectRoot, parsed).resolve(parsed)
        }

    override fun install(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage =
        resolving(reference) { parsed ->
            val scope = parsed.identifier.scope
            val repository =
                repositories.forScope(scope)
                    ?: throw PackageResolutionException(reference, "no package repository is registered for scope '$scope'")

            Log.info("Installing $reference...")

            val pkg =
                repository.getPackage(parsed)
                    ?: throw PackageResolutionException(reference, "not found in repository '$scope'")

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

    /**
     * Parses [reference] and runs [action] on it, translating any failure raised along the way
     * (malformed reference, network, manifest, writer) into a [PackageResolutionException]
     * that names the reference and keeps the original cause.
     */
    private inline fun <T> resolving(
        reference: String,
        action: (PackageReference) -> T,
    ): T =
        try {
            action(PackageReference.parse(reference))
        } catch (e: PackageResolutionException) {
            throw e
        } catch (e: Exception) {
            throw PackageResolutionException(reference, e.message ?: e::class.simpleName.orEmpty(), e)
        }

    companion object
}
