package eu.iamgio.quarkdown.document

/**
 * An author of a document.
 * @param name author's name
 * @param info additional information about the author (e.g. email, website)
 */
data class DocumentAuthor(
    val name: String,
    val info: Map<String, String> = emptyMap(),
)
