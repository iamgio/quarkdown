# Changelog

## [Unreleased]

### Fixed

&nbsp;

#### Fixed inline code inside compact footnote definitions

Compact footnotes like `` [^: text with `code`] `` were incorrectly parsed as plain text instead of footnotes. The definition pattern now allows backtick-enclosed spans inside the footnote body.

Thanks @Shreyansh-Kushwaha!

## [2.0.1] - 2026-05-04

### Added

 

#### [Execution timeout](https://quarkdown.com/wiki/cli-options#other-options)

The new `--timeout` flag sets the maximum time, in seconds, allowed for the entire program execution to complete. Defaults to 30 seconds; `0` disables the timeout.

```shell
quarkdown c main.qd --pdf --timeout 120
```

 

#### New locales

New locales have been added: Polish, Portuguese, Russian and Ukrainian.

Thanks @emsspree!

 

### Changed

 

#### Added link-to-root in new docs projects

When creating a `docs` project, `quarkdown create` now links the top-left title to the root document, for easier navigation back to the home page.

 

### Fixed

 

#### Fixed trailing `\` in Quarkdoc multiline signatures

When a function signature in Quarkdoc was long enough to split across multiple lines, the last parameter line incorrectly displayed a trailing `\` before the return type. 

* * *

### Sponsors

Thanks to our sponsors! 🎉

@vitto4

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>

## [2.0.0] - 2026-04-23

### Added

 

#### [Permission system](https://quarkdown.com/wiki/cli-compiler#permissions)

Quarkdown's permission system controls what a document can access during compilation, for increased safety.
If the compiler attempts an action that requires a permission it doesn't have, an error is raised.

You can grant or revoke permissions with the `--allow` and `--deny` flags:

```shell
quarkdown c main.qd --allow global-read --deny native-content
```

Available permissions: `project-read` (default), `global-read`, `network`, `native-content` (default), `all`. See the wiki page for more details.

 

#### [Line continuation in function calls](https://quarkdown.com/wiki/syntax-of-a-function-call#line-continuation)

A backslash (`\`) at the end of a line lets you split a function call's arguments across multiple lines.
This improves readability for calls with many parameters:

```markdown
.container alignment:{center} \
           background:{red} \
           padding:{1px}
```

 

#### [HTML static assets](https://quarkdown.com/wiki/html-static-assets)

A `public/` directory in your project root lets you ship arbitrary files, such as `robots.txt` or `CNAME`, alongside the compiled output. Quarkdown copies the entire contents into the root of the output directory, preserving the original structure, without any processing.

 

#### [HTML options, sitemap and canonical links](https://quarkdown.com/wiki/html-options)

The new `.htmloptions` function configures HTML-specific generation settings. It accepts a `baseurl` parameter which, when set:

-   Emits a `<link rel="canonical">` tag in each page's `<head>`, pointing to the canonical URL of the root document or subdocument.
-   Generates a `sitemap.xml` with absolute URLs for the root and all subdocuments, enabling search engine discovery.

```markdown
.htmloptions baseurl:{https://quarkdown.com/wiki}
```

 

#### [Keybindings](https://quarkdown.com/wiki/keybindings)

The new `.keybinding {keys}` function displays a keyboard shortcut as styled key labels.
Modifier keys are platform-aware: on macOS, they automatically display native symbols (e.g. `⌘` instead of `Ctrl`).

```markdown
Press .keybinding {Mod+Shift+K} to delete the line.
```

 

#### [Linked cross-references for all referenceable types](https://quarkdown.com/wiki/cross-references)

Cross-references (`.ref`) to figures, tables, code blocks, math equations, and custom numbered blocks are now rendered as clickable links that navigate to the referenced element. Previously, only heading references were linked.

 

#### Root path symbol (`@`) in links and images

When compiling to HTML, the `@` symbol at the start of a URL resolves to the root of the output, where the main HTML file is located. This makes it easy to reference shared assets from any subdocument, particularly useful in combination with the new `public/` directory.

```markdown
[Home](@)

![Logo](@/assets/logo.png)
```

In the previous example, `@/assets/logo.png` resolves to `./assets/logo.png`, which is where `public/assets/logo.png` is copied from the source to the output.

This `@` symbol is internally called _media passthrough prefix_, as it prevents the media storage system from registering the path as a media reference, and instead treats it as a passthrough to be copied verbatim.

 

#### [`.image` primitive function](https://quarkdown.com/wiki/primitives#images)

The new `.image` function creates images with fine-grained control over their properties, including media storage opt-out. The `mediastorage:{no}` parameter lets an image reference a fixed relative path, useful in combination with the new `public/` directory for shared assets.

```markdown
.image {photo.jpg} label:{A photo} title:{A caption} width:{200px} mediastorage:{no}
```

 

### Changed

 

#### Changed default output directory to `./quarkdown-output` (breaking change)

The default output directory (`--out`) was changed from `./output` to `./quarkdown-output` to avoid conflicts and ambiguity. If you were relying on the old default, please update your workflow accordingly or compile with `--out ./output` explicitly.

 

#### Fully offline HTML output

HTML documents now render entirely offline. Assets such as fonts, opt-in libraries and code highlighting themes, are now bundled in the Quarkdown installation and copied to each generated document. Previously, the output relied on CDNs and Google Fonts, which meant that opening a document without an internet connection could lead to broken styling and missing features.

With this change, output directory size is larger, but it comes with more predictable rendering and significantly faster page loads. First-time compilations may take slightly longer due to the additional copying step, but subsequent compilation times should be unaffected thanks to checksum validation.

As before, opt-in libraries, such as Mermaid and KaTeX, are still only included if used in the document, so they don't affect performance or output size if not needed.

> [!NOTE]
> Due to the excessive file size, Chinese-specific fonts (loaded from `.doclang {zh}`) are still loaded remotely.

> [!NOTE]
> User-picked fonts from Google Fonts are still loaded remotely.

 

#### Parallel rendering

Rendering now runs in parallel across sibling elements, improving performance on large documents.

 

#### Static preview output file name (breaking change)

When launching with `--preview` (without `--out-name`), the output directory name no longer matches `.docname`, because its dynamic nature may easily break the preview. Instead, it's now `preview-<mainfile>-<hash>`.  
In order to get the `.docname`-based output name, consider compiling without `--preview`.

 

#### Lightweight media storage IO

IO over the media storage system is now more lightweight and secure, as media files are now copied by reference rather than by content. Each media export now also comes with a checksum that's validated on subsequent exports to avoid unnecessary copying.

 

#### Renamed `Injection` stdlib module to `Html`

The `Injection` module, which contains functions related to raw HTML injection, was renamed to `Html`.
This change breaks previous references to the documentation of the module and its functions.

 

### Fixed

 

#### Fixed live preview flashing with dark themes

Fixed an issue that caused live preview to display white flashes when using dark color themes, during the crossfade transition.
The transition background now matches the target's background color.

 

#### Fixed broken wiki links in Quarkdoc

Wiki links at [/docs](https://quarkdown.com/docs) now correctly point to the new wiki.

* * *

### Sponsors

Thanks to our sponsors! 🎉

@vitto4

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>

## [1.15.1] - 2026-03-31

### Changed

#### Subdocument link to nonexistent file now produces a visual error

A subdocument link pointing to a nonexistent file now produces a clear visual error in the output document, rather than silently rendering `[???]`.

#### Reduced code block font size on small screens

Code blocks in `plain` and `docs` documents now use a smaller font size when viewed on small screens, improving readability on mobile devices.

#### Plain documents are start-aligned on small screens

Text alignment in `plain` documents switches from justified to start-aligned on small screens.

#### Reduced caption font size on small screens

Captions for figures and tables in `plain` and `docs` documents now use a smaller font size on small screens.

#### Upgraded to Reveal.js 6

Upgraded Reveal.js, the library powering `slides` documents, to v6.0.0. 
No breaking changes are expected in the rendered output.

### Fixed

#### Split paragraphs in `paged` documents now justify the last line correctly

When a paragraph is auto-split across a page break in a `paged` document, the last line of the first part is now correctly justified, rather than start-aligned.

Thanks @OverSamu!

#### Fixed live preview not updating when an anchor is present

Fixed an issue that caused live preview to skip updates when navigating to an anchor link of the same document, e.g. from the navigation sidebar.

Thanks @OverSamu!

#### Fixed `.filetree`'s 'ellipsis' text color

Ellipsis (`...`) items in file trees now display the correct color.

Thanks @cyphercodes!

#### Fixed captions not following global line spacing

Captions now follow the global configuration for line spacing and letter spacing.

#### Fixed Quarkdoc `Wiki page` broken links if they contain anchors

The `@wiki` documentation tag now correctly preserves `#` anchor separators in wiki URLs.

* * *

### Sponsors

Thanks to our sponsors! 🎉

@vitto4

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>

## [1.15.0] - 2026-03-24

### Added

#### [CSL bibliography styles](https://quarkdown.com/wiki/bibliography) (breaking change)

Quarkdown's internal bibliography management is now powered by [CSL](https://citationstyles.org) (Citation Style Language).

-   A curated selection of citation styles from the [CSL Style Repository](https://github.com/citation-style-language/styles) is now supported. The `style` parameter now accepts a CSL style identifier (e.g. `ieee`, `apa`, `chicago-author-date`, `nature`). The default style is now `ieee`.

    **Breaking change:** `plain` and `ieeetr` styles do not exist anymore, and have been replaced by `ieee`.

-   Along with BibTeX (`.bib`) files, the following file formats are now accepted:
    -   CSL JSON (`.json`)
    -   YAML (`.yaml`/`.yml`)
    -   EndNote (`.enl`)
    -   RIS (`.ris`)

-   Rendered bibliography entries are now localized to the document locale, set via `.doclang`.

#### [Multi-key citations](https://quarkdown.com/wiki/bibliography#citations)

`.cite` now accepts a comma-separated list of keys (e.g. `.cite {einstein, hawking}`) to produce a single combined citation label, whose format depends on the active citation style (e.g. `[1], [2]` for IEEE, `(Einstein, 1905; Hawking, 1988)` for APA).

#### Formatted captions

Captions for all supported elements now accept inline formatting (including inline function calls), rather than plain text. 

```markdown
![Pi](pi.png "The symbol of *pi*, which approximately equals .pi")
```

<img width="500" alt="Formatted caption" src="https://github.com/user-attachments/assets/589241b1-9273-41fa-9ffc-54104671a389" />

#### [Scoped page formatting](https://quarkdown.com/wiki/page-format#scoped-formatting)

`.pageformat` now supports scoping formats to specific pages in `paged` documents via two combinable parameters:

-   `side` (`left` or `right`): restricts formatting to recto or verso pages, enabling mirrored margins and other asymmetric layouts.
-   `pages` (e.g. `2..5`): restricts formatting to an inclusive range of page indices.

```markdown
.pageformat size:{A4}
.pageformat side:{left} margin:{2cm 3cm 2cm 1cm}
.pageformat side:{right} margin:{2cm 1cm 2cm 3cm}
```

```markdown
.pageformat pages:{1..3} borderbottom:{4px}
```

#### New syntax: [Tight function calls](https://quarkdown.com/wiki/syntax-of-a-function-call#tight-function-calls)

Inline function calls can now be wrapped in curly braces to delimit them from surrounding content, without relying on whitespace.

```markdown
abc{.uppercase {def}}ghi
```

#### [`.heading` primitive function](https://quarkdown.com/wiki/headings)

The new `.heading` function creates headings with granular control over their behavior, unlike standard Markdown headings (`#`, `##`, ...).
It allows explicit control over numbering (`numbered`), table of contents indexing (`indexed`), page breaks (`breakpage`), depth, and reference ID (`ref`).

#### [`.pagebreak` primitive function](https://quarkdown.com/wiki/page-break)

The new `.pagebreak` function provides an explicit way to insert a page break as an alternative to the `<<<` syntax.

#### [File tree](https://quarkdown.com/wiki/file-tree)

The new `.filetree` function renders a visual file tree from a Markdown list.

```markdown
.filetree
    - src
      - main.ts
      - ...
    - README.md
```

Bold entries (`**name**`) are highlighted with a distinct background color, useful for drawing attention to specific items.

```markdown
.filetree
    - src
      - **main.ts**
      - utils.ts
    - README.md
```

#### [Better heading configuration for table of contents and bibliography](https://quarkdown.com/wiki/table-of-contents)

Both `.tableofcontents` and `.bibliography` now accept the following optional parameters to control the heading that precedes them:

-   `breakpage`: controls whether the heading triggers an automatic page break.
-   `headingdepth`: the depth of the heading (1-6).
-   `numberheading`: controls whether the heading is numbered in the document hierarchy.
-   `indexheading`: when enabled, the heading is included in the document's own table of contents.

#### [Subscript and superscript text](https://quarkdown.com/wiki/text)

The `.text` function now accepts a `script` parameter with `sub` and `sup` values for subscript and superscript text.

### Changed

#### Removed `includeunnumbered` parameter from `.tableofcontents` (breaking change)

The `includeunnumbered` parameter has been removed, in favor of the more granular heading configuration previously mentioned.
Now all indexable headings are included in the ToC by default, regardless of their numbering.

#### `.container`'s `margin` now suppresses children's margins

When an explicit margin is applied to a `.container`, it now suppresses the margins of its direct children, for a more intuitive and flexible layout configuration.

#### `.fullspan` now relies on `.container`

`.fullspan`, used to create a block spanning over multiple columns in a multi-column layout, is now shorthand for `.container fullspan:{yes}`.

### Fixed

#### Stabilized multi-column layout

The [multi-column layout](https://quarkdown.com/wiki/multi-column-layout) via `.pageformat columns:{N}` is no longer experimental, and now works reliably across all document types.

#### Added call stack limit

Infinite recursion in function calls is now detected and reported as a clear error.

#### Fixed default browser not opening on Linux (Wayland and XDG environments)

On Linux systems where the Java AWT Desktop API does not support the BROWSE action (e.g., Wayland), `--browser default` now falls back to `xdg-open` automatically.
Additionally, `--browser xdg` is now a supported named choice for the `--browser` CLI option.

Thanks @szy1840!

#### Fixed scroll position not fully restored during live preview on long paged documents

When editing long paged documents with live preview, the scroll position could sometimes be restored only partially because of long paged.js load times. The swap now reliably waits for the content to be fully loaded.

#### Fixed Mermaid diagrams preventing page breaks

Fixed an issue that caused Mermaid diagrams in `paged` documents to cause subsequent content to overflow instead of being pushed to the next page.

#### Fixed tree traversal not reaching non-body nodes

Fixed an issue that caused tree traversal-dependent features, such as cross-references, to not work in titles of `.box` and `.collapse`, and in block quote attributions.

#### Improved lexer performance

The lexer has been optimized to reduce regex builds to a minimum, resulting in significantly improved performance for large documents.

* * *

### Sponsors

Thanks to our sponsors! 🎉

@vitto4

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>

## [1.14.1] - 2026-03-06

### Added

#### [Escaped characters in numbering formats](https://quarkdown.com/wiki/numbering)

A backslash (`\`) in a numbering format string now escapes the next character, treating it as a fixed symbol. For example, `\1` produces a literal `1` instead of a decimal counter.

### Fixed

#### Fixed live preview sometimes timing out on Windows

Fixed an IPv6-related issue that caused connections to Quarkdown's server to time out on Windows. _Please also update to the latest version of the VS Code extension to v1.1.2 or later._

#### Fixed block function call incorrectly matching lines with trailing content

Fixed an issue that caused a line like `.sum {1} {2} .sum {3} {4}` to be incorrectly lexed as two block function calls rather than a single paragraph with two inline function calls.

### Changed

#### Improved lexer performance

The lexer no longer restarts its regex search from scratch when a function call advances the scan position, resulting in slightly improved performance, especially for documents with many function calls.

## [1.14.0] - 2026-02-19

This version is the biggest release to date, with a large number of new features and improvements, and a [new official wiki](https://quarkdown.com/wiki), written in Quarkdown, that fully replaces the GitHub wiki for a better experience.

> Going forward, next minor releases will be smaller and more frequent.

### Added

#### [`docs` document type](https://quarkdown.com/wiki/document-types#docs-docs)

`docs` is the fourth document type available in Quarkdown, alongside `plain`, `paged` and `slides`. It is designed for technical documentation, wikis and knowledge bases.

It derives from `plain`, and adds a customizable navigation sidebar, a ToC sidebar, a header, accurate client-side search, and next/previous page navigation buttons.

You can see it in action in the [new official wiki](https://quarkdown.com/wiki)! To get started with a new `docs` document, you can rely on `quarkdown create` as usual.

#### New themes: Galactic (color) and Hyperlegible (layout)

Inspired by Astro, this new theme combination is the one used in the new wiki for improved readability and modern look.

<img width="606" height="350" alt="Galactic+Hyperlegible" src="https://github.com/user-attachments/assets/d8609d22-fb57-4db9-8b4a-98fa28cda421" />

#### [GitHub-style alerts](https://quarkdown.com/wiki/quote-types)

GitHub's alert syntax is now supported, making it easier to migrate from other tools:

```markdown
> [!NOTE]
> This is a note
```

Note that Quarkdown's original syntax is still supported _and recommended_, especially for English documents:

```markdown
> Note: This is a note
```

#### [Subdocument links now allow anchors](https://quarkdown.com/wiki/subdocuments)

Links to Quarkdown subdocuments now support anchors, to link to specific sections:

```markdown
[Page](page.qd#section)
```

#### [Customizable page numbering format](https://quarkdown.com/wiki/page-counter#formatting-the-page-number)

The `.formatpagenumber {format}` function overrides the page numbering format from the current page onward. It accepts the same format specifiers as `.numbering`, and applies to both page counters and table of contents.

```markdown
.pagemargin {topcenter}
    .currentpage

# First page

.formatpagenumber {i}

# Second page

# Third page
```

<img width="550" alt="Page number format example" src="https://raw.githubusercontent.com/iamgio/quarkdown/main/docs/page-counter/format.png" />

Thanks @OverSamu!

#### [Horizontal/vertical gap customization of `.grid`](https://quarkdown.com/wiki/stacks#parameters)

The `.grid` function now accepts `hgap` and `vgap` parameters to customize the horizontal and vertical gaps between grid items. `gap` still works as a shorthand for both.

Thanks @OverSamu!

#### [`none` is now converted to `null`](https://quarkdown.com/wiki/none#passing-none-to-functions)

When invoking a native function from the stdlib, [`none`](https://quarkdown.com/wiki/none) is now supported by nullable parameters, and converted to `null`.

Before:

```markdown
.function {rectangle}
    width height background?:
    .if {.background::isnone}
        .container width:{.width} height:{.height}
    .ifnot {.background::isnone}
        .container width:{.width} height:{.height} background:{.background}
```

After:

```markdown
.function {rectangle}
    width height background?:
    .container width:{.width} height:{.height} background:{.background}
```

#### [Icons](https://quarkdown.com/wiki/icons)

The new `.icon {name}` function relies on [Bootstrap Icons](https://icons.getbootstrap.com/#icons) to display pixel-perfect icons in your documents.

```markdown
Quarkdown is on .icon {github}
```

#### New output target: plain text

Quarkdown can now render to plain text (`.txt`) via `--render plaintext`.

This has no particular use case. It was needed to implement the docs search feature in the first place.

#### Get path to root directory

The new `.pathtoroot {granularity?}` function returns the relative path from the current source file to the parent directory of:

-   the root document, if `granularity` is `project` (default)
-   the subdocument, if `granularity` is `subdocument`

### Changed

#### `.css` doesn't require `!important` anymore

The `.css` function now applies `!important` automatically at the end of each rule.

#### Revised navigation sidebar

The navigation sidebar, visible in `plain` and `paged` documents on web view, is now easier to navigate, with all entries visible at once, and more accessible for screen readers.

<img width="208" height="632" alt="Sidebar" src="https://github.com/user-attachments/assets/54956e66-db34-486f-bb9a-275271ae7a7e" />

Additionally, its generation is now performed at compile time rather than runtime, providing major performance improvements for large documents.

#### Flexible naming strategy for subdocument output files

`--no-subdoc-collisions` was removed in favor of `--subdoc-naming <strategy>`, which is a flexible way to choose how subdocument output files are named:

-   `file-name` (default): each subdocument output file is named after its source file
-   `document-name`: each subdocument output file is named after its `.docname` value
-   `collision-proof`: former `--no-subdoc-collisions`

#### Revamped `create` CLI

The `quarkdown create` command is now more intuitive, for a smoother onboarding experience.

#### Libraries now include content

`.include {library}` now also includes top-level Markdown content from the library, just like `.include {file.qd}` does for regular files.

#### Page content border adjustments

Page content border (`.pageformat bordercolor`) is now supported in `plain` documents, and refined for `slides` documents, especially in PDF output.

#### Improved code diff styling

Code blocks using the `diff` language now have improved and clearer styling for added and removed lines.

### Fixed

#### Major improvements to live preview

Live preview has undergone major performance improvements and increased reliability, especially in combination with the new VS Code extension update.

Live reloading not being performed when editing subdocuments has also been fixed.

#### Fixed subdocument resolution from included files

Linking to subdocuments from files included via `.include` from a different directory now correctly resolves the subdocument path.

#### Fixed unresolved reference of local variables in body arguments

The following snippet used to cause an unresolved reference error for `y`:

```markdown
.function {a}
    x:
    .x

.function {b}
    y:
    .a
        .y

.b {hello}
```

#### Fixed paragraph spacing with floating element

Fixed an issue that caused no spacing to be present between two paragraphs if a floating element was in-between, via `.float`.

#### Fixed ToC with no level 1 headings

Table of contents are no longer empty if no level 1 headings are present, or if all are decorative.

#### Fixed line spacing in table cells

Table cells now correctly apply the same line spacing as paragraphs and lists.

* * *

### Sponsors

Shout out to our sponsors! 🎉

@vitto4

@serkonda7

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>

[Unreleased]: https://github.com/iamgio/quarkdown/compare/v2.0.1...HEAD

[2.0.1]: https://github.com/iamgio/quarkdown/compare/v2.0.0...v2.0.1

[2.0.0]: https://github.com/iamgio/quarkdown/compare/v1.15.1...v2.0.0

[1.15.1]: https://github.com/iamgio/quarkdown/compare/v1.15.0...v1.15.1

[1.15.0]: https://github.com/iamgio/quarkdown/compare/v1.14.1...v1.15.0

[1.14.1]: https://github.com/iamgio/quarkdown/compare/v1.14.0...v1.14.1

[1.14.0]: https://github.com/iamgio/quarkdown/compare/36ef163d22c13e51edfca12739b99aa6aa1368b4...v1.14.0
