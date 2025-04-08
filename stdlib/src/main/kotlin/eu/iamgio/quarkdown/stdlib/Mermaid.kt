package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagram
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
 * @param code the Mermaid code of the diagram
 * @return the generated diagram node
 */
fun mermaid(code: String) = MermaidDiagram(code).wrappedAsValue()
