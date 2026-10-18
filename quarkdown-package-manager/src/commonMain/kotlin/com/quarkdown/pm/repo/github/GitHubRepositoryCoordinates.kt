package com.quarkdown.pm.repo.github

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments

/**
 * Location of a GitHub repository at a given ref, and the URLs needed to read parts of it anonymously.
 * Path components are percent-encoded, so file names with spaces or reserved characters resolve correctly.
 * @param owner user or organization that owns the repository
 * @param repository repository name
 * @param ref branch, tag or commit to read from
 */
data class GitHubRepositoryCoordinates(
    val owner: String,
    val repository: String,
    val ref: String = DEFAULT_REF,
) {
    /**
     * @return URL of the Git Trees API listing every file of the repository at [ref]
     */
    fun treeUrl(): Url =
        URLBuilder(API_BASE)
            .appendPathSegments("repos", owner, repository, "git", "trees", ref)
            .apply { parameters.append("recursive", "1") }
            .build()

    /**
     * @param path path of a directory within the repository, with `/` separators
     * @return URL of the Git Trees API listing every file under that directory at [ref],
     *         answering 404 when the directory does not exist
     */
    fun subtreeUrl(path: String): Url =
        URLBuilder(API_BASE)
            .appendPathSegments("repos", owner, repository, "git", "trees", "$ref:$path")
            .apply { parameters.append("recursive", "1") }
            .build()

    /**
     * @param path path of a file within the repository, with `/` separators
     * @return URL serving the raw content of the file at [ref]
     */
    fun rawFileUrl(path: String): Url =
        URLBuilder(RAW_BASE)
            .appendPathSegments(owner, repository, ref)
            .appendPathSegments(path.split('/'))
            .build()

    companion object {
        /**
         * Ref used when none is specified.
         */
        const val DEFAULT_REF = "main"
        private const val API_BASE = "https://api.github.com"
        private const val RAW_BASE = "https://raw.githubusercontent.com"
    }
}
