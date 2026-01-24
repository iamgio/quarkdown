package com.quarkdown.stdlib.internal

/**
 * Matches CSS property declarations: `: value;` or `: value` before `}`
 */
private val CSS_PROPERTY_VALUE_PATTERN = Regex("""(:\s*)([^;{}:]+?)(\s*)(;|(?=}))""")

/**
 * Applies `!important` to each CSS property value that doesn't already have it.
 */
internal fun applyImportantToCSS(css: String): String =
    css.replace(CSS_PROPERTY_VALUE_PATTERN) { match ->
        val colon = match.groupValues[1]
        val value = match.groupValues[2].trim()
        val whitespace = match.groupValues[3]
        val terminator = match.groupValues[4]
        if (value.endsWith("!important")) {
            match.value
        } else {
            "$colon$value !important$whitespace$terminator"
        }
    }
