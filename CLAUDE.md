# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

HRAM is a Kotlin Multiplatform (KMP) heart rate monitoring app using Compose Multiplatform for
shared UI across Android and iOS. It connects to BLE heart rate monitors, tracks activity sessions,
and displays live metrics.

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# iOS framework (called by Xcode build phase)
./scripts/build-framework.sh [SDK] [CONFIGURATION]
./scripts/build-xcode.sh [SDK] [CONFIGURATION] [SIMULATOR_NAME]

# Tests
./gradlew testDebugUnitTest
./gradlew :ble:testAndroidHostTest --tests "com.achub.hram.ble.core.connection.HramConnectionTrackerTest"

# Code quality
./gradlew detekt
./gradlew koverHtmlReport   # Coverage report (80% minimum enforced)
```

## Architecture

### Layer Overview

```
Shared UI (Compose Multiplatform)
    → ViewModel (commonMain)
    → TrackingController (expect/actual per platform)
         → Android: Intent → BleTrackingService (Foreground Service)
         → iOS: ActivityTrackingManager + LiveActivityManager
    → ActivityTrackingManager ← BLE (Kable) + Room Database
```

### Source Set Organization

- **`ble/`** — Standalone BLE module (scanning, connection, data parsing, models, DI)
    - `src/commonMain/` — Shared BLE interfaces and implementations
    - `src/androidMain/` — Android Bluetooth state observer
    - `src/iosMain/` — iOS CoreBluetooth state observer
    - `src/commonTest/` — BLE unit tests
- **`build-logic/`** — Gradle convention plugins for reusable KMP module config
    - `convention/` — Precompiled script plugins (`kmp-library-convention`, `cmp-ui-lib-convention`,
      `koin-convention`, `quality-convention`, `test-mocking-convention`)
    - `shared-sources/` — Kotlin source injected into `commonMain` of every module that applies
      `test-mocking-convention`. Contains `OpenForMokkery.kt` (`com.achub.hram` package) — the
      annotation that marks classes to be opened by the `allOpen` compiler plugin for Mokkery
      mocking. Compiled per-module so it works on all KMP targets (JVM/Android and K/N/iOS).
- **`composeApp/src/commonMain/`** — Shared business logic and UI (depends on `:ble`)
- **`composeApp/src/androidMain/`** — Android-specific implementations
- **`composeApp/src/iosMain/`** — iOS-specific implementations (Kotlin/Native)
- **`composeApp/src/commonTest/`** — Shared unit tests
- **`androidApp/`** — Android shell (MainActivity)
- **`iosApp/`** — iOS shell (SwiftUI entry point, Live Activity widgets)

### Key Packages

#### `:ble` module

| Package                | Purpose                                                    |
|------------------------|------------------------------------------------------------|
| `ble/core/connection/` | BLE scanning, connection, reconnect retry (max 3 attempts) |
| `ble/core/data/`       | BLE characteristic parsing, HR + battery streams           |
| `ble/models/`          | BLE device, notification, and exception models             |
| `ble/di/`              | Koin DI modules for BLE (expect/actual per platform)       |
| `ble/utils/`           | BLE UUID constants, byte parsing, logging utilities        |

#### `composeApp` module (commonMain)

| Package       | Purpose                                                                      |
|---------------|------------------------------------------------------------------------------|
| `data/db/`    | Room database with heart rate and activity entities                          |
| `data/repo/`  | Repository layer (HR activities, BLE state, tracking state)                  |
| `data/store/` | DataStore-backed persistence for BLE and tracking state                      |
| `tracking/`   | `ActivityTrackingManager`, `StopWatch`, `TrackingController` (expect/actual) |
| `screen/`     | UI screens: `main/`, `activities/`, `record/`                                |
| `view/`       | Reusable Compose components, charts, AGSL shaders                            |
| `di/`         | Koin DI modules — most have platform-specific counterparts                   |
| `export/`     | CSV data export (platform-specific implementations)                          |

### Platform Differences

**Android** (`androidMain/`):

- `BleTrackingService` — Foreground service for background tracking
- `TrackingController` sends Intents to the service
- `Notificator` manages persistent notification with RemoteViews

**iOS** (`iosMain/` + `iosApp/`):

- `LiveActivityManager` updates WidgetKit Live Activities during tracking
- `TrackingController` directly calls `ActivityTrackingManager`
- Swift bridge in `iosApp/Bridge/` connects Kotlin to Swift Live Activity APIs
- Live Activity UI defined in Swift: `iosApp/HramLiveActivity/`

### Dependency Injection

Koin with KSP annotations. Each platform has companion DI modules for database, DataStore, BLE,
tracking, and export. Entry point: `di/Koin.kt`.

### BLE Device Identifier Note

`BleDevice.identifier` is a MAC address on Android but a CoreBluetooth UUID on iOS.

## Tech Stack

- Kotlin 2.3.20, Compose Multiplatform 1.10.3
- BLE: Kable v0.42.0
- Database: Room KMP v2.8.4
- DI: Koin v4.2.0 + Annotations + KSP
- Persistence: DataStore v1.2.1
- Testing: `kotlin.test` + Mokkery (mocking) + Kover (coverage)
- Static analysis: Detekt v1.23.8
- Logging: Napier v2.7.1

## Prerequisites

- macOS (required for iOS builds)
- JDK 17+
- Android Studio 2025.2.2 RC2+
- Xcode 26+
