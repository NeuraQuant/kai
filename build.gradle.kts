import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.vanniktech.maven.publish") version "0.25.3"
}

group = "io.github.neuraquant"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Testing (optional, minimal)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01, true)
    
    signAllPublications()
    
    coordinates("io.github.neuraquant", "kai", version.toString())
    
    pom {
        name.set("Kai")
        description.set("Lightweight utility library for structuring and managing Agentic AI agents")
        inceptionYear.set("2024")
        url.set("https://github.com/NeuraQuant/kai")
        
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        
        developers {
            developer {
                id.set("neuraquant")
                name.set("NeuraQuant")
                url.set("https://github.com/NeuraQuant")
            }
        }
        
        scm {
            url.set("https://github.com/NeuraQuant/kai")
            connection.set("scm:git:git://github.com/NeuraQuant/kai.git")
            developerConnection.set("scm:git:ssh://git@github.com/NeuraQuant/kai.git")
        }
    }
}