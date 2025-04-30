package com.quarkdown.core.function.value.data

import com.quarkdown.core.function.value.factory.ValueFactory

/**
 * A [String] wrapper that, when used as a function parameter, lets [ValueFactory.evaluableString] evaluate the raw content.
 * This allows function calls and other scripting techniques to be used executed within the string itself,
 * which would otherwise be natively unsupported unless the [String] argument is inlined (not used as a block argument).
 * Inline string evaluation is handled directly by the parser [com.quarkdown.core.parser.BlockTokenParser]).
 * This is used for example in the `.code` stdlib function.
 * @param content unwrapped string content, already evaluated
 * @see ValueFactory.evaluableString
 */
data class EvaluableString(val content: String)
