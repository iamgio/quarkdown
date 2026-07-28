package com.quarkdown.processor.generation

import com.google.devtools.ksp.symbol.KSType
import com.quarkdown.processor.model.ParameterDescriptor
import com.quarkdown.processor.util.backtick
import com.quarkdown.processor.util.backtickLastSegment

/**
 * Flat plain-parameter view of a source-level parameter list, mirroring the shape of the
 * generated wrapper's signature: [ParameterDescriptor.Plain] parameters map to themselves,
 * [ParameterDescriptor.Spread] parameters expand to their per-component [ParameterDescriptor.Plain]s.
 *
 * Every generator pass that needs "one entry per emitted parameter" (indexing, signature entries,
 * `FunctionParameter(...)` literals, typed local declarations) starts from this view.
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
 */
internal fun ParameterDescriptor.Plain.wrapperSignatureEntry(): String {
    val annotations = sourceAnnotations?.let { "$it " } ?: ""
    val declaration = "$annotations${exportedName.backtick()}: ${KSTypeRenderer.render(type)}"
    return defaultExpression?.let { "$declaration = $it" } ?: declaration
}

/**
 * Argument passed to the source function from within either the wrapper body or the pre-built
 * function value's invoke lambda: `original = exported` for a plain parameter, a reconstructed
 * data-class instance for a spread parameter.
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

/**
 * `FunctionParameter(...)` literal for the flat parameter at [index].
 */
internal fun ParameterDescriptor.Plain.functionParameterLiteral(index: Int): String =
    "${Fqns.functionParameter}(" +
        "name = \"$exportedName\", " +
        "type = ${classLiteralOf(type)}::class, " +
        "index = $index, " +
        "isOptional = $isOptional, " +
        "isExplicitlyBody = $isExplicitlyBody, " +
        "isInjected = $isInjected, " +
        "isNullable = $isNullable" +
        ")"

/**
 * Full `val name: SourceType = expr` line for the typed local backing the flat parameter at [index].
 */
internal fun ParameterDescriptor.Plain.typedLocalDeclaration(index: Int): String {
    val sourceType = KSTypeRenderer.render(type)
    val cast =
        when {
            isNullable || isInjected -> "slots[$index] as $sourceType"
            else -> "slots[$index]!! as $sourceType"
        }
    val initializer =
        when {
            isOptional -> "if (present[$index]) ($cast) else ($defaultExpression)"
            else -> cast
        }
    return "val ${exportedName.backtick()}: $sourceType = $initializer"
}

/** FQN of [type] with any nullability and type arguments stripped, so it can precede `::class`. */
private fun classLiteralOf(type: KSType): String = type.declaration.qualifiedName?.asString() ?: type.declaration.simpleName.asString()
