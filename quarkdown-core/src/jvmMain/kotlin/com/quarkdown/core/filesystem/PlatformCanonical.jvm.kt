package com.quarkdown.core.filesystem

import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toOkioPath

internal actual fun canonicalizeOnPlatform(
    path: Path,
    backend: FileSystem,
): Path? {
    if (backend !== FileSystem.SYSTEM) return null
    val nioPath = path.toFile().toPath()
    val real =
        try {
            nioPath.toRealPath()
        } catch (_: IOException) {
            nioPath.toAbsolutePath().normalize()
        }
    return real.toFile().toOkioPath()
}
