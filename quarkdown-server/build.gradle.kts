plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-template"))
    implementation(project(":quarkdown-interaction"))

    val ktorVersion = "3.5.0"

    implementation("io.ktor:ktor-server-netty:$ktorVersion") {
        exclude(group = "io.netty", module = "netty-codec-marshalling")
        exclude(group = "io.netty", module = "netty-codec-protobuf")
    }
    implementation("io.ktor:ktor-server-sse:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:2.0.18")
}
