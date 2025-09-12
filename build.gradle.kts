// FILE: build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

group = "kai"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-java:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xexplicit-api=strict")
    }
}

application {
    mainClass.set("QuickStartKt")
}