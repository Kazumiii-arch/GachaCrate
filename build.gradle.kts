plugins {
    java
}

group = "com.kazumiii"
version = "2.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io/")
}

dependencies {
    // Provided by the server at runtime.
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // Optional integrations (soft dependencies, only needed to compile).
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    processResources {
        // Puts the Gradle version into plugin.yml, so there is only one place to bump it.
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        archiveBaseName.set("GachaCrate")
        archiveClassifier.set("")
    }
}
