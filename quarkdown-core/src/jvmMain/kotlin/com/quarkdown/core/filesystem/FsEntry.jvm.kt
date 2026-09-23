package com.quarkdown.core.filesystem

import java.io.File
import okio.FileSystem as OkioBackend

/**
 * Converts this entry to a [File], if it is backed by the physical disk file system.
 * This is the boundary between the platform-neutral input abstraction and JVM-only
 * output or process-level code; input-side code must not use it.
 * @return the corresponding [File], or `null` if this entry is virtual
 */
fun FsEntry.toFileOrNull(): File? = if (backend === OkioBackend.SYSTEM) path.toFile() else null
