package com.quarkdown.core

import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.property.MutablePropertyContainer
import com.quarkdown.core.property.Property
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class PropertiesTest {
    private data class MyProperty(
        override val value: String,
    ) : Property<String> {
        companion object : Property.Key<String>

        override val key: Property.Key<String> = MyProperty
    }

    private val property = MyProperty("test")

    @Test
    fun properties() {
        val properties = MutablePropertyContainer<String>()
        properties += property

        val retrievedProperty: String? = properties[MyProperty]
        assertEquals(property.value, retrievedProperty)
    }

    @Test
    fun `node properties`() {
        val attributes = MutableAstAttributes()
        val node = Text("hello")
        attributes.properties.of(node) += property

        val retrievedProperty: String? = attributes.properties.of(node)[MyProperty]
        assertEquals(property.value, retrievedProperty)
    }
}
