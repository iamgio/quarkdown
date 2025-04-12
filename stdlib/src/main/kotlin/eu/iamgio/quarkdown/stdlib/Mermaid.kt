package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagram
import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagramFigure
import eu.iamgio.quarkdown.function.value.data.EvaluableString
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Mermaid` stdlib module exporter.
 * This module handles generation of Mermaid diagrams.
 */
val Mermaid: Module =
    setOf(
        ::mermaid,
    )

/**
 * Creates a Mermaid diagram.
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param code the Mermaid code of the diagram
 * @return the generated diagram node
 */
fun mermaid(
    caption: String? = null,
    code: EvaluableString,
) = MermaidDiagramFigure(
    MermaidDiagram(code.content),
    caption,
).wrappedAsValue()
