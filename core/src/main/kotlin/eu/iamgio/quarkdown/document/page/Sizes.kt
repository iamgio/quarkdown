package eu.iamgio.quarkdown.document.page

/**
 * A collection of generic top, right, bottom and left [Size]s.
 */
data class Sizes(val top: Size, val right: Size, val bottom: Size, val left: Size) {
    /**
     * Creates a [Sizes] object with the same [Size] for all sides.
     */
    constructor(all: Size) : this(all, all, all, all)

    /**
     * Creates a [Sizes] object with the same [Size] for vertical (top, bottom) and horizontal (left, right) sides.
     */
    constructor(vertical: Size, horizontal: Size) : this(vertical, horizontal, vertical, horizontal)

    /**
     * This size collection as a CSS value string.
     */
    val asCSS: String
        get() = "$top $right $bottom $left"
}
