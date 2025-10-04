import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "io.github.neuraquant"
version = "1.0.0"

// Version validation to prevent overwriting existing releases
tasks.register("validateVersion") {
    doLast {
        val currentVersion = project.version.toString()
        
        // Check if version is SNAPSHOT (allowed to be overwritten)
        if (currentVersion.endsWith("-SNAPSHOT")) {
            println("✅ SNAPSHOT version detected - overwriting is allowed")
            return@doLast
        }
        
        // For release versions, check if they already exist
        val groupPath = group.toString().replace(".", "/")
        val mavenCentralUrl = "https://repo1.maven.org/maven2/$groupPath/$name/$currentVersion"
        
        try {
            val connection = java.net.URL(mavenCentralUrl).openConnection()
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = (connection as java.net.HttpURLConnection).responseCode
            if (responseCode == 200) {
                throw GradleException("""
                    ❌ Version $currentVersion already exists in Maven Central!
                    URL: $mavenCentralUrl
                    
                    To fix this:
                    1. Update the version in build.gradle.kts
                    2. Use ./scripts/bump-version.sh to increment version
                    3. Or use a SNAPSHOT version for development
                """.trimIndent())
            } else {
                println("✅ Version $currentVersion is safe to publish")
            }
        } catch (e: java.net.ConnectException) {
            println("⚠️  Could not check Maven Central (network issue) - proceeding with caution")
        } catch (e: Exception) {
            if (e !is GradleException) {
                println("⚠️  Could not validate version - proceeding with caution")
            } else {
                throw e
            }
        }
    }
}

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Kai")
                description.set("Lightweight utility library for structuring and managing Agentic AI agents")
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
                        email.set("contact@neuraquant.io")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/NeuraQuant/kai.git")
                    developerConnection.set("scm:git:ssh://github.com:NeuraQuant/kai.git")
                    url.set("https://github.com/NeuraQuant/kai")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("sonatypeUsername") as String?)
            password.set(project.findProperty("sonatypePassword") as String?)
        }
    }
}

// Make publishing tasks depend on version validation
tasks.named("publishToSonatype") {
    dependsOn("validateVersion")
}

tasks.named("publish") {
    dependsOn("validateVersion")
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}