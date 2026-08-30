plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.21"
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}
