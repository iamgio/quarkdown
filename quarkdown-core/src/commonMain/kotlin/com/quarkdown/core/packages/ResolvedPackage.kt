package com.quarkdown.core.packages

import com.quarkdown.core.filesystem.FsEntry

/**
 * A package made available on the context's file system by a [PackageResolver].
 * @param root directory holding the package files
 * @param entrypoint the Quarkdown file to include, located inside [root]
 */
class ResolvedPackage(
    val root: FsEntry,
    val entrypoint: FsEntry,
)
