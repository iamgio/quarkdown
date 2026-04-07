
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.10"
    id("com.github.node-gradle.node") version "7.1.0"
    id("io.miret.etienne.sass") version "1.6.0"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.6")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
    implementation(project(":quarkdown-plaintext")) // For search index generation
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}

/*
Third-party library bundling

Libraries from node_modules are copied into build/thirdparty/
at Gradle build time, so they are included in the distribution archive
and loaded from the filesystem at runtime.

To add a new library:
1. Add the npm package to package.json
2. Add a LibrarySpec entry to `librariesToBundle` (or to `fontsourcePackages` for fonts)
3. For non-font libraries, add a ThirdPartyLibrary subclass in ThirdPartyLibrary.kt
*/

val thirdPartyDir =
    layout.buildDirectory
        .dir("thirdparty")
        .get()
        .asFile
val nodeModules = projectDir.resolve("node_modules")
val scssThirdParty = projectDir.resolve("src/main/scss/thirdparty")
val staticFontsDir = projectDir.resolve("src/main/fonts")

/**
 * Declarative specification for copying a library from `node_modules` into `build/thirdparty/`.
 * Multiple specs may share the same [target] (their files are merged into one directory).
 *
 * @param target output directory name under `build/thirdparty/`
 * @param source path relative to `node_modules/`
 * @param includes glob patterns to select files (empty = everything)
 */
data class LibrarySpec(
    val target: String,
    val source: String,
    val includes: List<String> = emptyList(),
)

/**
 * All libraries to copy from `node_modules` into the distribution.
 * To add a new library, append a `LibrarySpec` entry here.
 */
val librariesToBundle =
    listOf(
        LibrarySpec("bootstrap-icons", "bootstrap-icons/font", listOf("bootstrap-icons.min.css", "fonts/bootstrap-icons.woff2")),
        LibrarySpec("highlight.js", "highlightjs-line-numbers.js/dist", listOf("highlightjs-line-numbers.min.js")),
        LibrarySpec("highlight.js", "highlightjs-copy/dist", listOf("highlightjs-copy.min.js", "highlightjs-copy.min.css")),
        LibrarySpec("katex", "katex/dist", listOf("katex.min.css", "katex.min.js", "fonts/*.woff2")),
        LibrarySpec("mermaid", "mermaid/dist", listOf("mermaid.min.js")),
        LibrarySpec("reveal.js", "reveal.js/dist", listOf("reveal.js", "reset.css", "reveal.css", "theme/white.css", "plugin/notes.js")),
        LibrarySpec("pagedjs", "pagedjs/dist", listOf("paged.polyfill.js")),
    )

/**
 * @fontsource packages to bundle. Each is placed in its own `build/thirdparty/fonts/{name}/` directory
 * with auto-discovered latin woff2 variants and a generated `fonts.css`.
 * SCSS layout themes select which fonts they need via `@import url(...)`.
 */
val fontsourcePackages =
    listOf("lato", "inter", "noto-sans-mono", "source-sans-pro", "fira-sans")

/**
 * Highlight.js theme CSS files to copy as SCSS partials for compile-time inlining into color themes.
 * Key: source path relative to `node_modules/highlight.js/styles/`.
 * Value: output SCSS partial filename (without leading `_` or `.scss` extension).
 */
val hljsScssPartials =
    mapOf(
        "default.css" to "hljs-default",
        "atom-one-dark.min.css" to "hljs-atom-one-dark",
    )

/**
 * Bundles highlight.js with all common languages into a single browser-ready file,
 * since the npm package is modular and does not include a pre-built bundle.
 */
val bundleHighlightJs =
    tasks.register<NpxTask>("bundleHighlightJs") {
        group = "build"
        description = "Bundles highlight.js into a single browser-ready file"
        dependsOn(tasks.npmInstall)

        command.set("esbuild")
        args.set(
            listOf(
                "node_modules/highlight.js/lib/common.js",
                "--bundle",
                "--platform=browser",
                "--format=iife",
                "--global-name=hljs",
                "--minify",
                "--outfile=${thirdPartyDir.resolve("highlight.js/highlight.min.js")}",
            ),
        )
    }

/**
 * Main bundling task: copies libraries, bundles fontsource fonts,
 * and copies hljs SCSS partials.
 */
val bundleThirdParty =
    tasks.register<DefaultTask>("bundleThirdParty") {
        group = "build"
        description = "Bundles third-party libraries from node_modules into the distribution"
        dependsOn(tasks.npmInstall)
        dependsOn(bundleHighlightJs)

        doLast {
            librariesToBundle.forEach { (target, source, includes) ->
                copy {
                    from(nodeModules.resolve(source)) {
                        if (includes.isNotEmpty()) include(includes)
                    }
                    into(thirdPartyDir.resolve(target))
                }
            }

            fontsourcePackages.forEach { fontName ->
                bundleFontsource("fonts/$fontName", listOf(fontName))
            }

            // Copy static (non-npm) font files from the source tree.
            copy {
                from(staticFontsDir)
                into(thirdPartyDir.resolve("fonts"))
            }

            scssThirdParty.mkdirs()
            hljsScssPartials.forEach { (source, partialName) ->
                nodeModules
                    .resolve("highlight.js/styles/$source")
                    .copyTo(scssThirdParty.resolve("_$partialName.scss"), overwrite = true)
            }
        }
    }

/**
 * Auto-discovers latin-subset woff2 files from the given @fontsource packages,
 * copies them into the given [targetSubdir] under `build/thirdparty/`, and generates a `fonts.css`
 * with `@font-face` declarations derived from the file naming convention.
 *
 * @fontsource files follow the pattern `{font}-latin-{weight}-{style}.woff2`,
 * from which font-weight and font-style are extracted automatically.
 */
fun bundleFontsource(
    targetSubdir: String,
    fontNames: List<String>,
) {
    val targetDir = thirdPartyDir.resolve(targetSubdir)
    val cssBuilder = StringBuilder()

    // Matches e.g. "lato-latin-400-normal.woff2" -> (lato, 400, normal)
    val variantPattern = Regex("""^(.+)-latin-(\d+)-(\w+)\.woff2$""")

    for (fontName in fontNames) {
        val filesDir = nodeModules.resolve("@fontsource/$fontName/files")
        val family = fontName.split("-").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

        filesDir
            .listFiles()
            ?.filter { it.name.startsWith("$fontName-latin-") && it.extension == "woff2" }
            ?.sortedBy { it.name }
            ?.forEach { sourceFile ->
                val match = variantPattern.matchEntire(sourceFile.name) ?: return@forEach
                val (_, weight, style) = match.destructured

                sourceFile.copyTo(targetDir.resolve(sourceFile.name), overwrite = true)

                cssBuilder.appendLine(
                    """
                    |/* $fontName-latin-$weight-$style */
                    |@font-face {
                    |  font-family: '$family';
                    |  font-style: $style;
                    |  font-display: swap;
                    |  font-weight: $weight;
                    |  src: url('./${sourceFile.name}') format('woff2');
                    |}
                    """.trimMargin(),
                )
                cssBuilder.appendLine()
            }
    }

    targetDir.resolve("fonts.css").writeText(cssBuilder.toString())
}

// SCSS and TypeScript bundling

val scssDir = projectDir.resolve("src/main/scss")
val themeOutputDir = projectDir.resolve("src/main/resources/render/theme")

tasks.compileSass {
    sourceDir = scssDir
    outputDir = themeOutputDir
    dependsOn(bundleThirdParty) // hljs SCSS partials must be copied before SASS compilation

    // Copy layout theme JSON manifests (font dependencies) alongside the compiled CSS.
    doLast {
        copy {
            from(scssDir.resolve("layout")) { include("*.json") }
            into(themeOutputDir.resolve("layout"))
        }
    }
}

val bundleTypeScript =
    tasks.register<NpxTask>("bundleTypeScript") {
        group = "build"
        description = "Bundles TypeScript files using esbuild"

        dependsOn(tasks.npmInstall)

        command.set("esbuild")
        args.set(
            listOf(
                "src/main/typescript/index.ts",
                "--bundle",
                "--platform=browser",
                "--format=iife",
                "--outfile=src/main/resources/render/script/quarkdown.js",
                "--external:reveal.js",
                "--external:pagedjs",
                "--external:katex",
                "--external:highlight.js",
                "--external:mermaid",
                "--sourcemap",
            ),
        )
    }

tasks.processResources {
    dependsOn(tasks.compileSass)
    dependsOn(bundleTypeScript)
    // bundleThirdParty no longer needed for processResources since libraries are not in the JAR.
    // It is still needed for compileSass (hljs SCSS partials).

    // Write the absolute path of the third-party directory into a resource file
    // so the CLI can locate it at runtime regardless of how it was launched.
    doLast {
        val propsDir = destinationDir.resolve("render")
        propsDir.mkdirs()
        propsDir.resolve("thirdparty.path").writeText(thirdPartyDir.absolutePath)
    }
}

// Tests

val npmUnitTest =
    tasks.register<NpmTask>("npmTest") {
        group = "verification"
        description = "Runs npm tests"
        dependsOn(tasks.npmInstall)
        args.set(listOf("run", "test:run"))
    }

tasks.test {
    dependsOn(npmUnitTest)
}

val installPlaywrightBrowsers =
    tasks.register<NpxTask>("installPlaywrightBrowsers") {
        group = "verification"
        description = "Installs Playwright browsers"
        dependsOn(tasks.npmInstall)
        command.set("playwright")
        args.set(listOf("install", "--with-deps", "chromium"))
    }

val npmE2eTest =
    tasks.register<NpxTask>("e2eTest") {
        group = "verification"
        description = "Runs end-to-end tests"
        dependsOn(installPlaywrightBrowsers)
        command.set("playwright")

        val shard = project.findProperty("shard")?.toString()
        val totalShards = project.findProperty("totalShards")?.toString()

        val argsList = mutableListOf("test")
        if (shard != null && totalShards != null) {
            argsList.add("--shard=$shard/$totalShards")
        }
        args.set(argsList)
    }
