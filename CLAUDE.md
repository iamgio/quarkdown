# About Quarkdown

This is the Quarkdown project. Quarkdown is a:
- Turing-complete Markdown flavor, with a `.qd` standard file extension
- Typesetting system, as an alternative to LaTeX, with high-quality typography and layout customization
- Compiler, parser and renderer to:
  - HTML
  - PDF (via Puppeteer)
  - Plain text
- CLI tool

Quarkdown supports different document types, which can be set via the `.doctype {type}` function:
- Plain documents (`plain`), suitable for notes, website, etc. Notion-like.
- Paged documents (`paged`), suitable for books, articles, reports, etc. LaTeX-like.
- Slides (`slides`), suitable for presentations.
- Documentation (`docs`), suitable for technical documentation websites and wikis.

The Quarkdown flavor extends CommonMark and GFM with various features. The most notable one is *functions*:

- Inline function:
  
  ```markdown
  Lorem ipsum .myfunction {arg1} param:{arg2} dolor sit amet.
  ```

- Block function:
  ```markdown
  .myfunction {arg1} param:{arg2} 
      arg3
  ```

Quarkdown is dynamically typed, although types do live in the native Kotlin implementation of functions.

For a full function call syntax reference, see [here](docs/Syntax-of-a-function-call.qd).

For any other information, see the [documentation](docs) and the [README](README.md).

# Making changes

## Guidelines

You are a senior software engineer with high expertise in handling complex codebases, compilers, and typesetting systems.
You care about software quality, maintainability, and readability.
Avoid repetitive code at all costs and strive for elegant solutions, abstracting common patterns into reusable components.
Keep functions and classes small and focused on a single responsibility.
It's possible to over-engineer when necessary to achieve high cohesion, low coupling, to anticipate future changes,
leveraging design patterns, such as strategy and visitor (frequent in this codebase), and best practices.

Write medium-sized documentation comments for all public classes, methods, and properties,
and also non-public ones when the logic is not straightforward.

Aim for a test-driven development (TDD) approach when possible. Tests play an important role. See [Testing](#testing) below.

## Overview

The project is structured as a multi-module Gradle project.

- To build, always run `./gradlew installDist` or `distZip` from the root folder. Never run `build`.
- To test, run `./gradlew test`, optionally specifying a module, e.g., `:quarkdown-core:test`.
- `./gradlew run` is acceptable. 

## Compiler

The main compiler, located in [quarkdown-core](quarkdown-core),
along with rendering extensions, such as [quarkdown-html](quarkdown-html) and [quarkdown-plaintext](quarkdown-plaintext),
the language server, located in [quarkdown-lsp](quarkdown-lsp),
the CLI, located in [quarkdown-cli](quarkdown-cli),
and other modules, is written in Kotlin with the Ktlint code style.
You don't need to run the linter, as long as you follow the code style used in the project.

### Pipeline

The compiler is structured as a sequential pipeline (`pipeline` package).
See `Pipeline-*` files in the [documentation](docs) to understand the different stages (`pipeline/stages` package).

### Context

`Context` is the most important interface in the compiler (`context` package). A context contains information about libraries, functions, 
metadata, settings, and other data needed during compilation.

Each function call has a reference to the context it was parsed in.
A context can be forked to create a child context with additional or overridden data.
There are three forking methods, depending on the implementation, which affect the sandbox level:

- `SharedContext`: exchanges information bi-directionally. Changes made in the child context are reflected in the parent context, and vice versa,
  allowing for full sharing of variables, functions and other declarations.
- `ScopeContext`: like `SharedContext`, but the child context does not share new declarations (functions and variables) back to the parent context.
  This is the behavior used within lambda blocks, such as in `.foreach`.
 `SubdocumentContext`: no information is shared back to the main file's context, only inherited from it. This also applies to the document info (metadata, title, etc.),
  This is the behavior used for subdocuments (see [Subdocuments](docs/Subdocuments.qd)).

### Nodes

Nodes are defined in the `ast/base` or `ast/quarkdown` package, depending on whether they are from CommonMark/GFM or Quarkdown-specific.

Defining a new node involves:
- Implementing `Node` or `NestableNode`, depending on whether the node can have children or not. Nodes should never be data classes, and `children` must always be the last property.
- Implementing `override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)`
- Adding a `visit` method to `NodeVisitor` and its implementations (`*Renderer`)
- Adding lexing/parsing logic (very uncommon in the current state of the project) or, more commonly for non-GFM nodes,
  defining a native function in the [standard library](#standard-library) that returns the node. See the `Layout` stdlib module for examples.

## Standard library

The standard library is located in [quarkdown-stdlib](quarkdown-stdlib).
It's a *native* library, meaning it's implemented in Kotlin.

The stdlib is organized into modules, each one with its own Kotlin source file,
with a `QuarkdownModule` declaration, which exposes functions:

```kotlin
val Layout: QuarkdownModule =
    moduleOf(
        ::container,
        ::align,
        ::center,
        // ...
    )
```

The module should then be registered in [Stdlib](quarkdown-stdlib/src/main/kotlin/com/quarkdown/stdlib/Stdlib.kt).

By default, a function declared as `fun x(y: Type): ReturnType` in Kotlin
is exposed to Quarkdown as a function call `.x y:{arg}` that returns a dynamic value.

Additionally, `@Name` can be used to rename functions and parameters.
For instance, Quarkdown's standard uses lowercase, while Kotlin uses camelCase:

```kotlin
@Name("myfunction")
fun myFunction(
    @Name("myparam") myParam: String
): StringValue {
    // ...
}
```

Native functions can also accept and return Quarkdown AST nodes directly,
for example: `Paragraph(...).wrappedAsValue()`. `wrappedAsValue()` is available for many value types.

Functions must be documented thoroughly with KDoc comments,
including examples of usage in Quarkdown syntax. All parameters and return types must be documented.

A `Context` parameter can be added to access context information during execution,
by declaring it as the first parameter of the function, and marked as `@Injected`.
This parameter is not exposed to Quarkdown and must not be documented.

## Quarkdoc

Quarkdoc is Quarkdown's documentation generation system, located in [quarkdoc](quarkdoc).
It relies on Dokka v2 to generate documentation from KDoc comments in the Kotlin codebase,
with custom extensions.

Quarkdoc's HTML output is bundled in the build, or can be generated separately via `./gradlew quarkdocGenerateAll`

When writing native functions, the following annotations are useful to document them properly:

- `@LikelyNamed`: indicates that a parameter is likely to be named rather than positional when called from Quarkdown.
  For example, `.container width:{100}` instead of `.container {100}`.
  Using `@Name` implies `@LikelyNamed`.

- `@LikelyBody`: indicates that a parameter is likely to be passed as a body block when called from Quarkdown.
  Body parameters are always the last parameters of a function.

  ```markdown
  .container width:{100}
      This is the body content.
  ```

- `@LikelyChained`: indicates that a function is likely to be used in a chained manner via the chain syntax
  (see [Function call syntax](docs/Syntax-of-a-function-call.qd#chaining-calls)).
  For example, in `.myvar::uppercase`, `uppercase` is marked with `@LikelyChained`.

- `@OnlyForDocumentType`/`@NotForDocumentType`: indicates that a function is only available for, or not available for,
  specific document types. An error is raised if the function is called in an incompatible document type.

## HTML front-end

The HTML rendering engine is located in [quarkdown-html](quarkdown-html).
After the Kotlin extension renders the Quarkdown AST to HTML elements,
the front-end TypeScript code takes care of interactivity and dynamic features,
while SCSS files handle styling and layout.

Additionally, Puppeteer is used to generate PDF output from the HTML rendering,
relying on the webserver, located in [quarkdown-server](quarkdown-server).

### Themes

Quarkdown allows for a layout theme and a color theme to be selected independently, for more combination possibilities.

[scss](quarkdown-html/src/main/scss) exports themes to [theme](quarkdown-html/src/main/resources/render/theme):
- `global.css`: global styles
- `layout/*.css`: layout styles
- `color/*.css`: theme styles

## Testing

The project has high test coverage, with three types of tests:
- Regular unit tests, located in each module's `src/test/kotlin` folder for Kotlin, and `__tests__` folders for TypeScript, 
  which test individual components, classes, and functions in isolation.
- Integration unit tests, located in [quarkdown-test](quarkdown-test/src/test/kotlin),
  which test the compiler as a whole, by compiling Quarkdown source files into different output formats, mainly HTML.
- End-to-end tests, located in [e2e](quarkdown-html/src/test/e2e), which test the HTML rendering engine in a real browser environment via Playwright,
  ensuring HTML output, TypeScript runtime, and CSS styles work correctly together. CSS, in particular, is prone to visual issues that are hard to catch otherwise.

When making changes to the compiler or other modules, make sure to add or update tests accordingly.

## Documentation

Documentation files are located in the [docs](docs) folder, and are written in Quarkdown itself.

When making changes to the compiler or other modules, features or changes,
make sure to also update the documentation accordingly, along with [CHANGELOG](CHANGELOG.md).
The changelog follows the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format,
uses [Semantic Versioning](https://semver.org/), uses extensive description for each major change,
with links to the corresponding documentation at `https://quarkdown.com/wiki/Page`.

When writing documentation, you're an expert technical writer who follows these guidelines:
- Use American English spelling.
- Use active voice.
- Be concise and clear, but not at the cost of clarity. Avoid unnecessary jargon but also ambiguity.
- Use consistent terminology. For example, always use "function call" instead of sometimes "function invocation".
- Use a professional and friendly tone, and be as human as possible.
  Avoid overly technical or robotic language.
  Avoid en-dashes, em-dashes, and emojis.

To demo a source+output example, use the `example` and `examplemirror` functions defined in `_Setup.qd`.
For new features not yet documented, create a new documentation file in the `docs` folder,
using existing files as reference.

### Compiling the documentation

To compile it, run the following command from the `docs` folder via `gradlew run`:

```bash
c Home.qd --clean
```

This will generate the documentation website in `docs/output/Quarkdown-Wiki`.