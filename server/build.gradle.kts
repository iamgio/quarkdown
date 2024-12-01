plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))

    val ktorVersion = "3.0.1"

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}
