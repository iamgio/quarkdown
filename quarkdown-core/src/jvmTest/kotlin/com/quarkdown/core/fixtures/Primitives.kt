@file:QModule

package com.quarkdown.core.fixtures

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture module backing AstRewriterTest. `heading` shadows the stdlib primitive of the same name,
 * so the rewriter has something to swap a Heading node for.
 */

@QFunction
fun heading(
    @Body content: InlineMarkdownContent,
    depth: Int,
    ref: String? = null,
    numbered: Boolean = true,
    indexed: Boolean = true,
    breakpage: Boolean = true,
): NodeValue =
    NodeValue(
        BlockQuote(
            content =
                listOf(
                    Heading(
                        depth = depth,
                        text = content.children,
                        customId = ref,
                        canBreakPage = breakpage,
                        canTrackLocation = numbered,
                        excludeFromTableOfContents = !indexed,
                    ),
                ),
        ),
    )
