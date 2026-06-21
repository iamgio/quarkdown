package com.quarkdown.cli.util

import com.quarkdown.core.util.IOUtils
import java.io.File

/**
 * Reason why cleaning a directory is refused for safety.
 * Combined with `--clean`, accidental targeting of a sensitive directory could result
 * in irreversible data loss, so the CLI refuses any of these cases up front.
 */
sealed interface CleanRefusal {
    /**
     * Human-readable explanation of why the directory cannot be cleaned.
     */
    val message: String

    /**
     * The directory is a filesystem root (e.g. `/` on Unix, `C:\` on Windows).
     */
    data object FilesystemRoot : CleanRefusal {
        override val message = "it is a filesystem root"
    }

    /**
     * The directory is the user's home directory.
     */
    data object HomeDirectory : CleanRefusal {
        override val message = "it is the user home directory"
    }

    /**
     * The directory is the current working directory the CLI was invoked from.
     */
    data object WorkingDirectory : CleanRefusal {
        override val message = "it is the current working directory"
    }

    /**
     * The directory looks like the root of a source-controlled project (it contains a `.git` entry).
     */
    data object RepositoryRoot : CleanRefusal {
        override val message = "it looks like a repository"
    }

    /**
     * The directory (recursively) contains the main source file of the compilation.
     * @param source the source file whose deletion would be triggered by cleaning the directory
     */
    data class ContainsSourceFile(
        val source: File,
    ) : CleanRefusal {
        override val message = "it contains the source file '${source.absolutePath}'"
    }
}

/**
 * Markers identifying that a directory is the root of a versioned project.
 * The presence of these inside the candidate directory makes it a [CleanRefusal.RepositoryRoot].
 */
private val PROJECT_ROOT_MARKERS = setOf(".git", ".hg", ".svn")

/**
 * Decides whether [this] directory can be safely wiped by the CLI `--clean` flag.
 *
 * Cleaning is refused when the target directory is a sensitive location (filesystem root,
 * user home, the current working directory, or a project root), or when wiping it would
 * delete the main [sourceFile] of the compilation.
 *
 * @param sourceFile main source file of the compilation, if known
 * @param homeDirectory user home directory, exposed as a parameter to ease testing
 * @param workingDirectory current working directory, exposed as a parameter to ease testing
 * @return `null` if the directory can be cleaned, or a [CleanRefusal] describing why it cannot
 */
fun File.checkCleanSafety(
    sourceFile: File?,
    homeDirectory: File? = systemDirectory("user.home"),
    workingDirectory: File? = systemDirectory("user.dir"),
): CleanRefusal? {
    val target = this.canonicalFile

    if (target.parentFile == null) return CleanRefusal.FilesystemRoot
    if (homeDirectory != null && target == homeDirectory) return CleanRefusal.HomeDirectory
    if (workingDirectory != null && target == workingDirectory) return CleanRefusal.WorkingDirectory
    if (PROJECT_ROOT_MARKERS.any { File(target, it).exists() }) return CleanRefusal.RepositoryRoot

    if (sourceFile != null && IOUtils.isSubPath(parent = target, child = sourceFile)) {
        return CleanRefusal.ContainsSourceFile(sourceFile.canonicalFile)
    }

    return null
}

/**
 * @return the canonical directory referenced by the given system [property], or `null` if it cannot be resolved
 */
private fun systemDirectory(property: String): File? =
    runCatching {
        System.getProperty(property)?.let { File(it).canonicalFile }
    }.getOrNull()
