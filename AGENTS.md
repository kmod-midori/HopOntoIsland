# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew app:assembleDebug                         # Build debug APK
./gradlew app:assembleRelease                       # Build release APK (needs signing.properties + keystore.jks)
./gradlew :app:lintReportDebug                      # Lint (produces XML report)
adb install app/build/outputs/apk/debug/app-debug.apk  # Install on device
```

There are no test sources in this project.

## Architecture

**Hop Onto Island** is an Android app (Kotlin + Jetpack Compose, Material 3) that uses an Accessibility Service to capture on-screen text and sends it to an LLM for extraction, displaying results as Android Live Update notifications (status bar chips / lock screen).

- **Min SDK 36 / Target SDK 37** | JDK 11 for compile | Gradle 9.4.1, AGP 9.2.1, Kotlin 2.4.0
- Single module (`app`), package `moe.reimu.hopontoisland`
- Dependencies managed via Gradle version catalog at `gradle/libs.versions.toml`
- HTTP client: Ktor 3.5.x with OkHttp engine + kotlinx.serialization JSON

### Data Flow

```
Capture trigger (shortcut / Quick Settings tile / share intent / broadcast)
  → TextAccessibilityService (Accessibility Service — reads screen text from node tree)
  → RecognitionProcessor (builds LLM prompt, calls ApiModelProvider)
  → ApiModelProvider (HTTP POST to LLM — OpenAI-compatible / Gemini / Alibaba Cloud)
  → NotificationUtils.postLiveUpdate() (ongoing notification with setShortCriticalText)
```

### Key Source Files

| Path | Role |
|---|---|
| `MainActivity.kt` | Settings UI (provider, URL, API key, model name, test button) |
| `MyApplication.kt` | App init: Ktor client, notification channels, SharedPreferences wrapper |
| `TextAccessibilityService.kt` | Accessibility service — captures text from active window, triggers recognition |
| `RecognitionProcessor.kt` | Builds LLM prompt from captured text, calls ApiModelProvider, parses response |
| `CaptureActivity.kt` | Transparent activity that triggers capture (shortcut target) |
| `CaptureTileService.kt` | Quick Settings tile — taps trigger capture |
| `ShareActivity.kt` | Handles `ACTION_SEND` intents — posts shared text as Live Update |
| `NotificationUtils.kt` | `postLiveUpdate()`, `clearLiveUpdate()`, `postErrorNotification()` |
| `llm/ApiModelProvider.kt` | Ktor HTTP client calling OpenAI-compatible chat completions |
| `llm/ModelProvider.kt` | Interface for LLM providers |
| `model/` | `ModelRequest`, `ModelResponse`, `ModelMessage`, `RecognizedEntity` |
| `utils/Settings.kt` | SharedPreferences-backed settings (provider, key, URL, model name) |
| `utils/PreviewSettings.kt` | Stub settings for Compose previews |
| `ui/theme/` | Material 3 theme with dynamic color (Android 12+), purple/pink palette |

### Notification Channels

- `"default"` — general notifications
- `"liveUpdate"` — `IMPORTANCE_MAX`, uses `setShortCriticalText()` for status bar chips

### LLM Provider Support

Three providers in `ApiModelProvider`, selected by the `modelProvider` setting:
- `openai` — user-supplied URL
- `gemini` — user-supplied URL (default model: `gemini-2.5-flash`)
- `aliyun_cn` — Alibaba Cloud DashScope endpoint

All use the OpenAI-compatible `/v1/chat/completions` API format with Bearer auth.

### i18n

Three string resource sets: `values/` (English), `values-zh-rCN/` (Simplified Chinese), `values-zh-rHK/` (Traditional Chinese HK).

## CI

- **Debug** (`.github/workflows/commit.yml`): Runs on every push (excluding `.md`), JDK 17, assembles debug APK + lint
- **Release** (`.github/workflows/release.yml`): Runs on `v*` tags, decodes signing secrets from GitHub Actions secrets, builds signed release APK, creates draft GitHub Release

## Release Signing

Release builds read `signing.properties` (gitignored) at the repo root for keystore credentials. In CI, `KEYSTORE` (base64) and `KEYSTORE_PROPERTIES` are decoded from GitHub Secrets.
