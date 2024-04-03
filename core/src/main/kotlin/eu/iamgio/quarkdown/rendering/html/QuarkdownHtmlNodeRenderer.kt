package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.context.Context
import eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(context: Context) : BaseHtmlNodeRenderer(context) {
    override fun createCodeWrapper() =
        RenderWrapper.fromResourceName("/render/base/html-wrapper.html")
            .value("LANG", "")
            .value("TITLE", "") // TODO extract from context / document settings which can be affected by functions

    // Quarkdown nodes rendering
}
