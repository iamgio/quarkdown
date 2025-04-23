package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagram
import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagramFigure
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.data.EvaluableString
import eu.iamgio.quarkdown.function.value.data.Range
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
 * @param line whether to draw a line chart
 * @param bars whether to draw a bar chart
 * @param xAxisLabel optional label for the X axis
 * @param yAxisLabel optional label for the Y axis
 * @param yAxisRange optional range for the Y axis. If open-ended, the range will be set to the minimum and maximum values of the Y values
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param values the Y values to plot
 * @return the generated diagram node
 */
@Name("xychart")
fun xyChart(
    line: Boolean = true,
    bars: Boolean = false,
    @Name("x") xAxisLabel: String? = null,
    @Name("y") yAxisLabel: String? = null,
    @Name("yrange") yAxisRange: Range? = null,
    caption: String? = null,
    values: Iterable<OutputValue<*>>,
): NodeValue {
    val points = values.map { it.asDouble() }

    val content =
        buildString {
            xAxisLabel?.let {
                append("\n")
                append("x-axis \"")
                append(it)
                append("\"")
            }

            if (yAxisLabel != null || yAxisRange != null) {
                append("\n")
                append("y-axis")
                yAxisLabel?.let {
                    append(" \"")
                    append(it)
                    append("\"")
                }
                yAxisRange?.let {
                    append(" ")
                    append(it.start ?: points.minOrNull() ?: 0.0)
                    append(" --> ")
                    append(it.end ?: points.maxOrNull() ?: 1.0)
                }
            }

            if (line) {
                append("\n")
                append("line ")
                append(points)
            }
            if (bars) {
                append("\n")
                append("bar ")
                append(points)
            }
        }

    val code = "xychart-beta\n" + content.indent("\t")
    return mermaidFigure(caption, code)
}
