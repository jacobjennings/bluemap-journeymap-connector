plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.fabricmc.fabric-loom")
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project
val blueMapApiVersion: String by project
val journeyMapApiVersion: String by project

dependencies {
    // Core module
    implementation(project(":core"))
    include(project(":core"))

    // Minecraft & Fabric
    // 26.1+ is unobfuscated: no mappings() declaration, and mod dependencies use the
    // standard implementation/compileOnly configurations (no remapping = no modX configs).
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // Kotlin language adapter
    implementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    // BlueMap API (server-side marker management)
    implementation("de.bluecolored:bluemap-api:$blueMapApiVersion")

    // JourneyMap API (client-side waypoint access)
    // Published to: https://jm.gserv.me/repository/maven-snapshots/
    compileOnly("info.journeymap:journeymap-api-fabric:$journeyMapApiVersion")
}

tasks.processResources {
    val modId: String by project
    val modName: String by project
    val modVersion: String by project
    val modDescription: String by project
    val modAuthor: String by project
    val modLicense: String by project

    inputs.property("modId", modId)
    inputs.property("modName", modName)
    inputs.property("modVersion", modVersion)
    inputs.property("modDescription", modDescription)
    inputs.property("modAuthor", modAuthor)
    inputs.property("modLicense", modLicense)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("fabricKotlinVersion", fabricKotlinVersion)

    filesMatching("fabric.mod.json") {
        expand(
            "modId" to modId,
            "modName" to modName,
            "modVersion" to modVersion,
            "modDescription" to modDescription,
            "modAuthor" to modAuthor,
            "modLicense" to modLicense,
            "minecraftVersion" to minecraftVersion,
            "fabricKotlinVersion" to fabricKotlinVersion
        )
    }
}

// 26.1+ is unobfuscated: there is no remapJar task. The plain jar is the final artifact.
tasks.jar {
    val modId: String by project
    archiveBaseName.set(modId)
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

