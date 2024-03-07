package eu.iamgio.quarkdown.lexer.regex

/**
 * A builder for [Regex] patterns.
 * @param baseRegex initial pattern
 */
class RegexBuilder(baseRegex: String) {
    private val pattern = StringBuilder(baseRegex)

    /**
     * Adds a reference to the pattern. All the occurrences will be replaced.
     * @param label text to be replaced
     * @param regex new pattern to replace the label with
     * @return this for concatenation
     */
    fun withReference(
        label: String,
        regex: String,
    ) = apply {
        var index: Int
        while (pattern.indexOf(label).also { index = it } >= 0) {
            pattern.replace(index, index + label.length, regex)
        }
    }

    /**
     * @return a new [Regex]
     */
    fun build(): Regex = pattern.toString().toRegex()

    /**
     * @return a new [Regex] with the given [option].
     */
    fun build(option: RegexOption): Regex = pattern.toString().toRegex(option)

    /**
     * @return a new [Regex] with the given [options].
     */
    fun build(vararg options: RegexOption): Regex = pattern.toString().toRegex(options.toSet())
}
