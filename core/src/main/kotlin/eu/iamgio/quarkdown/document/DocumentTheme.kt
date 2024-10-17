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

/**
 * Given [this] theme with nullable components, merges it with a default theme in order to fill in the missing components.
 * If [this] is `null` itself, the default theme is returned.
 * @param default default theme
 * @return a new theme with all components filled in, either from [this] (higher priority) or [default] (fill)
 */
fun DocumentTheme?.orDefault(default: DocumentTheme) =
    DocumentTheme(
        color = this?.color ?: default.color,
        layout = this?.layout ?: default.layout,
    )
