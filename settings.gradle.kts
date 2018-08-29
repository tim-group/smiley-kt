pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "http://repo.net.local/nexus/content/groups/public")
    }
}

rootProject.name = "smiley-kt"

enableFeaturePreview("STABLE_PUBLISHING")

include("webui", "backend", "common")
