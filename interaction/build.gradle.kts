plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":core"))
}

tasks.test {
    useJUnitPlatform()
}
