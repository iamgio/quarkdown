@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Creates a reference to a target node with a matching ID.
 *
 * Examples of referenceable nodes include:
 *
 * - Headings
 *
 *   ```markdown
 *   # Heading {#id}
 *   ```
 *
 * - Figures
 *
 *   ```markdown
 *   ![Alt](image.png "Caption"){#id}
 *   ```
 *
 * - Tables
 *
 *   ```markdown
 *   | Header | Header |
 *   |--------|--------|
 *   | Cell   | Cell   |
 *   {#id}
 *   ```
 *
 * - Code blocks
 *
 *   ~~~markdown
 *   ```python {#id}
 *   print("Hello, World!")
 *   ```
 *   ~~~
 *
 * - Custom [numbered] blocks
 *
 *   ```markdown
 *   .numbered {key} ref:{id}
 *   ```
 *
 * The reference is successfully resolved if the ID matches that of a referenceable node in the document:
 *
 * ```
 * .ref {id}
 * ```
 *
 * @param id the reference ID of the target node being referenced
 * @return a [CrossReference] to the target node
 * @wiki cross-references
 */
@QFunction
@Name("ref")
fun reference(id: String) = CrossReference(id).wrappedAsValue()
