package eu.iamgio.quarkdown.pdf.html.executable

/**
 *
 */
open class NodeModule(
    val name: String,
) {
    fun isInstalled(wrapper: NodeJsWrapper): Boolean = wrapper.eval("require('$name')").also { println(it) }.isEmpty()
}
