# MYRAA — Android Native

MYRAA is a warm, voice-first phone assistant. This repository is a **native Android
rewrite** of the original MYRAA app. The original was a Capacitor / React web app;
this project is a proper Android-native (Kotlin + Jetpack Compose) application that
keeps the character and the core features, and drops the desktop tooling.

## What's inside

- **Kotlin + Jetpack Compose**, Android Studio compatible, Gradle 8.9 / AGP 8.7.3 / Kotlin 2.0.21.
- `minSdk 26` (Android 8.0) · `targetSdk 35` (Android 15+).
- Package / application id: `ai.myraa.native`.

## Features

### 3D character — preserved
The original character **Evelyn** is preserved exactly as shipped:

- `assets/public/characters/evelyn/model.pmx` — the 3D model (PMX 2.0)
- `assets/public/characters/evelyn/textures/` — all 9 textures + `textures.json`
- `assets/public/idle.mp4`, `talking.mp4`, `thinking.mp4` — the character's
  **idle / talking / thinking** animation clips

The character is shown on the Home screen and switches between the idle, talking
and thinking clips to match MYRAA's state (idle → talking while she speaks →
thinking while she works).

### Settings — side-by-side grid
Settings is a **2-column responsive grid** (not a long single list). Sections:
Appearance, Voice, AI Models, Permissions, Call Agent, Integrations, About.

### AI voice assistant
- Speech recognition and text-to-speech use the phone's on-device engines.
- **Hindi, Hinglish and English** are supported (switchable in Settings → Voice).
- A configurable Gemini-compatible text API powers general answers.
- **No API key is hardcoded.** The key is entered by the user and stored only in
  the device's private storage.

### Call agent
- Say *"call Rina"* — MYRAA resolves the name against your contacts (or accepts a
  spoken number).
- It always **shows the recipient and number for confirmation before dialling**.
- It then opens the dialer (or places the call directly, if you enable that in
  Settings → Call Agent).

### Other actions (modular)
- **Open any installed app** by name.
- **Reminders** scheduled with Android alarms and delivered as notifications.

### Desktop mode — removed
The original app's desktop control agent (an HTTP bridge to a Python backend with
desktop browser automation, screenshots, etc.) and every desktop-only option have
been **removed**. MYRAA is now Android-only.

## Build the APK with GitHub Actions

1. Push to `main` (or run the workflow manually).
2. Open the **Actions** tab → **Build APK** → wait for the green tick.
3. Open the run → **Artifacts → MYRAA-APK** → download and unzip `app-debug.apk`.

The workflow is at `.github/workflows/build.yml`.

## Build locally

Open the folder in Android Studio (Hedgehog or newer) and press **Run**, or:

```bash
gradle assembleDebug
```

The debug APK appears at `app/build/outputs/apk/debug/app-debug.apk`.

## First run

1. Open the app and grant the microphone, contacts, phone and notification permissions.
2. Go to **Settings → AI Models** and paste your API key
   (get a free one at https://aistudio.google.com/apikey).
3. On the **Assistant** tab, tap **Talk to MYRAA** and speak.

## Notes and honest status

- The character is displayed using the **original animation clips**. The PMX model
  and its textures are bundled and preserved, but a full native PMX skeletal
  renderer (as the original three.js scene had) is **not yet implemented** — this
  is the main follow-up item.
- This is a **debug** build (debug-signed); it is not store-ready.
- Voice recognition quality depends on the phone's speech engine.
