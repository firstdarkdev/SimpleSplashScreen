import xyz.wagyourtail.unimined.api.unimined
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    java
    id("xyz.wagyourtail.unimined") version "1.3.9" apply false
    id("com.hypherionmc.modutils.modpublisher") version "2.1.+" apply false
    id("com.hypherionmc.modutils.orion") version "2.0.+"
}

orion.setup {
    multiProject.set(true)
    enableMirrorMaven.set(true)
    enableReleasesMaven.set(true)
    enableSnapshotsMaven.set(true)

    dopplerToken.set(System.getenv("DOPPLER_TOKEN"))

    versioning {
        val releaseType = project.properties["releaseType"] ?: orion.getProperty("release_type")
        identifier(releaseType as String)
        uploadBuild(releaseType == "release")
    }

    tools {
        lombok()
    }
}

group = orion.getProperty("project_group")

subprojects {
    apply(plugin = "java")
    apply(plugin = "xyz.wagyourtail.unimined")
    apply(plugin = "com.hypherionmc.modutils.modpublisher")
    apply(plugin = "maven-publish")
    apply(plugin = "com.hypherionmc.modutils.orion")

    group = rootProject.group
    version = rootProject.version

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral()
    }

    tasks.jar {
        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to orion.getProperty("mod_name"),
                    "Specification-Vendor" to orion.getProperty("mod_author"),
                    "Specification-Version" to orion.versioning.buildVersion(),
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to orion.versioning.buildVersion(),
                    "Implementation-Vendor" to orion.getProperty("mod_author"),
                    "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                    "Timestamp" to System.currentTimeMillis(),
                    "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                    "Built-On-Minecraft" to orion.getProperty("minecraft_version")
                )
            )
        }
    }

    unimined.minecraft(sourceSets.main.get(), true) {
        version(orion.getProperty("minecraft_version"))

        mappings {
            mojmap()
            devNamespace("mojmap")
        }
    }

    tasks.withType(JavaCompile::class).configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType(GenerateModuleMetadata::class).configureEach {
        enabled = false
    }
}