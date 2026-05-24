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
