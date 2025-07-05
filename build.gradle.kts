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
version = "1.2.0" // Reflects the version with the latest advanced features

repositories {
    mavenCentral()
    // The repository needed to download the Spigot API
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
}

dependencies {
    // Spigot API for Minecraft 1.20.1.
    // 'compileOnly' means it's needed for compiling but won't be included in the
    // final JAR, as the server provides it at runtime.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
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
    // This ensures that running 'gradle build' will create the final JAR.
    build {
        dependsOn(shadowJar)
    }
    
    // Ensure the project uses UTF-8 encoding for all Java files for compatibility.
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
