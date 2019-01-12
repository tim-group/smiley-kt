plugins {
    id("kotlin-platform-common")
}

repositories {
    jcenter()
    val repoUrl: String by project
    maven(url = "$repoUrl/groups/public")
}

dependencies {
    compile(kotlin("stdlib-common"))
    testCompile(kotlin("test-common"))
    testCompile(kotlin("test-annotations-common"))
}
