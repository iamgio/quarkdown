plugins {
    id("quarkdown.jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

tasks.test {
    useJUnitPlatform()
}
