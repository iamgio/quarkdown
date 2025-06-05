package com.quarkdown.stdlib

import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagramFigure
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.data.EvaluableString
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.util.indent
import com.quarkdown.stdlib.internal.asDouble

/**
 * `Mermaid` stdlib module exporter.
 * This module handles generation of Mermaid diagrams.
 */
val Mermaid: Module =
    moduleOf(
        ::mermaid,
        ::xyChart,
    )

private fun mermaidFigure(
    caption: String?,
    @LikelyBody code: String,
) = MermaidDiagramFigure(
    MermaidDiagram(code),
    caption,
).wrappedAsValue()

/**
 * Creates a Mermaid diagram.
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param code the Mermaid code of the diagram
 * @return a new [MermaidDiagramFigure] node
 */
fun mermaid(
    caption: String? = null,
    @LikelyBody code: EvaluableString,
) = mermaidFigure(caption, code.content)

/**
 * A chart line is a list of its points.
 */
private typealias ChartLine = List<Double>

/**
 * Extracts the lines from the given values.
 * If a value in [values] is a collection of points, then it's a line.
 * Any other value is considered a standalone point, and an additional line is created for them.
 * @param values the values to extract lines from
 * @return a list of chart lines
 */
private fun extractLines(values: Iterable<OutputValue<*>>): List<ChartLine> {
    val (lines, points) = values.partition { it is IterableValue<*> }
    val lineOfPoints: ChartLine = points.map { it.asDouble() }
    return lines
        .asSequence()
        .map { it as IterableValue<*> }
        .filterNot { it.unwrappedValue.none() }
        .map { it.unwrappedValue.map { point -> point.asDouble() } }
        .toMutableList()
        .apply {
            if (lineOfPoints.isNotEmpty()) add(lineOfPoints)
        }
}

/**
 * Appends the axis definition to the given [StringBuilder].
 * @param name name of the axis (e.g. "x" or "y")
 * @param label optional label of the axis
 * @param range optional range of the axis
 * @param tags optional categorical tags of the axis
 * @param min minimum value of the plotted points along the axis
 * @param max maximum value of the plotted points along the axis
 */
private fun StringBuilder.axis(
    name: String,
    label: String?,
    range: Range?,
    tags: Iterable<Value<*>>?,
    min: Double,
    max: Double,
) {
    if (label == null && range == null && tags == null) return

    require(!(range != null && tags != null)) { "An XY chart axis cannot feature both numeric range and categorical tags." }

    append("\n")
    append(name).append("-axis")
    label?.let {
        append(" \"")
        append(it)
        append("\"")
    }
    range?.let {
        append(" ")
        append(it.start ?: min)
        append(" --> ")
        append(it.end ?: max)
    }
    tags?.let {
        append(" ")
        append(it.map { tag -> tag.unwrappedValue })
    }
}

/**
 * Creates a chart diagram on the XY plane.
 *
 * The following example plots 4 points at (1, 5), (2, 2), (3, 4), (4, 10), connected by a line:
 * ```
 * .xychart
 *   - 5
 *   - 2
 *   - 4
 *   - 10
 * ```
 *
 * Multiple lines can be plotted by supplying a list of lists of points. Each list will be plotted as a separate line:
 * ```
 * .xychart
 *   - |
 *     - 5
 *     - 8
 *     - 3
 *   - |
 *     - 3
 *     - 5
 *     - 10
 *   - |
 *     - 8
 *     - 3
 *     - 5
 * ```
 *
 * @param showLines whether to draw lines
 * @param showBars whether to draw bars
 * @param xAxisLabel optional label for the X axis
 * @param xAxisRange optional range for the X axis. If open-ended, the range will be set to the minimum and maximum values of the X values. Incompatible with [xAxisTags].
 * @param xAxisTags optional categorical tags for the X axis. Incompatible with [xAxisRange].
 * @param yAxisLabel optional label for the Y axis
 * @param yAxisRange optional range for the Y axis. If open-ended, the range will be set to the minimum and maximum values of the Y values
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param values the Y values to plot.
 *               They can be a list of points, which will be plotted as a single line,
 *               or a list of lists of points, which will be plotted as multiple lines.
 * @return the generated diagram node
 * @throws IllegalArgumentException if both [xrange] and [xtags] are set
 * @wiki XY chart
 */
@Name("xychart")
fun xyChart(
    @Name("lines") showLines: Boolean = true,
    @Name("bars") showBars: Boolean = false,
    @Name("x") xAxisLabel: String? = null,
    @Name("xrange") xAxisRange: Range? = null,
    @Name("xtags") xAxisTags: Iterable<Value<*>>? = null,
    @Name("y") yAxisLabel: String? = null,
    @Name("yrange") yAxisRange: Range? = null,
    caption: String? = null,
    values: Iterable<OutputValue<*>>,
): NodeValue {
    val lines: List<ChartLine> = extractLines(values)
    val (minY, maxY) = lines.flatten().let { (it.minOrNull() ?: 0.0) to (it.maxOrNull() ?: 1.0) }
    val (minX, maxX) = 0.0 to (lines.maxByOrNull { it.size }?.size?.toDouble() ?: 1.0)

    val content =
        buildString {
            axis("x", xAxisLabel, xAxisRange, xAxisTags, minX, maxX)
            axis("y", yAxisLabel, yAxisRange, null, minY, maxY)

            lines.forEach { points ->
                if (showBars) {
                    append("\n")
                    append("bar ")
                    append(points)
                }
                if (showLines) {
                    append("\n")
                    append("line ")
                    append(points)
                }
            }
        }

    val code = "xychart-beta\n" + content.indent("\t")
    return mermaidFigure(caption, code)
}
