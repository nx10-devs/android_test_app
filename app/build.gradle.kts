import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localPropsText = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties")
).asText.orNull
val localApiKey = localPropsText?.let { text ->
    Properties().apply { load(StringReader(text)) }
        .getProperty("ASPARAGUS_API_KEY")
        ?.trim()
}.orEmpty()
val resolvedDefaultApiKey = localApiKey.ifEmpty {
    providers.environmentVariable("ASPARAGUS_API_KEY").orNull?.trim().orEmpty()
}
val escapedDefaultApiKey = resolvedDefaultApiKey
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.nx10.androidTest"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.nx10.androidTest"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEFAULT_API_KEY", "\"$escapedDefaultApiKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.nx10:asparagus-sdk:0.2.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
