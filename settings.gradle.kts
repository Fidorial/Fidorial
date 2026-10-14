pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.euphyllia.moe/repository/maven-public/")
    }
    includeBuild("build-logic")
    includeBuild("fidorial-registry-generator")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "fidorial"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("fidorial-api")
include("fidorial-auth")
include("fidorial-bootstrap")
include("fidorial-server")
include("fidorial-test-plugin")
include("fidorial-annotation-processor")

gradle.lifecycle.beforeProject {
    val minecraftVersion = providers.gradleProperty("minecraftVersion").get().trim()
    val fidorialVersion = providers.gradleProperty("fidorialVersion").get().trim()
    val build = providers.environmentVariable("BUILD_NUMBER").orNull?.trim()?.toIntOrNull()
    // CI -> <minecraft version>-build.<build>+<fidorial version>
    // local -> <minecraft version>-local+<fidorial version>
    val versionString = when (build) {
        null -> "$minecraftVersion-local+$fidorialVersion"
        else -> "$minecraftVersion-build.$build+$fidorialVersion"
    }
    version = versionString
}
