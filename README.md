# Nexora Dub.Ai — Clean-room Android Starter

Nexora Dub.Ai is an independent Flutter starter for an Android dubbing workspace. This repository uses its own app identity, source tree, UI and Android package, with no third-party backend configured by default.

## Identity
- App name: **Nexora Dub.Ai**
- Android package: **com.nexora.dubai**
- Version: **1.0.0+1**
- Backend: **disconnected by default**

## Current starter UI
- Home dashboard
- Projects tab
- Voices tab
- Settings page
- Placeholders for import, transcription, translation and voice workflows

## Build APK automatically
The repository includes `.github/workflows/build-apk.yml`.

Every push to `main` that changes the Flutter source starts an Android release build. You can also run it manually from GitHub **Actions → Build Nexora Dub.Ai APK → Run workflow**.

After a successful run, download the artifact named **Nexora-DubAI-Android**. It contains:
- `Nexora-DubAI-v1.0.0.apk`
- `SHA256.txt`

The workflow creates a fresh Flutter Android shell in CI, applies the Android identity `com.nexora.dubai`, builds a release APK, and publishes the result as a GitHub Actions artifact.

## Development
```bash
flutter pub get
flutter run
```

## Security
Only connect APIs, servers, voice models, media providers and other services that you own or are authorized to use. Do not put secret keys directly in this public repository.
