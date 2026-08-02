plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-html"))
}
