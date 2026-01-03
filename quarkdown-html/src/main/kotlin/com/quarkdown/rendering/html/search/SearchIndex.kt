package com.quarkdown.rendering.html.search

import kotlinx.serialization.Serializable

/**
 * A search index for client-side documentation search.
 * This structure is serialized to JSON and loaded by the browser
 * to provide search functionality without a server.
 * @param entries the searchable documents in the index
 */
@Serializable
data class SearchIndex(
    val entries: List<SearchEntry>,
)

/**
 * A searchable document entry in the search index.
 * @param url relative URL to the document
 * @param title document title, used as primary search field and displayed in results
 * @param description brief summary of the document content
 * @param keywords additional terms to improve search relevance
 * @param headings section headings within the document, allowing navigation to specific sections
 */
@Serializable
data class SearchEntry(
    val url: String,
    val title: String?,
    val description: String?,
    val keywords: List<String>,
    val headings: List<SearchHeading>,
)

/**
 * A heading within a document, enabling search results to link directly to sections.
 * @param anchor the HTML anchor ID for the heading (used in URL fragment)
 * @param text the heading text content
 * @param level the heading level (1-6, corresponding to h1-h6)
 */
@Serializable
data class SearchHeading(
    val anchor: String,
    val text: String,
    val level: Int,
)
