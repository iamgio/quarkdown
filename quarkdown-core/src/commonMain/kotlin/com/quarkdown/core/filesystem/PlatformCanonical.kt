package com.quarkdown.core.filesystem

import okio.FileSystem
import okio.Path

/**
 * Resolves [path] through the platform's real-path lookup, if [backend] is the platform's disk.
 * @return the real path, or `null` if the platform or the backend cannot resolve it
 */
internal expect fun canonicalizeOnPlatform(
    path: Path,
    backend: FileSystem,
): Path?
