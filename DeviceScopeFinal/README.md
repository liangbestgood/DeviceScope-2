# DeviceScope v1.0.0

Android 16 device monitoring app. Target/compile SDK 36.

## Features
- CPU overall and per-core load
- RAM usage
- Battery percentage and estimated instantaneous power
- GPU renderer
- Android 16 CPU/GPU headroom when supported
- Game overlay via foreground special-use service
- Device report export
- Dark custom UI

## Build
AGP 8.9.1 with Gradle 8.11.1. Android API 36 requires AGP 8.9.1 or newer.

The included GitHub Actions workflow builds the debug APK without Android Studio.
