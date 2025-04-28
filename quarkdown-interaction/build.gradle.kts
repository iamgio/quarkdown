plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":quarkdown-core"))
}

tasks.test {
    useJUnitPlatform()
}
