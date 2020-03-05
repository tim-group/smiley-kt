pluginManagement {
    repositories {
        gradlePluginPortal()
        val repoUrl: String by settings
        maven(url = "$repoUrl/groups/public")
    }
}

rootProject.name = "smiley-kt"

include("webui", "server")
