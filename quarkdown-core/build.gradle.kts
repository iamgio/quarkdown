plugins {
    kotlin("jvm")
    id("com.quarkdown.amber") version "2.1.4"
    `java-test-fixtures`
}

val cslStyles: Configuration by configurations.creating

dependencies {
    sequenceOf(kotlin("test"), "org.assertj:assertj-core:3.27.6").forEach {
        testFixturesImplementation(it)
        testImplementation(it)
    }
    testImplementation(testFixtures(project))
    implementation(kotlin("reflect"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("org.apache.logging.log4j:log4j-core:2.25.3")
    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("gg.jte:jte:3.2.3")
    implementation("com.github.ajalt.colormath:colormath:3.6.1")
    implementation("com.github.fracpete:romannumerals4j:0.0.1")
    implementation("de.undercouch:citeproc-java:3.5.0")
    cslStyles("org.citationstyles:styles:26.2")
    implementation("org.citationstyles:locales:26.2")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
}

// Extracts only the CSL style files listed in csl-styles.txt from the full styles collection, to reduce the bundle size.
val extractCslStyles by tasks.registering {
    val styleListFile = file("csl-styles.txt")
    val outputDir = layout.buildDirectory.dir("generated/csl-styles")

    inputs.files(cslStyles)
    inputs.file(styleListFile)
    outputs.dir(outputDir)

    doLast {
        val outDir = outputDir.get().asFile
        outDir.deleteRecursively()
        outDir.mkdirs()

        val styleNames =
            styleListFile
                .readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

        project.copy {
            from(project.zipTree(cslStyles.singleFile))
            into(outDir)
            include(styleNames.map { "$it.csl" })
        }

        // Verify all listed styles were found.
        val extracted = outDir.listFiles()?.map { it.nameWithoutExtension }?.toSet() ?: emptySet()
        val missing = styleNames - extracted
        if (missing.isNotEmpty()) {
            error("CSL styles not found in styles JAR: ${missing.joinToString()}")
        }
    }
}

sourceSets.main {
    resources.srcDir(extractCslStyles)
}
