package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.json.DocsIndex
import com.quarkdown.quarkdoc.reader.json.IndexedFunction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards `docs-index.schema.json` against drifting from the actual wire format.
 */
class DocsIndexSchemaTest {
    @Test
    fun `schema matches the wire format`() {
        val schema = Json.parseToJsonElement(File("docs-index.schema.json").readText()).jsonObject
        val definitions = schema["\$defs"]!!.jsonObject

        fun requiredFields(definition: String): Set<String> =
            definitions[definition]!!
                .jsonObject["required"]!!
                .jsonArray
                .map { it.jsonPrimitive.content }
                .toSet()

        val sample =
            DocsIndex(
                functions =
                    listOf(
                        IndexedFunction(
                            name = "f",
                            moduleName = "M",
                            function =
                                DocsFunction(
                                    name = "f",
                                    parameters =
                                        listOf(
                                            DocsParameter(
                                                name = "p",
                                                description = "d",
                                                isOptional = false,
                                                isLikelyNamed = false,
                                                isLikelyBody = false,
                                                allowedValues = null,
                                            ),
                                        ),
                                    isLikelyChained = false,
                                ),
                            contentMarkdown = null,
                        ),
                    ),
            )

        val serialized = Json.encodeToJsonElement(sample).jsonObject
        val function = serialized["functions"]!!.jsonArray.single().jsonObject

        assertEquals(setOf("functions"), serialized.keys)
        assertEquals(requiredFields("indexedFunction"), function.keys)
        assertEquals(requiredFields("function"), function["function"]!!.jsonObject.keys)
        assertEquals(
            requiredFields("parameter"),
            function["function"]!!
                .jsonObject["parameters"]!!
                .jsonArray
                .single()
                .jsonObject.keys,
        )
    }
}
