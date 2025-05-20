plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
    implementation("org.apache.commons:commons-text:1.13.0")
}
