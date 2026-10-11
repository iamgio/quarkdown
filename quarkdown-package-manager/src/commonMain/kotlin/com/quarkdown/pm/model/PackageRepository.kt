package com.quarkdown.pm.model

/**
 * A source of packages, such as a remote Git repository.
 */
interface PackageRepository {
    /**
     * Looks up the exact version of a package a [reference] points to.
     * @param reference versioned reference to resolve
     * @return the package, or `null` if the package or the version does not exist in this repository
     * @throws com.quarkdown.pm.net.RemoteFetchException if the repository cannot be reached
     * @throws com.quarkdown.pm.manifest.ManifestParseException if the package's manifest is malformed
     */
    fun getPackage(reference: PackageReference): RepositoryPackage?
}

/**
 * A file belonging to a package.
 * @param path path of the file relative to the package root, with `/` separators, e.g. `assets/logo.png`
 * @param content loader of the file's bytes. Invoking it may perform network access
 */
data class PackageFile(
    val path: String,
    val content: () -> ByteArray,
)

/**
 * A package resolved from a [PackageRepository].
 * @param manifest parsed manifest of the package
 * @param files every file of the package, manifest included, with lazily loaded content
 */
data class RepositoryPackage(
    val manifest: PackageManifest,
    val files: List<PackageFile>,
)
