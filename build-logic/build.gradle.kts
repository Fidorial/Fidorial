plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(gradleApi())
    implementation(libs.spotless)
    implementation(libs.spotless.lib)
    implementation(libs.spotless.lib.extra)
    implementation(libs.javaparser.core)
    implementation(libs.errorprone)
}

gradlePlugin {
    plugins {
        register("pluginLibraries") {
            id = "fr.fidorial.plugin-libraries"
            implementationClass = "fr.euphyllia.fidorial.gradle.libraries.PluginLibrariesPlugin"
        }
    }
}
