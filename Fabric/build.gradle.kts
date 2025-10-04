base {
    archivesName.set("${orion.getProperty("mod_name").replace(" ", "")}-Fabric-${orion.getProperty("minecraft_version")}")
}

dependencies {
    modImplementation("net.fabricmc.fabric-api:fabric-api:${orion.getProperty("fabric_api")}")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${orion.getProperty("cloth_config")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    // Do not remove or edit!
    implementation(project(":Common"))
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
}

tasks.processResources {
    from(project(":Common").sourceSets.main.get().resources)
    val buildProps = project.properties.toMutableMap()

    filesMatching("fabric.mod.json") {
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
    displayName.set("[${orion.getProperty("minecraft_version")} Fabric] Simple Splash Screen - ${project.version}")
    setGameVersions("1.21.9")
    setLoaders("fabric")
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