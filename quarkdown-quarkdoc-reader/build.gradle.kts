plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.kotlinx.serialization.json)
}

tasks.test {
    useJUnitPlatform()
}
