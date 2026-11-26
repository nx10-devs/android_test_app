pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        val gprKey = providers.gradleProperty("gpr.key")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))
            .orNull
        if (!gprKey.isNullOrBlank()) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/nx10-devs/android_sdk")
                credentials {
                    username = providers.gradleProperty("gpr.user")
                        .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                        .orNull
                    password = gprKey
                }
                content {
                    includeGroup("com.nx10")
                }
            }
        }
    }
}

rootProject.name = "NX10 Test App"
include(":app")

val localSdkDir = listOf("../android_sdk", "../asparagus-android-sdk")
    .map { file(it) }
    .firstOrNull { it.exists() }
if (localSdkDir != null) {
    includeBuild(localSdkDir) {
        dependencySubstitution {
            substitute(module("com.nx10:asparagus-sdk")).using(project(":asparagus-sdk"))
        }
    }
}
