package com.quarkdown.core.ast.attributes.reference

import com.quarkdown.core.property.Property

/**
 * A pair of a referenced linked to its resolved definition.
 */
typealias ResolvedReference<R, D> = Pair<R, D>

/**
 * [Property] that can be assigned to each [ReferenceNode]. It contains the definition that the reference refers to.
 * @see ReferenceNode
 * @see com.quarkdown.core.context.hooks.reference.ReferenceDefinitionResolverHook for the assignment stage
 */
data class ResolvedReferenceProperty<R, D>(
    override val value: ResolvedReference<R, D>,
) : Property<ResolvedReference<R, D>> {
    class Key<R, D> : Property.Key<ResolvedReference<R, D>> {
        override fun equals(other: Any?): Boolean = other is Key<*, *>

        override fun hashCode(): Int = Key::class.java.hashCode()
    }

    override val key = Key<R, D>()
}
