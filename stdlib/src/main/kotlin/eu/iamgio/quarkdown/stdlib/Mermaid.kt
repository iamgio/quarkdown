package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagram
import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagramFigure
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.data.EvaluableString
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.stdlib.internal.asDouble
import eu.iamgio.quarkdown.util.indent

/**
 * `Mermaid` stdlib module exporter.
 * This module handles generation of Mermaid diagrams.
 */
val Mermaid: Module =
    setOf(
        ::mermaid,
        ::xyChart,
    )

private fun mermaidFigure(
    caption: String?,
    code: String,
) = MermaidDiagramFigure(
    MermaidDiagram(code),
    caption,
).wrappedAsValue()

/**
 * Creates a Mermaid diagram.
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param code the Mermaid code of the diagram
 * @return the generated diagram node
 */
fun mermaid(
    caption: String? = null,
    code: EvaluableString,
) = mermaidFigure(caption, code.content)

/**
 * Creates a chart diagram on the XY plane.
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param values the Y values to plot
 * @return the generated diagram node
 */
@Name("xychart")
fun xyChart(
    caption: String? = null,
    values: Iterable<OutputValue<*>>,
): NodeValue {
    val content =
        buildString {
            append("bar [")
            values.joinToString(separator = ", ") { it.asDouble().toString() }.also(::append)
            append("]")
        }

    val code = "xychart-beta\n" + content.indent("\t")
    println(code)
    return mermaidFigure(caption, code)
}
