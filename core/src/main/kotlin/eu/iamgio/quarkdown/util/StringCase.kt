package eu.iamgio.quarkdown.util

/**
 * A general purpose strategy to transform a string to a specific case type.
 */
sealed interface StringCase {
    /**
     * Transforms a [string] according to the case.
     * @return transformed string
     */
    fun transform(string: String): String

    /**
     * Uppercase. `Hello` -> `HELLO`
     */
    data object Upper : StringCase {
        override fun transform(string: String) = string.uppercase()
    }

    /**
     * Lowercase. `Hello` -> `hello`
     */
    data object Lower : StringCase {
        override fun transform(string: String) = string.lowercase()
    }

    /**
     * Capitalize. `hello` -> `Hello`
     */
    data object Capitalize : StringCase {
        override fun transform(string: String) = string.replaceFirstChar(Char::titlecase)
    }
}

/**
 * Transforms [this] string to a specific case.
 * @return the transformed string
 */
fun String.case(case: StringCase) = case.transform(this)
