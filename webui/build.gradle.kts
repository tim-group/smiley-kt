plugins {
    id("com.timgroup.webpack")
}

node {
    download = true
    version = "12.16.1"
}

val web by configurations.creating

dependencies {
    web(fileTree("build/site") {
        builtBy(tasks.named("webpack"))
    })
}
