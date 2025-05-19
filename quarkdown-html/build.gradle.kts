plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":quarkdown-core"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    implementation("org.apache.commons:commons-text:1.13.0")
}
