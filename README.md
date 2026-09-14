# Speedometer

A free, ad-free, no-tracking GPS speedometer for Android. Native Kotlin + Jetpack Compose.

## Features

- Large digital speed readout with an animated analog gauge dial
- mph ⇄ km/h toggle (remembered between launches)
- Max and average speed, with a reset button
- GPS status / accuracy indicator ("Acquiring GPS…", "GPS locked ±5 m", "Weak signal")
- Keeps the screen on while running
- **No ads, no analytics, no internet permission at all** — speed is read purely from the on-device GPS

## Requirements

- Android Studio (bundled JDK 21 is used for command-line builds)
- Android SDK Platform 34, Build-Tools 34.0.0+
- A device/emulator running **Android 8.0 (API 26)** or newer

## Project layout

```
app/src/main/
├── AndroidManifest.xml
├── java/com/spritzlive/speedometer/
│   ├── MainActivity.kt        # permission flow, keep-screen-on, wiring
│   ├── SpeedViewModel.kt      # tracks current/max/avg speed in SI units
│   ├── LocationProvider.kt    # FusedLocationProvider -> Flow<SpeedSample>
│   ├── SpeedPrefs.kt          # persisted unit setting + conversion factors
│   └── ui/
│       ├── SpeedScreen.kt     # main screen
│       ├── SpeedGauge.kt      # Canvas-drawn analog gauge
│       ├── PermissionScreen.kt
│       └── Theme.kt
└── res/                       # theme, colors, adaptive launcher icon
```

## Building from the command line

The machine's default `java` is a JRE 1.8, which cannot build. Point `JAVA_HOME`
at Android Studio's bundled JDK 21 first:

```bash
# Git Bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew.bat :app:assembleDebug
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Install on a connected device/emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or just open the folder in Android Studio and press ▶.

## Release signing (already set up)

Release signing is configured and working:

- **`upload-keystore.jks`** — RSA 2048, alias `upload`, 10,000-day validity.
- **`keystore.properties`** — holds the store/key passwords. Both files are
  **git-ignored**.

> ⚠️ **Back up `upload-keystore.jks` and `keystore.properties` now** (password
> manager / offsite). With Google Play App Signing an upload key can be reset if
> lost, but keeping your own backup avoids the hassle.

Build the signed artifacts:

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew.bat :app:bundleRelease   # -> app/build/outputs/bundle/release/app-release.aab  (upload this)
./gradlew.bat :app:assembleRelease # -> app/build/outputs/apk/release/app-release.apk      (for sideload testing)
```

Verify a build's signature:

```bash
"$ANDROID_HOME/build-tools/35.0.0/apksigner.bat" verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## Publishing to Google Play

1. Create a [Play Console](https://play.google.com/console) account ($25 one-time).
2. Create the app, then upload **`app-release.aab`**.
3. Keep **Google Play App Signing** enabled (the default) — Google manages the
   final signing key; your `upload-keystore.jks` is only the upload key.
4. Complete the **Data Safety** form — trivial here: the app collects nothing and
   has no internet permission. Location is used on-device only.
5. Roll out to **internal testing** first, then production.

## Notes

- `applicationId` is `com.spritzlive.speedometer`. It is **permanent once published** —
  change it now if you want a different package name.
- GPS speed is most accurate outdoors with a clear sky view; indoors it may read 0
  or show "Weak signal".
