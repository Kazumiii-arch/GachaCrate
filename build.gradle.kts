// ========================================================================
// File: build.gradle.kts
// Location: /build.gradle.kts
// ========================================================================

plugins {
    java
    // The shadow plugin helps package our project into a distributable JAR file.
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// Project metadata
group = "com.kazumiii"
version = "1.3.1" // Version reflects the switch to DecentHolograms

repositories {
    mavenCentral()
    // The repository needed to download the Spigot API
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    // The repository needed to download the DecentHolograms API
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Spigot API for Minecraft 1.20.1.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // --- THIS IS THE FIX ---
    // DecentHolograms API. 'compileOnly' is the correct configuration for an optional dependency.
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")
}

java {
    // Set the Java version to 17, which is required for Minecraft 1.20+ plugins.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    // Configure the shadowJar task to produce our final plugin file.
    shadowJar {
        archiveBaseName.set("GachaCrate")
        archiveClassifier.set("") // This removes the '-all' suffix from the JAR name.
        archiveVersion.set(project.version.toString())
    }

    // Make the main 'build' task dependent on the 'shadowJar' task.
    build {
        dependsOn(shadowJar)
    }
    
    // Ensure the project uses UTF-8 encoding for all Java files for compatibility.
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
