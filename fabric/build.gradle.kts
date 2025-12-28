plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("fabric-loom")
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project
val blueMapApiVersion: String by project
val journeyMapApiVersion: String by project

dependencies {
    // Core module
    implementation(project(":core", configuration = "namedElements"))
    include(project(":core"))

    // Minecraft & Fabric
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // mappings(loom.officialMojangMappings())
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // Kotlin language adapter
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    // BlueMap API (server-side marker management)
    implementation("de.bluecolored:bluemap-api:$blueMapApiVersion")

    // JourneyMap API (client-side waypoint access)
    // Published to: https://jm.gserv.me/repository/maven-snapshots/
    modCompileOnly("info.journeymap:journeymap-api-fabric:$journeyMapApiVersion")
}

loom {
    accessWidenerPath.set(file("src/main/resources/bluemap-journeymap-connector.accesswidener"))
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

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

