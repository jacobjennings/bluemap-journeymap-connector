plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.fabricmc.fabric-loom")
}

val minecraftVersion: String by project
val blueMapApiVersion: String by project

dependencies {
    // Minecraft (needed for BlockPos, Identifier, etc.)
    // 26.1+ is unobfuscated: no mappings() declaration is required.
    minecraft("com.mojang:minecraft:$minecraftVersion")

    // BlueMap API
    implementation("de.bluecolored:bluemap-api:$blueMapApiVersion")
}

// This module is not a mod itself, just a library
loom {
    // No access wideners or mixins needed for core
}

