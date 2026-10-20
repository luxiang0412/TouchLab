# TouchLab — Progress Log

This file is updated at the end of every phase. A phase is only marked DONE
after its verification commands have actually run.

---

## Phase 0 — Environment inspection and project bootstrap
- 状态：DONE
- 实现内容：
  - Inspected host OS, JDKs, Android SDK, adb, emulators and Git.
  - Found OS-default JDK 8; selected installed Zulu JDK 17 for all Android tooling.
  - Installed Android Platform 37.0 and Build Tools 37.0.0 through `sdkmanager`.
  - Created a Gradle 9.4.1 wrapper, AGP 9.2.1 project using AGP built-in Kotlin,
    Compose BOM 2026.09.00, compileSdk 37 / targetSdk 36 / minSdk 26.
  - Added a minimal Compose Activity, dark theme shell, adaptive vector icon.
  - Initialized a local Git repository.
- 修改文件：
  - `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`,
    `gradle/libs.versions.toml`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*`
  - `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`,
    `app/src/main/java/dev/touchlab/app/MainActivity.kt`,
    `app/src/main/res/values/*`, `app/src/main/res/drawable/*`,
    `app/src/main/res/mipmap-anydpi-v26/*`
  - `README.md`, `docs/ENVIRONMENT.md`, `docs/PROGRESS.md`, `docs/BACKLOG.md`,
    `.gitignore`
- 验证命令与退出码：
  - `./gradlew.bat --version` → 0 (Gradle 9.4.1, JVM 17.0.3)
  - `./gradlew.bat :app:assembleDebug` → 0 (`BUILD SUCCESSFUL in 2m 38s`)
- 证据：
  - APK: `app/build/outputs/apk/debug/app-debug.apk`
  - Toolchain table: `docs/ENVIRONMENT.md`
- 已知问题：
  - First build failed with `Invalid file path` due to backslash escaping in
    `local.properties`; fixed by using forward slashes.
  - API 37 emulator image was still downloading at the end of the phase; API 34
    emulator created for initial device checks.
- 下一步：Phase 1 — design tokens, home screen, navigation shell, test tags.

---

## Phase 1 — Design system and home screen
- 状态：DONE
- 实现内容：
  - Added design tokens (`Color.kt`, `Type.kt`, `Theme.kt`): dark palette exactly
    matching the brief, tonal type scale, Material 3 dark scheme.
  - Edge-to-edge Activity with light system-bar icons.
  - Branded home screen: wordmark, tagline, headline, three 168dp lab cards with
    original Canvas illustrations, numbering, arrows, press-scale feedback,
    settings entry and version label.
  - App-level two-level navigation with `rememberSaveable`; system back and
    in-app back return to home; 220ms crossfade (disabled under reduce motion).
  - Settings bottom sheet with Sound / Haptic / Reduce motion switches backed by
    DataStore; changes apply immediately and are saveable.
  - `testTag`s for every acceptance-relevant control.
  - Added API 34 emulator `touchlab_api34`, installed and launched the app.
- 修改文件：
  - `ui/theme/*`, `ui/Toy.kt`, `ui/TouchLabApp.kt`, `ui/components/*`
  - `feature/home/HomeScreen.kt`, `feature/play/PlayShell.kt` (temporary shell)
  - `settings/SettingsRepository.kt`, `settings/SettingsSheet.kt`
  - `MainActivity.kt`, `app/build.gradle.kts`, `gradle/libs.versions.toml`
  - `app/src/androidTest/java/dev/touchlab/app/TouchLabUiTest.kt`
  - `docs/screenshots/phase1-home.png`, `phase1-play-shell.png`,
    `phase1-settings.png`
- 验证命令与退出码：
  - `./gradlew.bat :app:assembleDebug` → 0
  - `./gradlew.bat :app:connectedDebugAndroidTest` → 0
    - 8 tests, 0 skipped, 0 failed on `touchlab_api34(AVD) - API 14 (Android 14)`
  - `adb install -r` + `am start` → installed and launched.
- 证据：
  - `app/build/reports/androidTests/connected/debug/index.html`
  - Screenshots listed above (real screencaps from emulator).
- 已知问题：
  - `PlayShell` is explicitly temporary; the real Jelly/Dust/Marbles physics
    screens replace it in Phases 2-4. No placeholder remains in final delivery.
  - Compose test API v1 emits a deprecation warning; kept for compatibility,
    migration tracked in backlog.
  - Home bottom footnote can require a small scroll on 1080x2400 with large
    system font; will re-check in Phase 6.
- 下一步：Phase 2 — JellyPhysics, unit tests, Canvas gestures and feedback.
