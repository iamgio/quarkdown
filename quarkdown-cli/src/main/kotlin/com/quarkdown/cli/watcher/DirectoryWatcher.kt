package com.quarkdown.cli.watcher

import io.methvin.watcher.DirectoryChangeEvent
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.extension

private typealias JDirectoryWatcher = io.methvin.watcher.DirectoryWatcher

/**
 * A [io.methvin.watcher.DirectoryWatcher] wrapper that recursively watches a directory for changes.
 * @param watcher [io.methvin.watcher.DirectoryWatcher] instance
 */
class DirectoryWatcher(
    private val watcher: JDirectoryWatcher,
) {
    /**
     * Synchronously starts watching for changes in the directory.
     */
    fun watchBlocking() {
        watcher.watch()
    }

    /**
     * Asynchronously starts watching for changes in the directory.
     */
    fun watch() {
        thread(start = true) {
            watchBlocking()
        }
    }

    /**
     * Stops watching for changes in the directory.
     */
    fun stop() {
        watcher.close()
    }

    companion object {
        /**
         * Creates a new [DirectoryWatcher].
         * @param directory directory to watch
         * @param excludeFiles files or directories to exclude from watching
         * @param exclude general function to exclude files or directories from watching, for example temporary IDE files
         * @param onChange function to call when a change is detected
         */
        private fun create(
            directory: File,
            excludeFiles: List<File> = emptyList(),
            exclude: (Path) -> Boolean = { it.extension.endsWith("~") },
            onChange: (DirectoryChangeEvent) -> Unit,
        ) = JDirectoryWatcher
            .builder()
            .path(directory.toPath())
            .listener {
                val acceptByPath = !exclude(it.path())
                val acceptByFiles = excludeFiles.none { file -> it.path().startsWith(file.absolutePath) }

                if (acceptByPath && acceptByFiles) {
                    onChange(it)
                }
            }.build()
            .let(::DirectoryWatcher)

        /**
         * Creates a new [DirectoryWatcher].
         * @param directory directory to watch
         * @param exclude file or directory to exclude from watching. If `null`, no files are excluded
         * @param onChange function to call when a change is detected
         */
        fun create(
            directory: File,
            exclude: File?,
            onChange: (DirectoryChangeEvent) -> Unit,
        ) = create(
            directory,
            excludeFiles = exclude?.let(::listOf) ?: emptyList(),
            onChange = onChange,
        )
    }
}
