// ========================================================================
// File: build.gradle.kts
// Location: /build.gradle.kts
// ========================================================================
plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.kazumiii"
version = "1.5.0" // Version bump for universal hologram support

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io/") } // For FancyHolograms
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    
    // --- ADDING BOTH HOLOGRAM APIS ---
    // Both are optional and only needed for compiling.
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")
    compileOnly("com.github.OllivDev:FancyHolograms:2.1.1")
}

java {
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
    
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
