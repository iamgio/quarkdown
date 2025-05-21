package com.quarkdown.core.ast.attributes.presence

import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.property.Property

/**
 * @return whether the [AstAttributes] contain a third-party presence property with the given [key]
 */
internal fun AstAttributes.hasThirdParty(key: Property.Key<Boolean>): Boolean = thirdPartyPresenceProperties[key] == true

/**
 * Marks the presence of a third-party element in the AST via the given [property].
 */
internal fun MutableAstAttributes.markThirdPartyPresence(property: Property<Boolean>) {
    thirdPartyPresenceProperties += property
}
