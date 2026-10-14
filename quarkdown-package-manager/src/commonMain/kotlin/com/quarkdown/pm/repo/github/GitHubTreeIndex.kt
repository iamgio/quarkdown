package com.quarkdown.pm.repo.github

/**
 * Read-only directory queries over the files of a [GitHubTree], whose paths are relative to the listed root.
 * @param tree complete listing of a repository or of one of its directories
 * @throws IllegalStateException if the listing is truncated, since packages could silently go missing
 */
class GitHubTreeIndex(
    tree: GitHubTree,
) {
    init {
        check(!tree.truncated) { "The listing was truncated by GitHub; packages cannot be resolved reliably." }
    }

    private val filePaths: List<String> = tree.tree.filter { it.isFile }.map { it.path }

    /**
     * @param directory path of a directory relative to the listing root, without trailing slash;
     *                  empty for the root itself
     * @return paths of all files located under [directory], at any depth, in listing order
     */
    fun filesUnder(directory: String): List<String> {
        val prefix = prefixOf(directory)
        return filePaths.filter { it.startsWith(prefix) }
    }

    /**
     * @param directory path of a directory relative to the listing root, without trailing slash;
     *                  empty for the root itself
     * @return names of the directories that are direct children of [directory]
     */
    fun childDirectoriesOf(directory: String): Set<String> =
        filesUnder(directory)
            .map { it.removePrefix(prefixOf(directory)) }
            .filter { '/' in it }
            .map { it.substringBefore('/') }
            .toSet()

    private fun prefixOf(directory: String): String = if (directory.isEmpty()) "" else "$directory/"
}
