@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.ScopeContext
import com.quarkdown.core.context.SharedContext
import com.quarkdown.core.context.SubdocumentContext
import com.quarkdown.core.context.packageResolver
import com.quarkdown.core.filesystem.FileSystem
import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.packages.PackageReferenceSyntax
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.core.packages.ResolvedPackage
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.requirePermission
import com.quarkdown.core.permissions.requireReadPermission
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.stdlib.internal.asString

/**
 * Includes the parsed content of the given raw Quarkdown [code] in the current document.
 * The context of the main file is shared, allowing for sharing of variables, functions and other declarations.
 * @param context main context to share
 * @param code raw Quarkdown source to include
 * @return the content of the file as a node
 */
internal fun includeResource(
    context: Context,
    code: String,
): NodeValue {
    // Evaluate the Quarkdown source.
    // This automatically converts the source into a value (e.g. a node, a string, a number, etc.)
    // and fills the current context with new declarations (e.g. variables, functions, link definitions, etc.)
    return ValueFactory.blockMarkdown(code, context).asNodeValue()
}

/**
 * Relationship between the main context and the included file's context via [include].
 * See [include] for details about each mode.
 */
enum class ContextSandbox {
    /** @see SharedContext */
    SHARE,

    /** @see ScopeContext */
    SCOPE,

    /** @see SubdocumentContext */
    SUBDOCUMENT,
}

/**
 * This function has three behaviors:
 * - Reads a Quarkdown file and includes its parsed content in the current document,
 *   using the specified [sandbox] strategy to determine what information is shared between the main context and the included file's context.
 * - Loads a library into the current context and includes its parsed content in the current document.
 *   Loadable libraries are fetched from the library folder (`--libs` CLI option).
 *   [sandbox] is ignored in this case.
 * - Resolves a package reference in the `@scope/name:version` form (e.g. `@atoms/simple-cv:1.0.0`), installing the package on first use, and includes its entry point.
 *   The included file's working directory is the package root. [sandbox] applies as for files.
 *
 * The context of the included file is always inherited from the main file, with an updated working directory that matches the included file's location.
 * [sandbox] defines, instead, what information is shared back to the main file's context, and how. The following modes are listed in ascending order of isolation:
 *
 * - `share` (default): exchanges information bi-directionally. Changes made in the included file's context are reflected in the main file's context, and vice versa,
 *            allowing for full sharing of variables, functions and other declarations.
 *            This is represented by [SharedContext].
 *
 * - `scope`: like `share`, but the included file's context does not share new declarations (functions and variables) back to the main file's context.
 *            This is the behavior used within lambda blocks, such as [forEach], and is represented by [ScopeContext].
 *
 * - `subdocument`: no information is shared back to the main file's context, only inherited from it. This also applies to the document info (metadata, title, etc.),
 *                  This is the behavior used for subdocuments, and is represented by [SubdocumentContext].
 *
 * @param path either a path (relative or absolute with extension) to the file to include, the name of a loadable library,
 *             or a package reference
 * @param sandbox relationship between the main context and the included file's context
 *
 * @return the content of the file as a node if a file is included, or nothing if a library is loaded
 * @throws IllegalArgumentException if the file does not exist or the loaded Quarkdown source cannot be evaluated
 * @throws com.quarkdown.core.packages.PackageResolutionException if a package reference is malformed,
 *                                                                 or the package cannot be retrieved or installed
 * @throws com.quarkdown.core.packages.UnsupportedPackageResolutionException if a package reference is used
 *                                                                            in an environment without package support
 * @permission [Permission.ProjectRead] to include files located in the project directory, including installed packages
 * @permission [Permission.GlobalRead] to include files located outside the project directory
 * @permission [Permission.PackageInstall] to download a referenced package that is not installed yet
 * @wiki including-other-quarkdown-files
 */
@QFunction
fun include(
    @Injected context: MutableContext,
    path: String,
    @LikelyNamed sandbox: ContextSandbox = ContextSandbox.SHARE,
): OutputValue<*> {
    // Load library by name if it exists.
    context.loadLibrary(path)?.let { (_, value) -> return value ?: VoidValue }

    if (PackageReferenceSyntax.isReference(path)) {
        val resolved = resolvePackage(context, path)
        return includeFile(context, resolved.entrypoint, workingDirectory = resolved.root, sandbox)
    }

    val file = file(context, path)
    return includeFile(context, file, workingDirectory = file.parent, sandbox)
}

/**
 * Includes [file] after forking the context with the given [workingDirectory] and [sandbox].
 */
private fun includeFile(
    context: MutableContext,
    file: FsEntry,
    workingDirectory: FsEntry?,
    sandbox: ContextSandbox,
): OutputValue<*> {
    val newFileSystem: FileSystem = context.fileSystem.branch(workingDirectory)
    val newContext: Context =
        when (sandbox) {
            ContextSandbox.SHARE -> SharedContext(context, newFileSystem)
            ContextSandbox.SCOPE -> ScopeContext(context, newFileSystem)
            ContextSandbox.SUBDOCUMENT -> SubdocumentContext(context, context.subdocument, newFileSystem)
        }
    return includeResource(newContext, file.readText())
}

/**
 * Resolves a package reference through the host's resolver: a local lookup first, then an installation
 * if the package is missing and the pipeline is allowed to install packages.
 * @param context context whose attached pipeline provides the resolver, the project root and the permissions
 * @param reference package reference as written in the document, e.g. `@atoms/cv:1.0.0`
 * @return the resolved package, whose entry point is readable
 * @throws PackageResolutionException if there is no project directory or the resolver fails
 * @throws com.quarkdown.core.permissions.MissingPermissionException if installation is needed but not permitted
 */
internal fun resolvePackage(
    context: Context,
    reference: String,
): ResolvedPackage {
    val projectRoot =
        context.rootFileSystem?.workingDirectory
            ?: throw PackageResolutionException(reference, "package references require a project directory")

    val resolver = context.packageResolver
    val resolved =
        resolver.locate(reference, projectRoot)
            ?: run {
                context.requirePermission(Permission.PackageInstall, "Cannot install package '$reference'")
                resolver.install(reference, projectRoot)
            }
    context.requireReadPermission(resolved.entrypoint)

    return resolved
}

/**
 * Performs a bulk include of the given paths via [include] (with the default `share` sandbox).
 * @param paths paths to the files or library names to include
 * @return a collection containing the output of the included files
 * @throws IllegalArgumentException if any of the loaded sources cannot be evaluated
 * @see include for information about file inclusion
 * @wiki including-other-quarkdown-files
 */
@QFunction
@Name("includeall")
fun includeAll(
    @Injected context: MutableContext,
    @LikelyBody paths: Iterable<Value<*>>,
): IterableValue<OutputValue<*>> =
    paths
        .map { include(context, it.asString()) }
        .let(::GeneralCollectionValue)

/**
 * Creates a link to a subdocument located at the given [path].
 *
 * This is an alias to the link syntax, `[Label](path)`, with more freedom:
 * - Function calls are supported, whereas the link syntax only supports static paths.
 * - The link syntax recognizes subdocuments only by their file extension (`.qd` or `.md`).
 *
 * @param path path to the subdocument
 * @param label optional label for the link.
 *              If not provided, this function will just add the subdocument to the document graph, without displaying a link.
 * @param anchor optional anchor to a specific section within the subdocument
 * @return a [SubdocumentLink] node, which may be hidden if [label] is not provided
 * @wiki subdocuments
 */
@QFunction
fun subdocument(
    path: String,
    label: InlineMarkdownContent? = null,
    anchor: String? = null,
): NodeValue =
    SubdocumentLink(
        Link(
            label = label?.children ?: emptyList(),
            url = path,
            title = null,
        ),
        anchor = anchor,
    ).wrappedAsValue()
