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
    // ADDED: Official DecentHolograms repositories
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") } // Also added as it's used by DH
}

dependencies {
    // Spigot API for Minecraft 1.20.1.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    compileOnly("eu.decentsoftware.holograms:decentholograms-api:2.9.4")
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
