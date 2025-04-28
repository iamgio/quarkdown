package eu.iamgio.quarkdown.document.size

/**
 * A generic bounding box with a width and a height.
 */
data class BoundingBox(
    val width: Size,
    val height: Size,
) {
    /**
     * A 90-degrees rotated version of this bounding box,
     * which happens to be a new [BoundingBox] with the height and width swapped.
     */
    val rotated: BoundingBox
        get() = BoundingBox(height, width)
}

/**
 * Shorthand for creating a [BoundingBox] from two [Size]s.
 * @param height height of the bounding box
 * @return a new [BoundingBox] with [this] width and the given [height]
 */
infix fun Size.by(height: Size) = BoundingBox(this, height)
