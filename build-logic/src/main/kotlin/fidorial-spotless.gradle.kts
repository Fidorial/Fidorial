import fr.euphyllia.fidorial.gradle.FinalizeVariables

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        ktlint("1.8.0")
    }

    kotlinGradle {
        ktlint("1.8.0")
    }

    java {
        target("src/**/*.java")
        targetExclude("src/generated/**")

        //palantirJavaFormat("2.96.0")
        forbidWildcardImports()
        formatAnnotations()
        removeUnusedImports()
        //custom("Wrap declaration parameters", WrapDeclarationParameters())
        custom("Finalize parameters and locals", FinalizeVariables())
        replaceRegex("Expand empty code blocks", """(?m)^([ \t]*)(.+) \{\}$""", "$1$2 {\n$1}")
        replaceRegex(
            "Separate module requires static",
            """(?m)^(    requires (?!static )[^\n]+;\n)(    requires static )""",
            "$1\n$2",
        )

        importOrder("", "javax|java", "\\#")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
