package com.quarkdown.pm.repo

import com.quarkdown.pm.manifest.ManifestParser
import com.quarkdown.pm.manifest.TomlManifestParser
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageIdentifier
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.model.PackageRepository
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.net.DefaultRemoteFetcher
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
 *
 * The repository listing is fetched through the Git Trees API once per instance, on first use,
 * and files are then read from raw GitHub URLs.
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
    private val index: GitHubTreeIndex by lazy {
        GitHubTreeIndex(GitHubTree.decode(fetcher.fetchText(coordinates.treeUrl)))
    }

    override fun getPackage(reference: PackageReference): RepositoryPackage? {
        val directory = "${reference.identifier.name}/${reference.version}"
        val treePaths = index.filesUnder(directory)

        val manifestPath = "$directory/${PackageManifest.FILE_NAME}"
        if (manifestPath !in treePaths) return null

        val manifest = manifestParser.parse(fetcher.fetchText(coordinates.rawFileUrl(manifestPath)))
        val files = treePaths.map { treePath -> packageFile(treePath, relativeTo = directory) }

        return RepositoryPackage(manifest, files)
    }

    /**
     * @param identifier the package to inspect
     * @return every version published for the package, or an empty set if the package does not exist
     */
    fun versionsOf(identifier: PackageIdentifier): Set<String> = index.childDirectoriesOf(identifier.name)

    private fun packageFile(
        treePath: String,
        relativeTo: String,
    ): PackageFile {
        val content by lazy { fetcher.fetchBytes(coordinates.rawFileUrl(treePath)) }
        return PackageFile(path = treePath.removePrefix("$relativeTo/")) { content }
    }
}
