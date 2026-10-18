package com.quarkdown.pm.repo

import com.quarkdown.pm.manifest.ManifestParser
import com.quarkdown.pm.manifest.TomlManifestParser
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageIdentifier
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.model.PackageRepository
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.model.latestVersion
import com.quarkdown.pm.net.DefaultRemoteFetcher
import com.quarkdown.pm.net.RemoteFetchException
import com.quarkdown.pm.net.RemoteFetcher
import com.quarkdown.pm.repo.github.GitHubRepositoryCoordinates
import com.quarkdown.pm.repo.github.GitHubTree
import com.quarkdown.pm.repo.github.GitHubTreeIndex

/**
 * Coordinates of the official package repository, `quarkdown-labs/atoms`.
 */
val ATOMS_COORDINATES = GitHubRepositoryCoordinates(owner = "quarkdown-labs", repository = "atoms")

/**
 * [PackageRepository] backed by a GitHub repository laid out as `<name>/<version>/<files>`,
 * where every version directory is a package root and therefore holds its [PackageManifest.FILE_NAME].
 * Files under a name starting with [PackageFile.INTERNAL_PREFIX] (e.g. `__tests`) are repository-internal and skipped.
 *
 * Each package directory is listed through the Git Trees API once per instance, on first use, so the cost grows
 * with the packages a document uses rather than with the whole repository; files are then read from raw GitHub URLs.
 * [listPackages] instead lists the whole repository in one request, since a catalog needs every package anyway,
 * and reads one manifest per package: that of its latest version.
 *
 * @param coordinates repository to read from
 * @param fetcher strategy for network access, replaceable on platforms without synchronous networking
 * @param manifestParser parser for `atom.toml`
 */
class AtomsPackageRepository(
    private val coordinates: GitHubRepositoryCoordinates = ATOMS_COORDINATES,
    private val fetcher: RemoteFetcher = DefaultRemoteFetcher,
    private val manifestParser: ManifestParser = TomlManifestParser,
) : PackageRepository {
    private val indexByPackage = mutableMapOf<String, GitHubTreeIndex>()

    override fun getPackage(reference: PackageReference): RepositoryPackage? {
        val name = reference.identifier.name
        val version = reference.version
        val versionPaths = index(name).filesUnder(version)

        if (manifestPath(version) !in versionPaths) return null

        val manifest = readManifest(name, version)
        val files =
            versionPaths
                .filterNot(PackageFile::isInternal)
                .map { versionPath -> packageFile(name, versionPath, relativeTo = version) }

        return RepositoryPackage(manifest, files)
    }

    /**
     * @param identifier the package to inspect
     * @return every version published for the package, or an empty set if the package does not exist
     */
    fun versionsOf(identifier: PackageIdentifier): Set<String> = index(identifier.name).childDirectoriesOf("")

    override fun listPackages(): Set<PackageManifest> {
        val index = GitHubTreeIndex(GitHubTree.decode(fetcher.fetchText(coordinates.treeUrl())))
        val files = index.filesUnder("").toSet()
        return index
            .childDirectoriesOf("")
            .filterNot { name -> name.startsWith(PackageFile.INTERNAL_PREFIX) }
            .mapNotNull { name ->
                val published = index.childDirectoriesOf(name).filter { version -> repositoryPath(name, manifestPath(version)) in files }
                latestVersion(published)?.let { version -> readManifest(name, version) }
            }.toSet()
    }

    /** @return the manifest of [version] of the package [name], relative to the package directory */
    private fun manifestPath(version: String): String = "$version/${PackageManifest.FILE_NAME}"

    /** @return [packagePath], relative to the directory of the package [name], as a path in the repository */
    private fun repositoryPath(
        name: String,
        packagePath: String,
    ): String = "$name/$packagePath"

    private fun readManifest(
        name: String,
        version: String,
    ): PackageManifest = manifestParser.parse(fetcher.fetchText(coordinates.rawFileUrl(repositoryPath(name, manifestPath(version)))))

    /**
     * Lists the package's directory on first use. A directory GitHub does not know yields an empty listing.
     */
    private fun index(name: String): GitHubTreeIndex =
        indexByPackage.getOrPut(name) {
            val tree =
                try {
                    GitHubTree.decode(fetcher.fetchText(coordinates.subtreeUrl(name)))
                } catch (_: RemoteFetchException.NotFound) {
                    GitHubTree(sha = "", tree = emptyList())
                }
            GitHubTreeIndex(tree)
        }

    private fun packageFile(
        name: String,
        versionPath: String,
        relativeTo: String,
    ): PackageFile {
        val content by lazy { fetcher.fetchBytes(coordinates.rawFileUrl(repositoryPath(name, versionPath))) }
        return PackageFile(path = versionPath.removePrefix("$relativeTo/")) { content }
    }
}
