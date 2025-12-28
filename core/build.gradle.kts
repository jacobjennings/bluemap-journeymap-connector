plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("fabric-loom")
}

val minecraftVersion: String by project
val blueMapApiVersion: String by project

dependencies {
    // Minecraft (needed for BlockPos, ResourceLocation, etc.)
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())

    // BlueMap API
    implementation("de.bluecolored:bluemap-api:$blueMapApiVersion")
}

// This module is not a mod itself, just a library
loom {
    // No access wideners or mixins needed for core
}

