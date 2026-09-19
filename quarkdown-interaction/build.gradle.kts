plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":quarkdown-core"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.websockets)
    api(libs.kotlinx.serialization.json)
}

tasks.test {
    useJUnitPlatform()
}
