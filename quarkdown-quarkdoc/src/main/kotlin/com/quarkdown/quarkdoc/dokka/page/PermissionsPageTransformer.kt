package com.quarkdown.quarkdoc.dokka.page

import com.quarkdown.core.permissions.Permission
import com.quarkdown.quarkdoc.dokka.util.findDeep
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.plugability.DokkaContext

private const val TAG_NAME = "permission"

/**
 * Data extracted from a single `@permission` documentation tag.
 * @param name the permission identifier (e.g. `project-read`)
 * @param description optional human-readable description of why the permission is needed
 */
data class PermissionData(
    val name: String,
    val description: String?,
)

/**
 * Resolves a permission class simple name (e.g. `ProjectRead`) to its kebab-case identifier (e.g. `project-read`).
 * @return the permission name, or `null` if the class name does not match any known permission
 */
private fun resolvePermissionName(className: String): String? = Permission.ALL.find { it::class.simpleName == className }?.name

/**
 * Transformer that generates a "Permissions" section listing the permissions required by a function,
 * as declared via `@permission` KDoc tags.
 */
class PermissionsPageTransformer(
    context: DokkaContext,
) : NewSectionDocumentablePageTransformer<DFunction, List<PermissionData>>("Permissions", context) {
    override fun extractDocumentable(documentables: List<Documentable>) = documentables.firstOrNull() as? DFunction

    /**
     * Extracts permission data from all `@permission` documentation tags of the given function.
     * Each tag is expected to contain a [DocumentationLink] referencing a [Permission] subclass,
     * optionally followed by a text description.
     */
    override fun extractData(documentable: DFunction): List<PermissionData>? {
        val documentation = documentable.documentation.values.firstOrNull() ?: return null

        val permissions =
            documentation
                .withDescendants()
                .filterIsInstance<CustomTagWrapper>()
                .filter { it.name == TAG_NAME }
                .mapNotNull { tag -> extractPermission(tag) }
                .toList()

        return permissions.ifEmpty { null }
    }

    /**
     * Extracts a [PermissionData] from a single `@permission` tag.
     * @return the extracted data, or `null` if the tag does not contain a valid permission reference
     */
    private fun extractPermission(tag: CustomTagWrapper): PermissionData? {
        val link = tag.root.findDeep<DocumentationLink>() ?: return null
        val className = link.dri.classNames?.substringAfterLast('.') ?: return null
        val permissionName = resolvePermissionName(className) ?: return null

        // The tag tree is: root -> P -> [DocumentationLink, Text(description)].
        val linkParent = tag.root.children.firstOrNull() ?: tag.root
        val description =
            linkParent.children
                .filterIsInstance<Text>()
                .joinToString("") { it.body }
                .trim()
                .ifEmpty { null }

        return PermissionData(permissionName, description)
    }

    override fun createSection(
        data: List<PermissionData>,
        documentable: DFunction,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ): ContentNode =
        builder.buildGroup {
            table {
                data.forEach { permission ->
                    row {
                        group(styles = setOf(ContentStyle.RowTitle)) {
                            text(permission.name)
                        }
                        permission.description?.let { text(it) }
                    }
                }
            }
        }
}
