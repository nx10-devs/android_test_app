# NX10 Asparagus Android Test App

Jetpack Compose test harness (`com.nx10.androidTest`) for exercising the **NX10 Asparagus Android SDK** (`com.nx10:asparagus-sdk`, Schema v3) on Android devices and emulators.

## SDK Resolution (Composite Build vs. Maven)

This app declares `implementation("com.nx10:asparagus-sdk:0.1.0")`.

- **Local side-by-side development (recommended):** Clone [`nx10-devs/android_sdk`](https://github.com/nx10-devs/android_sdk) as a sibling directory (`../android_sdk`). `settings.gradle.kts` automatically activates a Gradle Composite Build (`includeBuild("../android_sdk")`) so local SDK edits compile live with zero publishing step.
- **Standalone / local Maven:** Run `./gradlew publishToMavenLocal` inside `android_sdk`, or configure GitHub Packages credentials (`GITHUB_ACTOR`/`GITHUB_TOKEN` or `gpr.user`/`gpr.key`) to resolve `com.nx10:asparagus-sdk:0.1.0`.

## Build & Test

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew :app:assembleDebug
```

## Using the Test Harness

1. Launch **NX10 Test App** on an Android device or emulator.
2. In **1. Connection & Session**, paste your runtime **Deployment API Key** (never committed to Git).
3. Leave **Use Production Endpoint** off to target **Stage** by default, or toggle it on for **Production**.
4. Tap **Start Session**, then interact with the **Multi-Touch Sandbox Pad**, **Core Events & Spans**, **Game State (PUT / PATCH)**, **Flush Window Now**, and **Drain Queue** controls while monitoring live Schema v3 telemetry and logs.
