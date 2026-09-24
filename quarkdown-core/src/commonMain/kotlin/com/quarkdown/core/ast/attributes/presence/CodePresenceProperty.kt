package com.quarkdown.core.ast.attributes.presence

import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.property.Property

/**
 * If this property is present in [com.quarkdown.core.ast.attributes.AstAttributes.thirdPartyPresenceProperties]
 * and its [value] is true, it means there is at least one code block in the AST.
 * This is used to load the HighlightJS library in HTML rendering only if necessary.
 * @see com.quarkdown.core.context.hooks.presence.CodePresenceHook
 */
data class CodePresenceProperty(
    override val value: Boolean,
) : Property<Boolean> {
    companion object : Property.Key<Boolean>

    override val key: Property.Key<Boolean> = CodePresenceProperty
}

/**
 * Whether there is at least one code block in the AST.
 * @see CodePresenceProperty
 */
val AstAttributes.hasCode: Boolean
    get() = hasThirdParty(CodePresenceProperty)

/**
 * Marks the presence of code blocks in the AST
 * if at least one [Code] block is present in the document.
 * @see CodePresenceProperty
 * @see com.quarkdown.core.context.hooks.presence.CodePresenceHook
 */
fun MutableAstAttributes.markCodePresence() = markThirdPartyPresence(CodePresenceProperty(true))
