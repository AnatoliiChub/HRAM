# HRAM: Heart Rate, Activity & Motion.

### Kotlin Multiplatform project targeting Android \& iOS

| **Android**                                                                                             | **iOS**                                                                                                 |
|---------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/2496db1f-9d6d-421f-b678-334ca482647f" width="280"> | <img src="https://github.com/user-attachments/assets/ed85f462-0922-47c9-82ab-8071af28d618" width="280"> |

HRAM is a Kotlin Multiplatform app for heart rate \& activity tracking with BLE heart rate monitors.  
It uses Compose Multiplatform for shared UI, Kotlin Multiplatform for shared logic, Koin for DI, and an SQL
database for storing heart rate activities.

Tested with a Decathlon HRM Belt as an example device.

Status: **Work in progress / Prototype**

## ⚠️ **Disclaimer:**

No implied warranty or guarantee of functionality. Use at your own risk.  
This project is for educational purposes in software development only.
It is **not a medical application** and must **not be used for medical assessment, diagnosis, monitoring, or treatment**.

---

## Table of Contents

- [Getting started](#getting-started)
    - [Prerequisites](#prerequisites)
- [Run targets](#run-targets)
    - [Android](#android)
    - [iOS](#ios)
- [Testing](#testing)
    - [Running Tests](#running-tests)
- [Project description](#project-description)
- [Project structure](#project-structure)
- [Implemented features](#implemented-features)
    - [BLE](#ble)
    - [Tracking](#tracking)
    - [Data layer & database](#data-layer--database)
    - [UI & screens](#ui--screens)
    - [Dependency injection](#dependency-injection)
- [Tech stack](#tech-stack)
- [Current limitations](#current-limitations)
- [Video Demo](#video-demo)

---

## Getting started

### Prerequisites

- macOS
- JDK 17\+
- Android Studio Otter 2 Feature Drop \| 2025\.2\.2 RC 2 or newer
- Xcode 26\+
- `git`

## Run targets

### Android

Open HRAM in Android Studio. Select the Android configuration for composeApp. Choose a
device/emulator. Run.
Useful tasks:

- `./gradlew :composeApp:assembleDebug`
- `./gradlew :composeApp:installDebug`

### iOS

1. Open iosApp/iosApp.xcodeproj in Xcode.
2. Select a simulator.
3. Run.

To create a build for a real device run in terminal:

`xcodebuild  -project iosApp/iosApp.xcodeproj -configuration Debug -scheme iosApp -sdk iphoneos  DEVELOPMENT_TEAM=“YOUR_DEVELOPMENT_TEAMID”  CODE_SIGN_STYLE=Automatic CODE_SIGN_IDENTITY="Apple Development" -verbose`

Just replace `YOUR_DEVELOPMENT_TEAMID` with your team ID.

---

## Testing

Unit tests for the shared logic are located in `composeApp/src/commonTest`. The project utilizes the following testing libraries:

- **`kotlin.test`**: For standard assertions.
- **`kotlinx-coroutines-test`**: For testing coroutine-based asynchronous code.
- [**`Mokkery`**](https://mokkery.dev/): For creating mocks and stubs of dependencies.

### Running Tests

To execute all tests in the `composeApp` module, run:

`./gradlew testDebugUnitTest`

To run a specific test class:

`./gradlew :composeApp:testDebugUnitTest --tests "com.achub.hram.ble.core.connection.HramConnectionTrackerTest"`

**Test coverage** is generated using [Kover](https://github.com/Kotlin/kotlinx-kover)
To generate an HTML coverage report, run the following command:

`./gradlew koverHtmlReport`


## Project description

HRAM focuses on:

- Discovering, connecting, and reading data from BLE heart rate devices.
- Tracking heart rate sessions basic info.
- Visualizing heart rate data using charts and indication views.
- Storing activity data in a local database.
- Sharing core logic (tracking, BLE, database, view models) between Android and iOS.

For compatibility, devices must implement the standard Heart Rate Service (UUID: 0x180D).

---

## Project structure

The project is organized into packages, with the core logic residing in `composeApp/src/commonMain`.

- `composeApp/src/commonMain/kotlin/com/achub/hram/`
    - `ble/` - BLE scanning, connection, and data handling.
    - `data/` - Database entities, DAOs, and activity repository.
    - `di/` - Koin dependency injection modules.
    - `screen/` - screens for each feature (Main, Activities, Record).
    - `tracking/` - Business logic for managing activity tracking sessions.
    - `view/` - Reusable UI components like charts and dialogs.

---

## Implemented features

### BLE

- BLE Layer is implemented in `hram/ble`:
    - The app communicates with BLE devices that implement the standard Heart Rate Service.
    - `BleDevice` model describing discovered devices; `identifier` field used for mac address (Android) or UUID (iOS).
    - `HrNotification` model for heart rate data (from the Heart Rate Measurement characteristic).
    - `BleNotification` encapsulates `HrNotification`, battery level, and BLE connection status.
    - Core components:
        - `BleConnectionManager`: Manages Bluetooth state, device scanning, and the connection lifecycle (
          connect/disconnect/reconnect).
        - `BleDataRepo`: Provides streams for BLE characteristic data (heart rate measurement, battery level).
        - `HrDeviceRepo`: A high-level repo that coordinates the `BleConnectionManager` and `BleDataRepo` to
          provide a unified interface for interacting with HR devices.

**What implemented:**

- Scanning for and managing BLE heart rate devices.
- Connecting to devices and receiving heart rate notifications.
- Reconnecting on disconnection with retry logic.
- Parsing low-level BLE data.

**BLE Connection and Data flow:**

```mermaid
flowchart LR
    A["SCANNING <br> ads"] -->|filter<br>HR Service| B[DEVICE<br>SELECTED]
    B -->|identifier| C[CONNECT<br>Peripheral]
    C --> D[SUBSCRIBE:<br>HRM 0x2A37,<br>Battery 0x2A19]
    D --> E[PARSE<br>NOTIFICATIONS]
    E --> F[UI<br>or<br>DATABASE]
```

**BLE reconnection flow:**

```mermaid
flowchart TD
    A[Observe device connection state] --> |new state| B{State == Connected or Connecting?}
    B -->|YES| A
    B -->|No| D[Peripheral disconnect]
    D --> E[Peripheral connect]
    E --> F{Connected?}
    F -->  |YES| A
    F -->  |NO, attempts <= 3| E
    F -->  |NO, attempts >3| G[CONNECTION FAILED]
```

---

### Tracking

Tracking is implemented in `hram/tracking`:

- `ActivityTrackingManager` \- interface for activity tracking.
    - Start, pause, resume, stop tracking.
    - Integration with BLE data streams and activity repository.
    - Core stopwatch logic abstracted in `StopWatch`.

**What works:**

- Heart rate session lifecycle (start/pause/stop etc.).
- Time tracking for each session.
- Combined use of BLE data and stopwatch inside tracking manager.

---

### Data layer \& database

Located under `hram/data`:

- Activity repositories:
    - `HrActivityRepo` stores \& retrieves HR activities.
- Database:
    - `HramDatabase` in `hram/data/db`.
    - Heart rate and activity entities.
    - Queries to read \& write activity data.
    - Optimized heart rate aggregation per activity: splits sessions into time buckets and calculates average heart rate
      directly in the database for fast, efficient queries.

**What works:**

- Persisting activity data (heart rate sessions) locally.
- Querying history via the repository layer.

---

### UI \& screens

Common UI code lives under `hram/screen` and `hram/view`:

- Screens:
    - `screen/main`:
        - Entry Compose screen(s) for main app navigation.
    - `screen/activities`:
        - Screens for listing activities and viewing details (history).
        - `hram/view/chart` \- chart components for visualizing HR/metrics.
    - `screen/record`:
        - Screens for recording an activity (live HR, timer, etc.).

**What works:**

- Compose-based UI shared across platforms.
- Custom charts for heart rate data.
- Custom components using new Material 3 Expressive
- Localization support for English and Ukrainian.

**Activities Screen:**

| iOS                                                                                                     | Android                                                                                                 |
|---------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/692d93cc-8714-4570-acad-ea2e8f8a4ea0" width="280"> | <img src="https://github.com/user-attachments/assets/38342965-2064-4668-866e-b0246ee62e5a" width="280"> |

**Record Screen:**

| iOS                                                                                                     | Android                                                                                                 |
|---------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/c7a61873-c63c-4ead-9355-360d5b069e79" width="280"> | <img src="https://github.com/user-attachments/assets/fcd85206-a383-4fea-803d-87aa671f6de3" width="280"> |

---

### Dependency injection

Dependency injection is implemented using Koin under `hram/di`:

- `Koin.kt` \- starting point for DI initialization.
- `AppModule.kt` \- app-level bindings.
- `ViewModelModule.kt` \- registrations for view models.
- `TrackingModule.kt` \- bindings for tracking manager
- `BleModule.kt`, `BleDataModule.kt` in - BLE-specific bindings.
- `DatabaseModule.kt`, `DataModule.kt` in - database and repository bindings.
- `UtilsModule.kt` \- utility bindings.
- `DatabaseModule.kt` and `BleModule.kt` provide platform-specific implementations where needed.

---

## Tech stack

| Category                 | Technology                                                            |
|:-------------------------|:----------------------------------------------------------------------|
| **Language**             | Kotlin (Multiplatform), Swift (iOS Shell)                             |
| **UI**                   | Compose Multiplatform                                                 |
| **Architecture**         | MVVM, Repository Pattern                                              |
| **Dependency Injection** | Koin Annotations                                                      |
| **Permissions**          | [moko-permissions](https://github.com/icerockdev/moko-permissions)    |
| **Persistence**          | Room (KMP)                                                            |
| **BLE**                  | [Kable](https://github.com/JuulLabs/kable)                            |
| **Logging**              | [Napier](https://github.com/AAkira/Napier)                            |
| **Testing**              | kotlin.test, kotlinx-coroutines-test, [Mokkery](https://mokkery.dev/) |
| **Code Coverage**        | [Kover](https://github.com/Kotlin/kotlinx-kover)                      |

---

## Current limitations

- No background activity tracking is wired yet - tracking works while the app is active.
- No external cloud sync/export.
- Limited error handling and UX for BLE edge cases.

## Video Demo

### Android:

https://github.com/user-attachments/assets/62cf2b3e-e3a5-4052-bfd2-d4797b415d2d

### iOS:

https://github.com/user-attachments/assets/a1b7f320-824e-4ba9-ae1e-bdf70a293b23

