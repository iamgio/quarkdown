package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.AstAttributes

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param attributes additional attributes of the node tree
 */
class QuarkdownHtmlNodeRenderer(private val attributes: AstAttributes) : BaseHtmlNodeRenderer(attributes) {
    // Quarkdown nodes rendering
}
