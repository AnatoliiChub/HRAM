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

## Getting started

### Prerequisites

- macOS
- JDK 17\+
- Android Studio Otter 2 Feature Drop \| 2025\.2\.2 RC 2 or newer
- Xcode 26\+
- `git`

## Run targets

The project is currently under active development, and only a debug version of the application is available.

### Android

Open HRAM in Android Studio. Select the Android configuration for androidApp. Choose a
device/emulator. Run.
Useful tasks:

- `./gradlew :composeApp:assembleDebug`
- `./gradlew :composeApp:installDebug`

### iOS

Open HRAM in Android Studio. Select the iOS configuration for iosApp. Choose a
device/emulator. Run.

#### Scripts

`/scripts` - This directory contains shell scripts for building the iOS application.

#### build-framework.sh
Builds the Compose Multiplatform framework for iOS.

**Usage:**
```bash
./scripts/build-framework.sh [SDK] [CONFIGURATION]
```

**Parameters:**
- `SDK` (default: `iphonesimulator`): Target SDK - `iphonesimulator` or `iphoneos`
- `CONFIGURATION` (default: `Debug`): Build configuration

**Examples:**
```bash
./scripts/build-framework.sh iphonesimulator Debug
./scripts/build-framework.sh iphoneos
./scripts/build-framework.sh  # Uses defaults
```

**Output:**
- Framework location: `composeApp/build/xcode-frameworks/{CONFIGURATION}/{SDK}/ComposeApp.framework`

---

#### build-xcode.sh
Builds the iOS Xcode project.

**Usage:**
```bash
./scripts/build-xcode.sh [SDK] [CONFIGURATION] [SIMULATOR_NAME]
```

**Parameters:**
- `SDK` (default: `iphonesimulator`): Target SDK - `iphonesimulator` or `iphoneos`
- `CONFIGURATION` (default: `Debug`): Build configuration
- `SIMULATOR_NAME` (default: `iPhone 16e`): Simulator device name (only used for iphonesimulator)

**Examples:**
```bash
./scripts/build-xcode.sh iphonesimulator Debug "iPhone 16e"
./scripts/build-xcode.sh iphoneos Debug
./scripts/build-xcode.sh  # Uses defaults
```

**Output:**
- App location: `./build/ios-{SDK}/Build/Products/{CONFIGURATION}-{SDK}/`

---

#### Complete Build Process

To build the complete iOS application, run both scripts in sequence:

```bash
# Build the framework
./scripts/build-framework.sh iphonesimulator Debug

# Build the Xcode project
./scripts/build-xcode.sh iphonesimulator Debug "iPhone 16e"
```

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
- Sharing core logic (tracking, BLE, database, view models) and UI between Android and iOS.

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

### Background mode

**Implementation Details:**

Background execution is nuanced per platform. `TrackingController` is the central entry point found in `hram/tracking/TrackingController.kt`. It provides a unified interface but has platform-specific implementations that leverage the shared `ActivityTrackingManager` and reactive state repositories:

1.  **Android**:
    - Uses a **Foreground Service** (`BleTrackingService`) to keep the app alive.
    - `TrackingController` sends Intents to the service.
    - The Service delegates work to `ActivityTrackingManager` and observes shared state repositories to update **Notifications** (remote views).

2.  **iOS**:
    - Relies on iOS background modes (CoreBluetooth).
    - `TrackingController` delegates to `ActivityTrackingManager`.
    - `TrackingController` observes shared state repositories and call `LiveActivityManager` to push updates to **Live Activities**.

```mermaid

flowchart TB
    %% Shared UI
    subgraph Shared CMP ["Shared UI (CMP)"]
        direction TB
        UI[User Command]
        subgraph VM[ViewModel]
            TC_Shared[TrackingController]
        end

        VM --> TC_Shared
        UI --> VM
    end
      
    %% Background Mode
    subgraph BG[Background Mode Implementation]
        direction LR
        %% Android Platform
        subgraph Android ["Android Platform"]
            TC_And[TrackingController <br> Android]
            Service[BleTrackingService]
              Notif[Notification <br> Manager]
          
            Service -- "Updates RemoteViews" --> Notif
        end

        %% Shared KMP
        subgraph Shared ["Shared Logic (KMP)"]
            VM[ViewModel]
            TC_Shared[TrackingController]
            ATM[ActivityTrackingManager]
            Repos[(State Repo)]

            ATM -- "Update State" --> Repos
        end

        %% iOS Platform
        subgraph iOS ["iOS Platform"]
            LAM[LiveActivityManager]
            LA[Native Live Activities <br> Swift Code]
            TC_iOS[TrackingController <br> iOS]

            TC_iOS -- "Controls" --> LAM
            LAM -- "Interop call" --> LA
        end
    end

    %% Cross-layer connections
    TC_Shared -- "Implementation" --> TC_And
    TC_Shared -- "Implementation" --> TC_iOS
    Service -- "Calls" --> ATM
    TC_iOS -- "Calls" --> ATM
    Service -. "Listen" .-> Repos
    TC_iOS -. "Listen" .-> Repos
    TC_And -. "Intents (Start/Stop)" .-> Service
```

---

### Data layer & database

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

#### StateManagement

The app persists transient state (like current BLE connection status or active tracking session info) to survive process death or navigation.
Ble-Notifications state will be extracted to in-memory singleton repo, in the near future.

**Packages:**
- `com.achub.hram.data.repo.state`: Repository interfaces and implementations for reactive state exposure.
- `com.achub.hram.data.store`: logic for serialization and storage (using DataStore).

**Key Components:**
- **BleStateRepo**: reflects the current state of BLE connection state(Disconnected, Connecting, Connected, etc.).
- **TrackingStateRepo**: Manages the state of the activity (Idle, Active, Paused).
- **DataStore**: Uses `BleStateSerializer` and `TrackingStateStageSerializer` to save state to disk asynchronously.

---

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
- Custom AGSL shaders for visual effects (Liquid Ripple for Bottom Bar, Liquid Wave for Heart animation).
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

#### Notifications / Live Activities

To keep the user informed during a workout (even when the device is locked), the app uses platform-specific ongoing notifications:

- **Android**: Custom **Notifications** with `RemoteViews`.
    - Displays real-time heart rate.
    - Includes a "breathing" animation synced to the heart rate.

- **iOS**: **Live Activities** \& Dynamic Island.
    - Implemented using SwiftUI and WidgetKit.
    - Shows heart rate, session duration, and battery level on the Lock Screen and Dynamic Island.
    - Updates are pushed from the shared Kotlin code via `LiveActivityManager`.

| iOS                                                                                                     | Android                                                                                                 |
|---------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
|  <img src="https://github.com/user-attachments/assets/2d36adf1-1771-4bde-b7c9-241383129105" width="280">| <img src="https://github.com/user-attachments/assets/bc8cb7a4-3bd0-4425-bd9a-9c0ccf6dcbc6" width="280"> |

| Dynamic Island on iOS                                                                                                             |
|------------------------------------------------------------------------------------------------------------------------------------
|<img width="441" height="226" alt="image" src="https://github.com/user-attachments/assets/935fa63c-a783-42fa-bcc9-ae38e8b461b9" /> |
|<img width="444" height="226" alt="image" src="https://github.com/user-attachments/assets/99d933f5-9e8c-4e4d-b71c-55a81d2f027f" /> |
|<img width="441" height="227" alt="image" src="https://github.com/user-attachments/assets/071756d2-3e35-4364-9d57-0cb5a3301665" /> |




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

- No external cloud sync/export.
- Limited error handling and UX for BLE edge cases.

## Video Demo: iOS - Android

https://github.com/user-attachments/assets/e88b0a23-735c-469a-849d-84a6222fd4a3

