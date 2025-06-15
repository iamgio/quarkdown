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
    implementation("org.apache.commons:commons-text:1.13.0")
}

tasks.compileSass {
    dependsOn(tasks.processResources)
    val dir = projectDir.resolve("src/main/resources/render/theme")
    sourceDir = dir
    outputDir = dir
}

sequenceOf("run", "build", "distZip").forEach {
    project.parent!!.tasks.named(it) {
        dependsOn(tasks.compileSass)
    }
}
