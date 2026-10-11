package com.quarkdown.pm.repo.github

/**
 * Read-only directory queries over the files of a [GitHubTree].
 * @param tree complete listing of the repository
 * @throws IllegalStateException if the listing is truncated, since packages could silently go missing
 */
class GitHubTreeIndex(
    tree: GitHubTree,
) {
    init {
        check(!tree.truncated) { "The repository listing was truncated by GitHub; packages cannot be resolved reliably." }
    }

    private val filePaths: List<String> = tree.tree.filter { it.isFile }.map { it.path }

    /**
     * @param directory path of a directory relative to the repository root, without trailing slash
     * @return paths of all files located under [directory], at any depth, in listing order
     */
    fun filesUnder(directory: String): List<String> {
        val prefix = "$directory/"
        return filePaths.filter { it.startsWith(prefix) }
    }

    /**
     * @param directory path of a directory relative to the repository root, without trailing slash
     * @return names of the directories that are direct children of [directory]
     */
    fun childDirectoriesOf(directory: String): Set<String> =
        filesUnder(directory)
            .map { it.removePrefix("$directory/") }
            .filter { '/' in it }
            .map { it.substringBefore('/') }
            .toSet()
}
