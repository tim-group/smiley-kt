pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "http://repo.net.local/nexus/content/groups/public")
    }
}

rootProject.name = "smiley-kt"

include("webui")
