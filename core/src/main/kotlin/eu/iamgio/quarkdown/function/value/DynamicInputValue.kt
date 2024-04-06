package eu.iamgio.quarkdown.function.value

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * An [InputValue] whose type has not been yet determined.
 * @param unwrappedValue raw, unprocessed representation of the wrapped value
 */
data class DynamicInputValue(override val unwrappedValue: String) : InputValue<String> {
    /**
     * @param type type of the value to convert this automatic value to
     * @return a new typed [InputValue], automatically determined from [type]
     */
    fun convertTo(type: KClass<out InputValue<*>>): InputValue<*> =
        when {
            type.isSubclassOf(String::class) -> ValueFactory.string(unwrappedValue)
            type.isSubclassOf(Number::class) -> ValueFactory.number(unwrappedValue)
            else -> throw IllegalArgumentException("Cannot convert DynamicInputValue to type $type")
        }
}
