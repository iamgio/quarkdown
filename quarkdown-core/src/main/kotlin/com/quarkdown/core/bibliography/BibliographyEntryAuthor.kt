package com.quarkdown.core.bibliography

/**
 * A structured author of a [BibliographyEntry].
 * @param fullName the full name of the author
 * @param firstName the first name of the author
 * @param lastName the last name of the author
 */
data class BibliographyEntryAuthor(
    val fullName: String?,
    val firstName: String?,
    val lastName: String?,
)

private const val AUTHOR_SEPARATOR = " and "
private const val NAME_SEPARATOR = ", "

/**
 * Given a [BibliographyEntry] with the [author] property in the format "Last, First and Last, First",
 * generates structured author information.
 * @return a list of structured authors, based on the [author] property.
 */
val BibliographyEntry.structuredAuthors: List<BibliographyEntryAuthor>
    get() =
        author
            ?.split(AUTHOR_SEPARATOR)
            ?.map { it.trim() }
            ?.map { name ->
                val parts = name.split(NAME_SEPARATOR).map { it.trim() }
                BibliographyEntryAuthor(
                    fullName = name,
                    firstName = parts.lastOrNull().takeIf { parts.size > 1 },
                    lastName = parts.firstOrNull(),
                )
            } ?: emptyList()
