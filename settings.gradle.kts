pluginManagement {
    repositories {
        mavenCentral()
        maven("https://mcentral.firstdark.dev/releases")
        maven("https://maven.firstdark.dev/releases")
        gradlePluginPortal() {
            content {
                excludeGroup("org.apache.logging.log4j")
            }
        }
    }
}

rootProject.name = "SimpleSplashScreen"
include("Common", "NeoForge", "Fabric")