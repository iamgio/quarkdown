plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.20.1")
}

tasks.test {
    useJUnitPlatform()
}
