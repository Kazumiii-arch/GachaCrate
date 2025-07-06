plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.kazumiii"
version = "1.4.0" // Version bump for this final fix

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven {
        url = uri("https://jitpack.io/")
    }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
}

dependencies {
    // Spigot API for Minecraft 1.20.1.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // DecentHolograms dependency from JitPack.
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.9.3")
}

java {
    // Set the Java version to 21 to match DecentHolograms requirements.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21)) // <--- CHANGED TO 21
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
