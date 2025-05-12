package com.quarkdown.core.function

/**
 * @return [this] string reformatted in Quarkdown format (lowercase, no underscores). i.e. `SPACE_AROUND` -> `spacearound`
 */
fun String.toQuarkdownNamingFormat(): String = lowercase().replace("_", "")

/**
 * @return [this] enum's name in Quarkdown format (lowercase, no underscores). i.e. `SPACE_AROUND` -> `spacearound`
 */
val Enum<*>.quarkdownName: String
    get() = name.toQuarkdownNamingFormat()
