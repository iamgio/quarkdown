package eu.iamgio.quarkdown.lexer.regex

/**
 * A builder for [Regex] patterns.
 * @param baseRegex initial pattern
 */
class RegexBuilder(baseRegex: String) {
    private val pattern = StringBuilder(baseRegex)

    /**
     * Adds a reference to the pattern. The first occurrence be later replaced.
     * @param label text to be replaced
     * @param regex new pattern to insert
     * @return this for concatenation
     */
    fun withReference(
        label: String,
        regex: String,
    ) = apply {
        val index = pattern.indexOf(label)
        pattern.replace(index, index + label.length, regex)
    }

    /**
     * @return a new [Regex]
     */
    fun build(): Regex = pattern.toString().toRegex()

    /**
     * @return a new [Regex] with the given [option].
     */
    fun build(option: RegexOption): Regex = pattern.toString().toRegex(option)
}
