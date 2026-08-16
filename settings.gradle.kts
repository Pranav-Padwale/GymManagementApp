pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GymManagementApp"
include(":shared")
include(":desktopApp")
include(":server")
include(":androidApp")
