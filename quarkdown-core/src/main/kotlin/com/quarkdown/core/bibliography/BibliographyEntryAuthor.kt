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

private val AUTHOR_SEPARATOR = ",? and ".toRegex()
private const val NAME_SEPARATOR_LAST_NAME_FIRST = ", "
private const val NAME_SEPARATOR_FIRST_NAME_FIRST = " "

/**
 * Given a [BibliographyEntry] with the [author] property in the format "Last, First and Last, First",
 * generates structured author information.
 * @return a list of structured authors, based on the [author] property.
 */
val BibliographyEntry.structuredAuthors: List<BibliographyEntryAuthor>
    get() {
        val rawAuthors =
            author
                ?.split(AUTHOR_SEPARATOR)
                ?.map { it.trim() }
                ?: return emptyList()

        return rawAuthors.map { name ->
            // Last name is first: "Last, First"
            // First name is first: "First Last"
            val lastNameFirst = ", " in name
            val parts =
                name
                    .split(if (lastNameFirst) NAME_SEPARATOR_LAST_NAME_FIRST else NAME_SEPARATOR_FIRST_NAME_FIRST)
                    .map { it.trim() }

            when {
                lastNameFirst ->
                    BibliographyEntryAuthor(
                        fullName = name,
                        firstName = parts.lastOrNull().takeIf { parts.size > 1 },
                        lastName = parts.firstOrNull(),
                    )
                else ->
                    BibliographyEntryAuthor(
                        fullName = name,
                        firstName = parts.firstOrNull().takeIf { parts.size > 1 },
                        lastName = parts.lastOrNull(),
                    )
            }
        }
    }
