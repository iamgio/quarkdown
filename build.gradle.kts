
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.time.Year

plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
    application
    `maven-publish`
}

allprojects {
    group = "com.quarkdown"
    version = rootProject.file("version.txt").readText().trim()

    repositories {
        mavenCentral()
    }
}

// Subprojects that should not produce a published Maven artifact.
// `quarkdown-test` contains only integration tests and has no `src/main/`.
val publicationExcludedSubprojects = setOf("quarkdown-test")

/**
 * Shared POM metadata applied to every Quarkdown Maven publication: project URL, licence,
 * developers, and SCM. Per-publication `name` and `description` are set at the call site.
 */
val publicationPomMetadata: MavenPom.() -> Unit = {
    url.set("https://github.com/iamgio/quarkdown")
    licenses {
        license {
            name.set("GNU General Public License v3.0")
            url.set("https://www.gnu.org/licenses/gpl-3.0.html")
        }
    }
    developers {
        developer {
            id.set("iamgio")
            name.set("Giorgio Garofalo")
            url.set("https://github.com/iamgio")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/iamgio/quarkdown.git")
        developerConnection.set("scm:git:ssh://git@github.com:iamgio/quarkdown.git")
        url.set("https://github.com/iamgio/quarkdown")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "se.patrikerdes.use-latest-versions")
    apply(plugin = "maven-publish")

    plugins.withType<JavaPlugin>().configureEach {
        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
        }
    }

    afterEvaluate {
        if (project.name in publicationExcludedSubprojects) return@afterEvaluate
        if (!plugins.hasPlugin("java")) return@afterEvaluate

        // GitHub Packages publication.
        //
        // The repository URL is derived from the `GITHUB_REPOSITORY` environment variable set by
        // GitHub Actions, so the same configuration publishes to the correct registry for the
        // upstream repo and for any fork. Credentials are read from `GITHUB_ACTOR` and
        // `GITHUB_TOKEN` in CI, and fall back to the `gpr.user` / `gpr.key` Gradle properties for
        // local publication.
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("Quarkdown module: ${project.name}")
                        publicationPomMetadata()
                    }
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    val repository = System.getenv("GITHUB_REPOSITORY") ?: "iamgio/quarkdown"
                    url = uri("https://maven.pkg.github.com/$repository")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                            ?: providers.gradleProperty("gpr.user").orNull
                        password = System.getenv("GITHUB_TOKEN")
                            ?: providers.gradleProperty("gpr.key").orNull
                    }
                }
            }
        }
    }
}

// Fat JAR / Distribution dependencies
gradle.projectsEvaluated {
    dependencies {
        subprojects.forEach {
            when {
                it.extra.has("noRuntime") && it.extra["noRuntime"] == true -> {
                    compileOnly(it)
                }

                else -> {
                    implementation(it)
                }
            }
        }
    }
}

application {
    mainClass.set("com.quarkdown.cli.QuarkdownCliKt")
}

ktlint {
    version.set("1.7.1")
}

// Dokka

dokka {
    dokkaPublications.html {
        outputDirectory.set(
            layout.buildDirectory
                .file("docs")
                .get()
                .asFile,
        )
    }
}

/**
 * Whether [project] uses the Quarkdoc plugin, which means its documentation must be included in the distribution zip.
 */
fun usesQuarkdoc(project: Project): Boolean {
    val quarkdoc = project(":quarkdown-quarkdoc")
    return project.configurations
        .asSequence()
        .flatMap { it.dependencies }
        .filterIsInstance<ProjectDependency>()
        .any { it.dependencyProject == quarkdoc }
}

val quarkdocGenerate =
    tasks.register("quarkdocGenerate") {
        group = "documentation"
        description = "Generates the Quarkdoc documentation for modules that include the Quarkdoc plugin."

        dependencies {
            subprojects.filter(::usesQuarkdoc).forEach {
                dokka(it)
            }
        }

        dependsOn(tasks.dokkaGenerate)
    }

tasks.register("quarkdocGenerateAll") {
    group = "documentation"
    description = "Generates the Quarkdoc documentation for all modules."

    dependencies {
        subprojects.forEach {
            dokka(it)
        }
    }

    dependsOn(tasks.dokkaGenerate)
}

allprojects {
    fun asset(path: String): File = project(":quarkdown-quarkdoc").projectDir.resolve("src/main/resources/$path")

    dokka {
        pluginsConfiguration.html {
            val year = Year.now().value
            footerMessage.set("&copy; $year Quarkdown")
            customAssets.from(*asset("assets/images").listFiles()!!)
            customStyleSheets.from(asset("styles/stylesheet.css"))
        }
    }
}

// Tasks

/**
 * Populates a [CopySpec] with the Quarkdown install library layout, with subdirectories such as `qd/` and `html/`.
 * Used by both [distributions]`.main` and [assembleDevLib].
 *
 * This layout is navigable at runtime via the `install-layout-navigator` module.
 */
val installLibLayout: CopySpec.() -> Unit = {
    // .qd library files.
    into("qd") {
        from(project(":quarkdown-libs").file("src/main/resources")) {
            include("*.qd")
        }
    }
    // HTML rendering resources (third-party libraries, themes, scripts) for offline rendering.
    into("html") {
        from(project(":quarkdown-html").layout.buildDirectory.dir("install"))
    }
    // Agent skills.
    into("skills") {
        from(rootProject.file("skills"))
    }
}

// Bundled JVM runtime

/**
 * Creates a minimal JRE via `jlink`, containing only the JDK modules required by Quarkdown and its dependencies.
 * The output is placed at `build/runtime` and included in the distribution as `runtime/`.
 *
 * The required modules are discovered dynamically via `jdeps` on the runtime classpath JARs,
 * so the module set stays correct as dependencies change. SPI-loaded modules (e.g. `jdk.crypto.ec`
 * for HTTPS/TLS) are added explicitly, since `jdeps` cannot detect them via static analysis.
 */
val bundleRuntime by tasks.registering {
    group = "distribution"
    description = "Creates a minimal JRE via jlink for bundling with the distribution."

    dependsOn(tasks.jar, subprojects.map { it.tasks.named("jar") })

    val runtimeClasspath = configurations.runtimeClasspath
    val runtimeDir = layout.buildDirectory.dir("runtime")
    outputs.dir(runtimeDir)

    // SPI-loaded modules that jdeps cannot detect via static analysis:
    // - `jdk.crypto.ec`: TLS handshakes with EC certificates (most modern HTTPS endpoints).
    // - `jdk.localedata`: display names and resource bundles used by Quarkdown's localization.
    val spiModules = listOf("jdk.crypto.ec", "jdk.localedata")

    doLast {
        val jdepsOutput =
            ByteArrayOutputStream()
                .also { out ->
                    exec {
                        val jars = runtimeClasspath.get().filter { it.name.endsWith(".jar") }
                        commandLine(
                            "jdeps",
                            "--ignore-missing-deps",
                            "--multi-release",
                            "17",
                            "--print-module-deps",
                            *jars.map { it.absolutePath }.toTypedArray(),
                        )
                        standardOutput = out
                    }
                }.toString()
                .trim()

        // jdeps may output warnings before the final line; the module list is always the last line.
        val detectedModules = jdepsOutput.lines().last().trim()
        val allModules = (detectedModules.split(",") + spiModules).joinToString(",")

        val outputDir = runtimeDir.get().asFile
        delete(outputDir)

        exec {
            commandLine(
                "jlink",
                "--add-modules",
                allModules,
                "--strip-debug",
                "--no-man-pages",
                "--no-header-files",
                "--compress",
                "2",
                "--output",
                outputDir.absolutePath,
            )
        }
    }
}

distributions.main {
    contents {
        into("lib", installLibLayout)
        // Bundled minimal JRE, created by bundleRuntime.
        from(layout.buildDirectory.dir("runtime")) {
            into("runtime")
        }
        // Include the generated Dokka documentation, generated by quarkdocGenerate,
        // in the 'docs' directory.
        val dokkaOutputDir = layout.buildDirectory.file("docs")
        from(dokkaOutputDir) {
            into("docs")
        }
        // .qd wiki sources for the agent skill.
        into("docs/wiki") {
            from(rootProject.file("docs")) {
                include("**/*.qd")
            }
            includeEmptyDirs = false
        }
    }
}

// Assembles a dev-time install `lib/` layout at `<rootProject>/build/dev-lib`, mirroring
// the distribution layout so that `gradle run` and IntelliJ runs can resolve the install
// directory at runtime.
val assembleDevLib by tasks.registering(Sync::class) {
    dependsOn(":quarkdown-html:bundleThirdParty")
    into(layout.buildDirectory.dir("dev-lib"))
    installLibLayout()
}

/**
 * Standalone zip artifact containing the Quarkdown install `lib/` layout, with the same
 * `lib/qd`, `lib/html`, `lib/skills` shape produced by [installLibLayout]. Published as a
 * Maven artifact alongside the JVM modules so that consumers embedding Quarkdown can fetch
 * the runtime resources (`.qd` libraries, HTML themes and scripts, third-party bundles,
 * agent skills) without having to vendor the upstream source or unpack the full
 * distribution zip.
 */
val installLibZip by tasks.registering(Zip::class) {
    group = "distribution"
    description = "Packages the Quarkdown install `lib/` layout (qd, html, skills) as a standalone zip artifact."

    archiveBaseName.set("quarkdown-install-lib")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // The HTML install layout is produced as a side-effect of these bundling tasks.
    // Provider-based wiring should detect these automatically, but declaring them explicitly
    // keeps the published artifact reliably reproducible.
    dependsOn(
        ":quarkdown-html:assembleThemes",
        ":quarkdown-html:bundleTypeScript",
        ":quarkdown-html:bundleHighlightJs",
        ":quarkdown-html:bundleThirdParty",
    )

    into("lib", installLibLayout)
}

// Root-project publication of the standalone install-lib zip.
//
// The JVM module publications live on the subprojects (see the `subprojects` block above);
// this block adds a single artifact on the root so consumers can resolve the runtime
// install layout under a stable Maven coordinate:
//
//     com.quarkdown:quarkdown-install-lib:<version>@zip
publishing {
    publications {
        create<MavenPublication>("installLib") {
            artifactId = "quarkdown-install-lib"
            artifact(installLibZip) {
                extension = "zip"
            }
            pom {
                name.set("quarkdown-install-lib")
                description.set("Quarkdown install `lib/` layout (qd, html, skills) as a standalone zip artifact.")
                publicationPomMetadata()
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            val repository = System.getenv("GITHUB_REPOSITORY") ?: "iamgio/quarkdown"
            url = uri("https://maven.pkg.github.com/$repository")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: providers.gradleProperty("gpr.user").orNull
                password = System.getenv("GITHUB_TOKEN")
                    ?: providers.gradleProperty("gpr.key").orNull
            }
        }
    }
}

tasks.installDist {
    dependsOn(quarkdocGenerate, bundleRuntime)
}

tasks.distZip {
    dependsOn(quarkdocGenerate, bundleRuntime)
    archiveVersion.set("")
}

tasks.distTar {
    dependsOn(quarkdocGenerate, bundleRuntime)
    archiveVersion.set("")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.ktlintCheck)
}

tasks.named<CreateStartScripts>("startScripts") {
    classpath = files("lib/*") // Fixes the 'Input line is too long' error on Windows.
    // Prepends subscripts to the generated start scripts.
    doLast {
        val dir = file("scripts")
        val scripts = sequenceOf("bootstrap")

        scripts.forEach { scriptName ->
            val unixPrefix = dir.resolve("$scriptName.sh").readText() + "\n"
            val windowsPrefix = dir.resolve("$scriptName.bat").readText() + "\n"
            unixScript.writeText("#!/bin/sh\n\n" + unixPrefix + unixScript.readText())
            windowsScript.writeText("@echo off\n\n" + windowsPrefix + windowsScript.readText())
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.3"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

// Dependency updates

allprojects {
    tasks.dependencyUpdates {
        rejectVersionIf {
            Regex("[.-](alpha|beta|rc|cr|m|preview|b|ea)", RegexOption.IGNORE_CASE) in candidate.version
        }
    }

    tasks.useLatestVersions {
        updateBlacklist =
            listOf(
                "org.jetbrains.kotlin",
                "org.jetbrains.dokka",
            )
    }
}
