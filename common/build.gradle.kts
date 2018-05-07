plugins {
    id("kotlin-platform-common")
}

repositories {
    jcenter()
    maven(url = "http://repo.net.local/nexus/content/groups/public")
}

dependencies {
    compile(kotlin("stdlib-common"))
    testCompile(kotlin("test-common"))
    testCompile(kotlin("test-annotations-common"))
}
