package com.quarkdown.pm.repo.github

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Response of the GitHub Git Trees API.
 * @param sha commit or tree SHA the listing refers to
 * @param tree entries of the listing
 * @param truncated whether GitHub cut the listing short because the repository is too large
 */
@Serializable
data class GitHubTree(
    val sha: String,
    val tree: List<GitHubTreeEntry>,
    val truncated: Boolean = false,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * @param source JSON body returned by the Git Trees API
         * @return the decoded tree
         * @throws kotlinx.serialization.SerializationException if the body is not a tree response
         */
        fun decode(source: String): GitHubTree = json.decodeFromString(serializer(), source)
    }
}

/**
 * A single entry of a [GitHubTree].
 * @param path path of the entry relative to the repository root
 * @param type `blob` for files, `tree` for directories, `commit` for submodules
 * @param size size in bytes, present for blobs only
 */
@Serializable
data class GitHubTreeEntry(
    val path: String,
    val type: String,
    val size: Long? = null,
) {
    /**
     * Whether this entry is a regular file.
     */
    val isFile: Boolean
        get() = type == BLOB

    private companion object {
        const val BLOB = "blob"
    }
}
