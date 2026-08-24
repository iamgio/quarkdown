plugins {
    kotlin("jvm")
    id("gg.jte.gradle") version "3.2.4"
}

dependencies {
    implementation("gg.jte:jte:3.2.4")
    testImplementation(kotlin("test"))
}

// Production templates: precompile into class files and bundle them into this module's jar so
// they end up on the runtime classpath through the regular project dependency mechanism.
// `installDist` drops the jar under `lib/`, and `assembleDevLib` mirrors that into `build/dev-lib/`.
jte {
    sourceDirectory.set(file("src/main/jte").toPath())
    targetDirectory.set(layout.buildDirectory.dir("jte-classes/main").map { it.asFile.toPath() })
    contentType.set(gg.jte.ContentType.Plain)
    trimControlStructures.set(true)
    precompile()
}

tasks.jar {
    dependsOn(tasks.precompileJte)
    from(layout.buildDirectory.dir("jte-classes/main")) {
        include("**/*.class")
        include("**/*.bin")
    }
}

// Test fixtures: precompiled into a separate output directory wired only into the test
// source set, so they don't ship in the production jar.
val precompileTestJte =
    tasks.register<gg.jte.gradle.PrecompileJteTask>("precompileTestJte") {
        sourceDirectory.set(file("src/test/jte").toPath())
        targetDirectory.set(
            layout.buildDirectory
                .dir("jte-classes/test")
                .get()
                .asFile
                .toPath(),
        )
        contentType.set(gg.jte.ContentType.Plain)
        trimControlStructures.set(true)
        // Default JTE package, same as the main precompile task. Template names are distinct
        // (production templates under creator/* and live-preview/*, fixtures under test/*
        // and template/*) so they don't collide.
        packageName.set("gg.jte.generated.precompiled")
        // Needed for the internal javac step that compiles the generated Java sources;
        // without it, the JTE runtime types (e.g. TemplateOutput) aren't resolved.
        compilePath.from(configurations.compileClasspath)
    }

sourceSets.test {
    output.dir(layout.buildDirectory.dir("jte-classes/test"), "builtBy" to precompileTestJte)
}

// Native image metadata

/**
 * Generates native-image reflection metadata for the precompiled JTE templates.
 *
 * JTE resolves a template by calling `Class.forName` on its generated class name, which the image
 * builder cannot detect. The list is derived from the precompiled output rather than maintained by
 * hand, so that adding or renaming a template cannot silently break the native image.
 */
val generateJteNativeImageMetadata by tasks.registering {
    group = "build"
    description = "Generates native-image reflection metadata for the precompiled JTE templates."

    val classesDir = layout.buildDirectory.dir("jte-classes/main")
    val outputFile =
        layout.buildDirectory.file(
            "generated/native-image/META-INF/native-image/com.quarkdown/quarkdown-template/reflect-config.json",
        )

    dependsOn(tasks.precompileJte)
    inputs.dir(classesDir)
    outputs.file(outputFile)

    doLast {
        val root = classesDir.get().asFile
        val templateClasses =
            root
                .walk()
                .filter { it.isFile && it.extension == "class" }
                .map {
                    it
                        .relativeTo(root)
                        .path
                        .removeSuffix(".class")
                        .replace(File.separatorChar, '.')
                }.sorted()
                .toList()

        check(templateClasses.isNotEmpty()) {
            "No precompiled JTE templates found in $root. The native image would fail to render any template."
        }

        val entries =
            templateClasses.joinToString(",\n") {
                """  { "name": "$it", "allDeclaredMethods": true, "allDeclaredConstructors": true }"""
            }

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("[\n$entries\n]\n")
        }
    }
}

tasks.jar {
    dependsOn(generateJteNativeImageMetadata)
    from(layout.buildDirectory.dir("generated/native-image"))
}
