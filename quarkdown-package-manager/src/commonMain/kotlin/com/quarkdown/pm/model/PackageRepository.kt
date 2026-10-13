package com.quarkdown.pm.model

import com.quarkdown.pm.model.PackageFile.Companion.INTERNAL_PREFIX

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
 * @throws IllegalArgumentException if [path] is empty, absolute, uses backslashes or contains a `..` segment
 */
data class PackageFile(
    val path: String,
    val content: () -> ByteArray,
) {
    init {
        require(path.isNotEmpty() && !path.startsWith('/') && '\\' !in path && !path.matches(DRIVE_PREFIX)) {
            "Invalid package file path '$path': must be relative to the package root and use '/' as separator."
        }
        require(path.split('/').none { it == ".." }) {
            "Invalid package file path '$path': must not contain '..'."
        }
    }

    companion object {
        private val DRIVE_PREFIX = Regex("""^[A-Za-z]:.*""")

        /**
         * Prefix of file and directory names that are internal to the repository (tests, scratch material)
         * and not part of the package.
         */
        const val INTERNAL_PREFIX = "__"

        /**
         * @param path path relative to the package root, with `/` separators
         * @return whether any segment of [path] starts with [INTERNAL_PREFIX], making the file internal and
         *         excluded from installation
         */
        fun isInternal(path: String): Boolean = path.split('/').any { it.startsWith(INTERNAL_PREFIX) }
    }
}

/**
 * A package resolved from a [PackageRepository].
 * @param manifest parsed manifest of the package
 * @param files every file of the package, manifest included, with lazily loaded content
 */
data class RepositoryPackage(
    val manifest: PackageManifest,
    val files: List<PackageFile>,
)
