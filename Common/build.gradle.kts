tasks.jar {
    archiveBaseName.set("${orion.getProperty("mod_name").replace(" ", "")}-Common-${orion.getProperty("minecraft_version")}")
}

dependencies {
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${orion.getProperty("cloth_config")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
}

/**
 * ===============================================================================
 * =       DO NOT EDIT BELOW THIS LINE UNLESS YOU KNOW WHAT YOU ARE DOING        =
 * ===============================================================================
 */

unimined.minecraft {
    fabric {
        loader(orion.getProperty("fabric_loader"))
    }

    defaultRemapJar = false
}

tasks.withType(ProcessResources::class).configureEach {
    val buildProps = project.properties.toMutableMap()

    filesMatching(listOf("pack.mcmeta")) {
        expand(buildProps)
    }
}