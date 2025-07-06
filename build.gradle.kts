
plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.kazumiii"
version = "1.4.0" // Version bump for this final fix

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    // The repository needed to download the DecentHolograms API
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Spigot API for Minecraft 1.20.1.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // DecentHolograms API. 'compileOnly' is the correct configuration.
    // Changed dependency to the API artifact.
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
