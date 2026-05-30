plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.22.2")
}

tasks.test {
    useJUnitPlatform()
}
