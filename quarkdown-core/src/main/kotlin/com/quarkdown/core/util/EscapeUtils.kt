package com.quarkdown.core.util

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents a target (commonly a language or format) that strings can be escaped for.
 *
 * For instance:
 * - in HTML `<` becomes `&lt;`
 * - in JavaScript `"` becomes `\"`
 *
 * This is the inverse of [UnescapeTarget].
 */
sealed interface EscapeTarget {
    /**
     * Escapes the input string for the target format.
     * @param input the string to escape
     * @return the escaped string
     */
    fun escape(input: String): String
}

/**
 * Represents a target (commonly a language or format) that strings can be unescaped from.
 *
 * For instance:
 * - in HTML `&lt;` becomes `<`
 * - in JavaScript `\"` becomes `"`
 *
 * This is the inverse of [EscapeTarget].
 */
sealed interface UnescapeTarget {
    /**
     * Unescapes the input string from the target format.
     * @param input the string to unescape
     * @return the unescaped string
     */
    fun unescape(input: String): String
}

/**
 * Utilities for escaping and unescaping strings for various targets.
 */
object Escape {
    object Html : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = KsoupEntities.encodeHtml4(input)

        override fun unescape(input: String): String = KsoupEntities.decodeHtml4(input)
    }

    object Xml : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = KsoupEntities.encodeXml(input)

        override fun unescape(input: String): String = KsoupEntities.decodeXml(input)
    }

    object JavaScript : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = Json.escape(input).replace("</", "<\\/")

        override fun unescape(input: String): String = Json.unescape(input)
    }

    object Json : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = JsonPrimitive(input).toString().removeSurrounding("\"")

        override fun unescape(input: String): String =
            kotlinx.serialization.json.Json
                .parseToJsonElement("\"$input\"")
                .jsonPrimitive
                .content
    }

    /**
     * Percent-encodes a URL path. Each `/`-separated segment is encoded individually,
     * preserving `/` as a path separator. Spaces become `%20` (not `+`).
     */
    object Url : EscapeTarget {
        override fun escape(input: String): String =
            input.split("/").joinToString("/") {
                it.encodeURLParameter(spaceToPlus = false)
            }
    }
}
