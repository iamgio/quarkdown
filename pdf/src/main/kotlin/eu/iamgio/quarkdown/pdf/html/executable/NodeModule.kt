package eu.iamgio.quarkdown.pdf.html.executable

/**
 *
 */
open class NodeModule(
    private val name: String,
) {
    fun isInstalled(wrapper: NodeJsWrapper): Boolean = wrapper.eval("require('$name')").isEmpty()
}
