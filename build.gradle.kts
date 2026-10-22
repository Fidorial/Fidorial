plugins {
    java
    id("fidorial-spotless") apply false
    alias(libs.plugins.shadow) apply false
}

tasks.register("printVersion") {
    description = "Prints the version"
    val version = project.version
    doFirst {
        println(version)
    }
}
