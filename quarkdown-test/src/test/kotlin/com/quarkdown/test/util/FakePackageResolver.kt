package com.quarkdown.test.util

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.core.packages.PackageResolver
import com.quarkdown.core.packages.ResolvedPackage

/**
 * In-memory [PackageResolver] for integration tests.
 * Packages are installed into [fs] under `@<scope>/<name>/`, with `main.qd` as the entry point.
 * @param fs the virtual file system the pipeline runs on
 * @param available installable packages: reference to (relative file path to content)
 */
class FakePackageResolver(
    private val fs: VirtualFileSystem,
    private val available: Map<String, Map<String, String>> = emptyMap(),
) : PackageResolver {
    /**
     * References passed to [locate], in order.
     */
    val located = mutableListOf<String>()

    /**
     * References passed to [install], in order.
     */
    val installed = mutableListOf<String>()

    private fun directoryOf(
        reference: String,
        projectRoot: FsEntry,
    ): FsEntry = projectRoot.resolve("@" + reference.removePrefix("@").substringBefore(':'))

    private fun resolved(directory: FsEntry): ResolvedPackage? =
        directory.resolve("main.qd").takeIf { it.exists }?.let { ResolvedPackage(directory, it) }

    override fun locate(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage? {
        located += reference
        return resolved(directoryOf(reference, projectRoot))
    }

    override fun install(
        reference: String,
        projectRoot: FsEntry,
    ): ResolvedPackage {
        installed += reference
        val files = available[reference] ?: throw PackageResolutionException(reference, "not found")
        val directory = directoryOf(reference, projectRoot)
        files.forEach { (path, content) -> fs.write(directory.resolve(path).fullPath, content) }
        return resolved(directory) ?: throw PackageResolutionException(reference, "no main.qd")
    }
}
