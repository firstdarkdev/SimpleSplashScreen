base {
    archivesName.set("${orion.getProperty("mod_name").replace(" ", "")}-NeoForge-${orion.getProperty("minecraft_version")}")
}

dependencies {
    modImplementation("me.shedaniel.cloth:cloth-config-neoforge:${orion.getProperty("cloth_config")}")

    // DO NOT EDIT OR REMOVE
    implementation(project(":Common"))
}

/**
 * ===============================================================================
 * =       DO NOT EDIT BELOW THIS LINE UNLESS YOU KNOW WHAT YOU ARE DOING        =
 * ===============================================================================
 */

unimined.minecraft {
    neoForge {
        loader(orion.getProperty("neoforge_version"))
        mixinConfig("${orion.getProperty("mod_id")}.mixins.json", "${orion.getProperty("mod_id")}-neoforge.mixins.json")
    }
}

tasks.processResources {
    from(project(":Common").sourceSets.main.get().resources)
    val buildProps = project.properties.toMutableMap()

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(buildProps)
    }
}

tasks.compileTestJava { enabled = false }

tasks.withType(JavaCompile::class).configureEach {
    source(project(":Common").sourceSets.main.get().allSource)
}

publisher {
    apiKeys {
        curseforge(System.getenv("CURSE_TOKEN"))
        modrinth(System.getenv("MODRINTH_TOKEN"))
        nightbloom(System.getenv("PLATFORM_KEY"))
    }

    curseID.set("503390")
    modrinthID.set("4uhcFYch")
    nightbloomID.set("simplesplash")
    versionType.set("release")
    changelog.set(rootProject.file("changelog.md"))
    projectVersion.set("${orion.getProperty("minecraft_version")}-${project.version}")
    displayName.set("[${orion.getProperty("minecraft_version")} NeoForge] Simple Splash Screen - ${project.version}")
    setGameVersions("1.21.9")
    setLoaders("neoforge")
    setCurseEnvironment("client")
    artifact.set(tasks.getByName("remapJar"))
    disableEmptyJarCheck.set(true)

    curseDepends {
        required("cloth-config")
    }

    modrinthDepends {
        required("cloth-config")
    }
}