plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.kazumiii"
version = "1.4.0" // Version bump for this final fix

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.codemc.io/repository/nms/") }
}

dependencies {
    // Spigot API for Minecraft 1.20.1.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // CORRECTED: DecentHolograms API dependency based on their build.gradle.
    // Group ID: eu.decentsoftware.holograms
    // Artifact ID: decentholograms-api (most probable name for the API artifact)
    // Version: 2.8.6
    compileOnly("eu.decentsoftware.holograms:decentholograms-api:2.8.6")
}

java {
    // Set the Java version to 17, which is required for Minecraft 1.20+ plugins.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    shadowJar {
        archiveBaseName.set("GachaCrate")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
    }

    build {
        dependsOn(shadowJar)
    }
    
    withType<JavaCompile> 
{
        options.encoding = "UTF-8"
    }
}
