plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

application { 
    mainClass.set("com.gym.server.ApplicationKt") 
}

dependencies {
    val ktorVersion = "3.0.0"
    val mongoVersion = "5.1.3"

    implementation(project(":shared"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongoVersion")
    implementation("org.mongodb:bson-kotlinx:$mongoVersion")
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.mindrot:jbcrypt:0.4")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

tasks.test {
    useJUnitPlatform()
}
