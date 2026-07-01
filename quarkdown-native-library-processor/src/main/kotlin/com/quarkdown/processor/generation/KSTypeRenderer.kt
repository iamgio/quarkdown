package com.quarkdown.processor.generation

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Variance

/**
 * Renders a [KSType] back to a Kotlin source-level type string, fully qualified.
 */
internal object KSTypeRenderer {
    /**
     * Renders [type] to a Kotlin source-level qualified type string.
     */
    fun render(type: KSType): String {
        val name =
            type.declaration.qualifiedName?.asString()
                ?: type.declaration.simpleName.asString()
        val args =
            type.arguments
                .takeIf { it.isNotEmpty() }
                ?.joinToString(
                    prefix = "<",
                    postfix = ">",
                    transform = ::renderArgument,
                ).orEmpty()
        val nullability = if (type.isMarkedNullable) "?" else ""
        return "$name$args$nullability"
    }

    /**
     * Renders a single generic type argument, including its variance.
     * Star projections and unresolved type references collapse to `*`.
     */
    private fun renderArgument(argument: KSTypeArgument): String {
        val type = argument.type?.resolve() ?: return "*"
        val rendered = render(type)
        return when (argument.variance) {
            Variance.COVARIANT -> "out $rendered"
            Variance.CONTRAVARIANT -> "in $rendered"
            Variance.STAR -> "*"
            Variance.INVARIANT -> rendered
        }
    }
}
