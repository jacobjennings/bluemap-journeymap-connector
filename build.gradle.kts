plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.serialization") version "2.3.21" apply false
    // 26.1+ uses the new unobfuscated Loom plugin id (hosted on maven.fabricmc.net),
    // which skips all remapping. The old "fabric-loom" id is for obfuscated (<=1.21.11) versions.
    id("net.fabricmc.fabric-loom") version "1.17.12" apply false
}

allprojects {
    group = "com.machinepeople.bluemapjourneymapconnector"
    version = property("modVersion") as String

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://repo.bluecolored.de/releases/") { name = "BlueMap" }
        maven("https://jm.gserv.me/repository/maven-snapshots/") { name = "JourneyMap-Snapshots" }
        maven("https://jm.gserv.me/repository/maven-releases/") { name = "JourneyMap-Releases" }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        val implementation by configurations
        implementation(kotlin("stdlib"))
        // Versions matched to what Fabric Language Kotlin 1.13.11 bundles at runtime,
        // since these are provided (not jar-in-jar'd) by FLK.
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }

    // Minecraft 26.1 requires Java 25 at runtime; compile against it too.
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
}
