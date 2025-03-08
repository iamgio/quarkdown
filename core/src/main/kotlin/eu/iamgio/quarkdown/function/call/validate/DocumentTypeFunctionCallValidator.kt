package eu.iamgio.quarkdown.function.call.validate

import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.error.InvalidFunctionCallException
import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * Validator of a function call that checks if the document the function call lies in is of a certain type.
 * If not, an [InvalidFunctionCallException] is thrown.
 * @param T output type of the function
 * @param allowedTypes allowed document types
 */
class DocumentTypeFunctionCallValidator<T : OutputValue<*>>(
    private val allowedTypes: Iterable<DocumentType>,
) : FunctionCallValidator<T> {
    override fun validate(call: FunctionCall<T>) {
        val type = call.context?.documentInfo?.type ?: return
        if (type in allowedTypes) {
            return
        }

        throw InvalidFunctionCallException(
            call,
            reason =
                "Function ${call.function.name} was called in a $type document type, " +
                    "while it only allows the following: ${allowedTypes.joinToString()}",
        )
    }
}
