package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.context.Context
import eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
import eu.iamgio.quarkdown.rendering.wrapper.TemplatePlaceholders

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(context: Context) : BaseHtmlNodeRenderer(context) {
    override fun createCodeWrapper() =
        RenderWrapper.fromResourceName("/render/quarkdown/html-wrapper.html")
            // TODO extract from context / document settings which can be affected by functions
            .value(TemplatePlaceholders.TITLE, "en")
            .value(TemplatePlaceholders.LANGUAGE, "")

    // Quarkdown nodes rendering
}
