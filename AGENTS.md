# AGENTS.md — TouchLab

Guidance for AI coding agents working in this repository.

## What this is

**TouchLab** — a native Android "pocket tactile physics laboratory" with three
toy screens (Jelly, Magnet Dust, Marbles), bilingual EN/zh-CN UI, and offline-only
behavior. Published as a signed APK through GitHub Actions releases.

The authoritative product/engineering spec is
`TouchLab-Android-Agent-Development-Guide.md` (Chinese). Read it before large
changes. Phase-by-phase history and evidence live in `docs/PROGRESS.md`.

## Stack (verified, do not casually bump)

| Piece | Value |
|---|---|
| Package / appId | `dev.touchlab.app` |
| min / target / compile SDK | 26 / 36 / 37 (`platforms;android-37.0`) |
| Gradle wrapper | 9.4.1 |
| AGP | 9.2.1 with **built-in Kotlin** (never apply `org.jetbrains.kotlin.android`) |
| Kotlin (effective) | 2.2.10; Compose compiler plugin 2.2.10 |
| Compose BOM | 2026.09.00 |
| JDK / jvmTarget | 17 |
| Build tools | 37.0.0 |

## Build & verify (Windows Git Bash on this machine)

```bash
export JAVA_HOME='C:\Users\luxia\programe\java\zulu17.34.19-ca-jdk17.0.3-win_x64'
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew.bat :app:testDebugUnitTest :app:assembleDebug
./gradlew.bat :app:lintDebug
./gradlew.bat :app:assembleRelease      # unsigned unless ANDROID_KEYSTORE_* env is set
```

`local.properties` must escape the drive colon: `sdk.dir=C\:/Users/.../Android/Sdk`
(backslashes / unescaped `C:` break lint + AGP). It is git-ignored.

Always run `testDebugUnitTest` + `assembleDebug` after code changes, and
`lintDebug` before committing release-related work. Lint must stay at 0 errors.

## Code map

```
app/src/main/java/dev/touchlab/app/
├── MainActivity.kt            # edge-to-edge + locale-wrapped base context
├── ui/                        # theme, Toy enum, TouchLabApp (navigation), components
├── feature/home/              # home cards
├── feature/play/              # PlayShell dispatch + PhysicsFrameLoop (shared loop)
├── feature/jelly/             # JellyPhysics (pure) + JellyScreen (canvas + gestures)
├── feature/dust/              # DustPhysics + DustScreen
├── feature/marbles/           # MarblesPhysics + MarblesScreen
├── feedback/                  # SoundEngine (SoundPool), Haptics, UI feedback helper
├── settings/                  # DataStore repository + settings bottom sheet
└── localization/              # AppLanguage (system/中文/English override)
```

**Physics classes are pure Kotlin (no Android imports)** and must stay that way so
they can be covered by JVM tests in `app/src/test/java/dev/touchlab/app/physics/`.

## Non-obvious rules

1. **Frame loop**: use `rememberPhysicsFrameCounter` / `rememberSimulationRunning`.
   It runs a fixed 1/120 s step, clamps real dt to 50 ms, and limits to 6 substeps.
   The returned `State<Int>` may only be read **inside the Canvas draw lambda** —
   reading it during composition causes a recomposition every frame.
2. **Drawing**: one `Canvas` per toy. Never create a composable per particle/marble.
3. **Numbers are bounded**: every physics step clamps dt, velocity, displacement;
   guards against NaN/Inf; fixed particle counts. Do not remove those clamps.
4. **Strings**: every user-visible string lives in
   `res/values/strings.xml` (English) *and* `res/values-zh-rCN/strings.xml`.
   `Toy` holds `@StringRes` ids, not literals. Adding a string means adding both.
5. **Locale**: `MainActivity.attachBaseContext` wraps the context with
   `AppLanguage.wrap`; the choice is stored in SharedPreferences
   (`touchlab_language`) and applied by recreating the activity. Keep this hook.
6. **Settings** (sound / haptics / reduce motion) live in DataStore
   (`touchlab_settings`). Audio must degrade silently if SoundPool fails; haptics
   are rate-limited (55/130 ms) and gated by the setting.
7. **Offline only**: no network permission, no analytics, no accounts, no ads.

## Testing conventions

- JVM tests assert physics invariants (bounds, no NaN, energy not increasing,
  determinism, reset, dt clamping). Add a regression test with each bug fix.
- Compose UI tests use `testTag`s (`home_*_card`, `play_canvas`, `play_pause`,
  `play_reset`, `setting_*`, `settings_sheet`, ...). Assertions must be
  **locale-agnostic**: use `composeRule.activity.getString(R.string.…)`, never
  hardcoded English text (the device may run in Chinese).
- Toy screens animate forever, so tests that open a toy set
  `mainClock.autoAdvance = false` and call `advanceTimeBy(...)` manually.
- On physical devices, also set the three system animation scales to 0 for the
  run, and restore afterwards.

## Real-device testing (Xiaomi / MIUI)

- MIUI blocks an instrumentation-launched activity unless the app has the
  per-app **「后台弹出界面」= 允许** (Settings → Apps → TouchLab → Permissions).
  This permission is **lost after every reinstall** (UID changes), so re-grant
  after installing a new APK, then run `am instrument` repeatedly without
  reinstalling.
- Wireless debugging worked around a flaky USB: `adb pair host:port` then
  `adb connect host:connectPort`. A previously used device serial is
  `192.168.1.8:43281` (POCO F5, Android 14) — never assume it is connected;
  check `adb devices -l` first and always target with `adb -s` / `ANDROID_SERIAL`
  when the emulator is also online.
- Screenshots and evidence: `docs/screenshots/`, reports in
  `docs/TEST-REPORT.md` / `docs/KNOWN-ISSUES.md`.

## Release / CI

- `.github/workflows/android-ci.yml` runs unit tests + lint + debug build on
  every push/PR.
- `.github/workflows/release.yml` runs on `v*` tags: builds release + debug APKs
  and creates a GitHub Release with them attached. Signing is enabled when the
  `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
  `ANDROID_KEY_PASSWORD` repository secrets exist.
- The release keystore lives in `.release/` (git-ignored) together with
  `credentials.txt`. **Never commit or print keystore material.** Losing it
  breaks future updates of the same signature.
- Shipping to Google Play additionally needs a signed AAB, Play Console setup and
  package-name verification — see `docs/RELEASE.md`.

## Git

- Windows commits can lose the executable bit; if CI reports
  `./gradlew: Permission denied`, run
  `git update-index --chmod=+x gradlew` and push.
- Commit style follows the guide: `feat:`, `fix:`, `style:`, `test:`, `ci:`,
  `docs:`, `chore:`.
- `docs/PROGRESS.md` must be updated with commands/evidence when a phase or a
  notable change lands.
