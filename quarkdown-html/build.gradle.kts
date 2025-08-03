plugins {
    kotlin("jvm")
    id("io.miret.etienne.sass") version "1.5.2"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
}

tasks.compileSass {
    val dir = projectDir.resolve("src/main/resources/render/theme")
    sourceDir = dir
    outputDir = dir
}

tasks.processResources {
    dependsOn(tasks.compileSass)
}
