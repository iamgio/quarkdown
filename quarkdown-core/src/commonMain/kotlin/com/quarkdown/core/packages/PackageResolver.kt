package com.quarkdown.core.packages

import com.quarkdown.core.filesystem.FsEntry

/**
 * Host-provided strategy that turns a package reference, in the form of `@scope/package:version`, into files on the
 * context's file system.
 */
interface PackageResolver {
    /**
     * Looks the package up locally, without side effects.
     * @param reference the reference as written in the document
     * @param projectRoot directory of the main document, under which packages are installed
     * @return the package if the exact referenced version is available locally, `null` otherwise
     * @throws PackageResolutionException if the reference is malformed or the local installation is corrupted
     */
    fun locate(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage?

    /**
     * Makes the package available locally, replacing a previously installed different version.
     * May perform network access and write files.
     * @param reference the reference as written in the document
     * @param projectRoot directory of the main document, under which packages are installed
     * @return the installed package
     * @throws PackageResolutionException if the package cannot be retrieved or written
     */
    fun install(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage

    /**
     * Resolver for hosts that do not support packages. Both operations throw [UnsupportedPackageResolutionException].
     */
    object Unsupported : PackageResolver {
        override fun locate(
            reference: String,
            projectRoot: FsEntry,
        ): ResolvedPackage = throw UnsupportedPackageResolutionException()

        override fun install(
            reference: String,
            projectRoot: FsEntry,
        ): ResolvedPackage = throw UnsupportedPackageResolutionException()
    }
}
