package eu.iamgio.quarkdown.document

/**
 * The theme of a document. A theme is defined by different components, hence allowing different combinations.
 * Components can also be not specified by setting them to `null`, and are hence ignored.
 * @param color color scheme component (refers to an internal resource in `resources/render/theme/color`)
 * @param layout layout format component (refers to an internal resource in `resources/render/theme/layout`)
 */
data class DocumentTheme(
    val color: String?,
    val layout: String?,
)
