package com.quarkdown.stdlib

import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Reference` stdlib module exporter.
 * This module handles cross-references.
 * @see com.quarkdown.core.ast.quarkdown.reference
 */
val Reference: Module =
    moduleOf(
        ::reference,
    )

/**
 * Creates a reference to a target node with a matching ID.
 *
 * Examples of referenceable nodes include:
 *
 * - Headings (`# Heading {#id}`)
 * - Figures (`![Alt](image.png "Caption"){#id}`)
 * - Tables
 * - Code blocks (`lang {#id} ...`)
 * - Custom numbered blocks (`.numbered {key} ref:{id}`)
 *
 * The reference is successfully resolved if the ID matches that of a referenceable node in the document:
 *
 * ```
 * .ref {id}
 * ```
 *
 * @param id the reference ID of the target node being referenced
 * @return a [CrossReference] to the target node
 * @wiki Cross-reference
 */
@Name("ref")
fun reference(id: String) = CrossReference(id).wrappedAsValue()
