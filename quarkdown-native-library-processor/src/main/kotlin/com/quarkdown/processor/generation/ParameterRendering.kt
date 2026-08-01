package com.quarkdown.processor.generation

import com.quarkdown.processor.model.ParameterDescriptor
import com.quarkdown.processor.util.backtick
import com.quarkdown.processor.util.backtickLastSegment

/**
 * Flat plain-parameter view of a source-level parameter list, mirroring the shape of the
 * generated wrapper's signature: [ParameterDescriptor.Plain] parameters map to themselves,
 * [ParameterDescriptor.Spread] parameters expand to their per-component [ParameterDescriptor.Plain]s.
 *
 * Every generator pass that needs "one entry per emitted parameter" starts from this view.
 */
internal val List<ParameterDescriptor>.flatParameters: List<ParameterDescriptor.Plain>
    get() = flatMap { it.contributions }

private val ParameterDescriptor.contributions: List<ParameterDescriptor.Plain>
    get() =
        when (this) {
            is ParameterDescriptor.Plain -> listOf(this)
            is ParameterDescriptor.Spread -> components
        }

/**
 * Signature entry for a wrapper parameter: annotations, name, type, and optional default.
 *
 * Example: `@Body content: com.quarkdown.core.ast.InlineMarkdownContent`.
 */
internal fun ParameterDescriptor.Plain.wrapperSignatureEntry(): String {
    val annotations = sourceAnnotations?.let { "$it " } ?: ""
    val declaration = "$annotations${exportedName.backtick()}: ${KSTypeRenderer.render(type)}"
    return defaultExpression?.let { "$declaration = $it" } ?: declaration
}

/**
 * Delegation argument for one source-level parameter: `original = exported` for a plain
 * parameter, and `original = FQN(componentOriginal = componentExported, ...)` for a spread one,
 * so the source function receives a reconstructed data-class instance.
 */
internal val ParameterDescriptor.delegationArgument: String
    get() =
        when (this) {
            is ParameterDescriptor.Plain -> {
                "${originalName.backtick()} = ${exportedName.backtick()}"
            }

            is ParameterDescriptor.Spread -> {
                val reconstruction =
                    components.joinToString(", ") {
                        "${it.originalName.backtick()} = ${it.exportedName.backtick()}"
                    }
                "${originalName.backtick()} = ${dataClassFqn.backtickLastSegment()}($reconstruction)"
            }
        }
