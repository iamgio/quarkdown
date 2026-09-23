plugins {
    id("quarkdown.jvm")
}

dependencies {
    testImplementation(project(":quarkdown-core-test-fixtures"))
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-html"))
}
