# TouchLab — Environment Record

Recorded: 2026-10-20 (local machine)

## Host

| Item | Value |
|---|---|
| OS | Windows 11 10.0.26200 (amd64) |
| Shell used by agent | Git Bash (MINGW64) |
| Project path | `C:/Users/luxia/project/private/TouchLab` |

## JDK

The OS-default `java` is JDK 8 (`Zulu 8.66.0.15`), which cannot run modern
Android tooling or `sdkmanager`. The project is built with the installed JDK 17:

```text
JAVA_HOME=C:\Users\luxia\programe\java\zulu17.34.19-ca-jdk17.0.3-win_x64
openjdk version "17.0.3" 2022-04-19 LTS
Zulu17.34+19-CA
```

Other installed JDKs: Zulu 11, Zulu 21. JDK 17 is used consistently for Gradle
and SDK command-line tools.

## Android SDK

```text
SDK path (local.properties): C:/Users/luxia/AppData/Local/Android/Sdk
adb: 1.0.41 / platform-tools 35.0.2 (C:\Users\luxia\programe\platform-tools)
cmdline-tools: 13.0
emulator: 35.5.10
AEHD hypervisor 2.2: installed and usable
```

Installed platforms before this project: 28, 30, 31, 33, 34.
Installed build-tools before this project: 30.0.3, 34.0.0, 36.0.0.

Installed during Phase 0:

- `platforms;android-37.0`
- `build-tools;37.0.0`
- `system-images;android-37.0;google_apis;x86_64` (for API 37 emulator coverage)

`ANDROID_HOME` and `ANDROID_SDK_ROOT` were unset in the host environment, so
the SDK location is supplied through `local.properties` (git-ignored).

## Verified build toolchain

| Component | Version | Notes |
|---|---|---|
| Gradle Wrapper | 9.4.1 | Generated from a trusted local Gradle 8.5 install, then downloaded from services.gradle.org |
| AGP | 9.2.1 | Matches guide candidate; max supported API 37.0 |
| Kotlin | AGP 9 built-in Kotlin (KGP 2.2.10 per AGP 9.2 release notes) | `org.jetbrains.kotlin.android` is deliberately **not** applied |
| Compose Compiler plugin | `org.jetbrains.kotlin.plugin.compose` 2.2.10 | Matches built-in Kotlin version |
| Compose BOM | 2026.09.00 | Official stable BOM shown in Compose setup docs |
| compileSdk | 37 | Platform `android-37.0` |
| targetSdk | 36 | Google Play 2026 requirement |
| minSdk | 26 | Android 8.0 |
| JDK / jvmTarget | 17 | |

## Emulators / devices

| Name | Image | Purpose |
|---|---|---|
| `touchlab_api34` | `system-images;android-34;google_apis;x86_64`, Pixel 6 profile | Primary interaction tests, screenshots |
| API 37 AVD | `system-images;android-37.0;google_apis;x86_64` | Current-platform coverage (created once image finishes installing) |

## Reproducing the environment

```bash
export JAVA_HOME="C:\Users\luxia\programe\java\zulu17.34.19-ca-jdk17.0.3-win_x64"
export PATH="$JAVA_HOME/bin:$PATH"
printf 'sdk.dir=C:/Users/luxia/AppData/Local/Android/Sdk\n' > local.properties
./gradlew.bat :app:assembleDebug
```

## Phase 0 evidence

- `./gradlew.bat --version` → Gradle 9.4.1, Launcher JVM 17.0.3
- `./gradlew.bat :app:assembleDebug` → `BUILD SUCCESSFUL in 2m 38s`
- APK: `app/build/outputs/apk/debug/app-debug.apk` (12,541,265 bytes)

### First failure and fix

The first build failed with `java.io.IOException: Invalid file path`. The
generated `local.properties` contained Windows backslashes that were parsed as
escape sequences. Replacing it with forward slashes
(`sdk.dir=C:/Users/luxia/AppData/Local/Android/Sdk`) fixed the build.
