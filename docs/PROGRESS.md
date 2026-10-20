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

---

## Phase 2 — Jelly physics toy
- 状态：DONE
- 实现内容：
  - `JellyPhysics`: pure Kotlin, 24-point closed outline, per-point damped
    spring, gaussian local indentation, volume bulge on the far side, independent
    center spring-follow with grab offset, boundary clamping, displacement and
    velocity limits, NaN guards, reset and resize.
  - `JellyScreen`: 120Hz fixed-step simulation driven by `withFrameNanos`,
    frame dt clamped to 50ms with max 6 substeps, lifecycle RESUMED gating,
    pause/resume, reset, first-touch hint dismissal.
  - Smooth closed Catmull-Rom-derived cubic path, mint radial body, shadow,
    clipped highlights, contact soft spot.
  - Haptics: one tap on press, one on release (never per frame), gated by the
    user setting.
  - Real gesture handling with `awaitEachGesture` down/move/up/cancel and
    explicit consumption; paused canvas does not accept input.
- 修改文件：
  - `feature/jelly/JellyPhysics.kt`, `feature/jelly/JellyScreen.kt`
  - `feedback/Haptics.kt`, `feature/play/PlayShell.kt`
  - `app/src/test/java/dev/touchlab/app/physics/JellyPhysicsTest.kt`
- 验证命令与退出码：
  - `./gradlew.bat :app:testDebugUnitTest :app:assembleDebug` → 0
    - 8 Jelly unit tests, 0 failures.
  - Installed on `touchlab_api34` and interacted via adb motion events.
- 证据：
  - Unit XML: `app/build/test-results/testDebugUnitTest/TEST-dev.touchlab.app.physics.JellyPhysicsTest.xml`
  - `docs/screenshots/phase2-jelly-rest.png`
  - `docs/screenshots/phase2-jelly-press.png`
- 已知问题：
  - The local indentation at the pressed edge is visible but subtle on a
    ～110dp-radius blob; amplitude will be re-tuned in Phase 6 visual polish.
  - No sound yet (Phase 5).
- 下一步：Phase 3 — Magnet Dust.

---

## Phase 3 — Magnet Dust physics toy
- 状态：DONE
- 实现内容：
  - Shared `PhysicsFrameLoop`: 120Hz fixed step, 50ms frame clamp, max 6
    substeps, lifecycle RESUMED gating, frame counter read only in draw phase.
  - Refactored Jelly to the shared loop.
  - `DustPhysics`: 120 fixed particles, deterministic LCG seed 42, softened
    inverse-square attraction, tangential swirl, soft core repulsion (halo
    instead of a single-pixel collapse), idle sine drift, damping, speed and
    acceleration clamps, wall bounce, finite guards, resize scaling, reset.
  - `DustScreen`: single Canvas for all 120 particles, short velocity trails,
    touch halo + ring, reduce-motion lowers drift/trail and white glints.
  - Haptic press/release only, no per-frame haptics.
- 修改文件：
  - `feature/play/PhysicsFrameLoop.kt`, `feature/dust/DustPhysics.kt`,
    `feature/dust/DustScreen.kt`, `feature/jelly/JellyScreen.kt`,
    `feature/play/PlayShell.kt`
  - `app/src/test/java/dev/touchlab/app/physics/DustPhysicsTest.kt`
- 验证命令与退出码：
  - `./gradlew.bat :app:testDebugUnitTest :app:assembleDebug` → 0
    - 19 total unit tests (8 Jelly + 11 Dust), 0 failures.
  - Installed and interacted on `touchlab_api34`.
- 证据：
  - Unit XML: `app/build/test-results/testDebugUnitTest/TEST-dev.touchlab.app.physics.DustPhysicsTest.xml`
  - `docs/screenshots/phase3-dust-idle.png`
  - `docs/screenshots/phase3-dust-gather.png`
- 已知问题：
  - Idle dust is intentionally sparse; brightness will be reviewed in Phase 6.
  - No sound yet (Phase 5).
- 下一步：Phase 4 — Marbles.

---

## Phase 5 — Sound, haptics, settings, lifecycle
- 状态：DONE
- 实现内容：
  - `tools/generate_sounds.py`: five original 16-bit WAVs synthesized with the
    Python standard library, committed under `res/raw/` (jelly press/release,
    dust touch, marble hit, ui tap).
  - `SoundEngine`: SoundPool with USAGE_GAME, per-cue rate limiting (50–110ms),
    silent graceful degradation, DEBUG-only play logs.
  - Haptics: press/release/impact with internal rate limits (55/130ms), gated
    by the user setting, no per-frame vibration.
  - Wired sounds + haptics into all three toys, buttons, cards and settings.
  - Marbles physics now reports impact strength per frame (`consumeImpact`) for
    sound/haptic on collisions while still bounded by rate limits.
  - DataStore settings (sound/haptics/reduce motion) fully effective and wired;
    reduce motion also modulates Jelly spring amplitude and Dust drift/trails.
  - Lifecycle handling: physics loop pauses on background/pause/settings sheet;
    verified 10 home/resume cycles without crash or ANR.
- 验证命令与退出码：
  - `./gradlew.bat :app:testDebugUnitTest :app:assembleDebug` → 0
  - `./gradlew.bat :app:connectedDebugAndroidTest` → 0 (9/9), including new
    all-three-switches settings test.
  - Manual on emulator: sound logs appear when enabled and stop when disabled;
    settings persist after force-stop + relaunch (screenshot evidence).
  - Emulator has no usable vibrator; haptic calls execute but hardware feedback
    is recorded as unverified on this environment.
- 证据：
  - `docs/screenshots/phase5-settings-toggled.png`,
    `phase5-settings-persisted.png`
  - `tools/generate_sounds.py`, `app/src/main/res/raw/*.wav`
- 已知问题：
  - Emulator cannot reproduce audible/haptic hardware output; to be verified on
    a real phone before Play release.
- 下一步：Phase 6 — visual and adaptive-layout polish.

## Phase 6 — UI/UX polish and layout adaptation
- 状态：DONE
- 实现内容：
  - Adaptive home cards: height grows with system font scale (168→232dp),
    titles wrap up to 2 lines, removed the redundant "Tap to play" label.
  - Play top bar adapts to 2-line titles and large fonts.
  - Settings sheet is scrollable for large fonts; reduce-motion respected.
  - Jelly: stronger local press indentation, softer secondary highlight.
  - Marbles: lighter contact shadows.
  - Android 12+ splash theme with the adaptive icon on a branded background.
- 验证命令与退出码：
  - `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest` → 0
  - Manual on emulator at 360dp width + font scale 1.4: home renders, three
    cards visible, no truncation of "Magnet Dust", layout scrolls (uiautomator
    bounds captured), then device restored.
- 证据：
  - `docs/screenshots/home-narrow-font.png` (360dp + 1.4 font)
  - `docs/screenshots/home.png`, `phase2-jelly-*-final.png`,
    `phase3-dust-*-final.png`, `phase4-marbles-motion-final.png`,
    `phase4-marbles-floating-final.png`, `settings-final.png`
- 已知问题：
  - Splash/thermal details still to be sanity-checked on first cold launch on a
    real device.
- 下一步：Phase 7 — full test matrix, release build, final reports.

---

## Phase 7 — Tests, performance evidence, release prep
- 状态：PARTIAL（DONE 条件因无真机不能全部满足）
- 实现内容：
  - Ran the full matrix: `assembleDebug`, `testDebugUnitTest` (31/31),
    `lintDebug` (0 errors), `connectedDebugAndroidTest` (9/9),
    `assembleRelease` (unsigned APK).
  - Fixed lint `PropertyEscape` error in `local.properties`
    (`sdk.dir=C\:/Users/...`), Modifier-ordering warning, removed unused
    drawable; adaptive-icon folder kept at v26 as required by AAPT.
  - Stability: scripted interactions (Jelly drag loops, 30s Dust hold, Marbles
    flicks, toggles/resets) with no FATAL/ANR; rotation safe; 10 background
    round trips safe.
  - Captured final screenshots incl. 360dp + font 1.4; recorded emulator-only
    `gfxinfo` numbers for the record.
  - Wrote `docs/TEST-REPORT.md` and `docs/KNOWN-ISSUES.md`; updated README.
- 验证命令与退出码：
  - `:app:testDebugUnitTest` → 0（31 用例，0 失败）
  - `:app:lintDebug` → 0（0 error；预期 warning 若干）
  - `:app:connectedDebugAndroidTest` → 0（9/9）
  - `:app:assembleRelease` → 0（`app-release-unsigned.apk`，未签名）
- 证据：
  - 见 `docs/TEST-REPORT.md` 各表与 `docs/screenshots/`。
- 已知问题：
  - 无真机：触觉/音效/真机性能未验证；API 37 与 minSdk 26 设备未实测
    （详见 `docs/KNOWN-ISSUES.md`）。
  - Release APK 未签名；包名唯一性/归属需上架前确认。
- 下一步：
  - 真机接入后：重跑 connected 测试、实测音效/震动、跑 Macrobenchmark，
    完善 `TEST-REPORT` 的性能与设备覆盖部分，将判定升级为 DONE。
