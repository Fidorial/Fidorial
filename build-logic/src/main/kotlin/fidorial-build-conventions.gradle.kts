import fr.euphyllia.fidorial.gradle.FidorialBuildExtension
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    id("net.ltgt.errorprone")
}

val fidorialBuild = extensions.create<FidorialBuildExtension>("fidorialBuild")

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.lucko.me/")
    // geyser's configurate fork
    maven("https://repo.opencollab.dev/maven-releases")
    maven("https://repo.opencollab.dev/maven-snapshots")
}

extensions.configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = Charsets.UTF_8.name()
    options.isFork = true
    options.release = 25

    fidorialBuild.readUnnamedModules.get().forEach {
        options.compilerArgs.addAll(
            listOf(
                "--add-reads",
                "$it=ALL-UNNAMED"
            )
        )
    }

    options.errorprone {
        disable(
            "MissingSummary", "ReferenceEquality", "AvoidCommonTypeNames", "SameNameButDifferent",
            "IdentityHashMapUsage", "ThreadLocalUsage", "UnnecessaryLambda", "UnnecessaryMethodReference",
            "NonApiType", "InlineFormatString", "StringConcatToTextBlock", "RefactorSwitch",
            "UnusedTypeParameter", "ExposedPrivateType", "LoopOverCharArray"
        )

        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:JSpecifyMode", "true")
        option("NullAway:JSpecifyExperimental", "true")

        error(
            "EqualsIncompatibleType",
            "CollectionIncompatibleType",
            "StringCaseLocaleUsage",
            "DefaultLocale",
            "DefaultCharset",
            "UnsafeLocaleUsage"
        )
    }

    options.compilerArgs.addAll(
        listOf(
            "-Xmaxwarns", "10000",
            "-Xmaxerrs", "10000"
        )
    )
}

tasks.withType<ProcessResources>().configureEach {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.withType<Test>().configureEach {
    fidorialBuild.readUnnamedModules.get().forEach {
        jvmArgs("--add-reads", "$it=ALL-UNNAMED")
    }
}

tasks.withType<JavaExec>().configureEach {
    fidorialBuild.readUnnamedModules.get().forEach {
        jvmArgs("--add-reads", "$it=ALL-UNNAMED")
    }
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = Charsets.UTF_8.name()

    val options = options as StandardJavadocDocletOptions
    fidorialBuild.readUnnamedModules.get().forEach {
        options.addStringOption(
            "-add-reads",
            "$it=ALL-UNNAMED"
        )
    }
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.52.0")
    errorprone("com.uber.nullaway:nullaway:0.14.3")
}
