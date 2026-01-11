import Foundation
import ActivityKit

// MARK: - Kotlin/Native C Bridge Functions
// These @_cdecl functions allow Kotlin to call Swift code
import ComposeApp

@_cdecl("startLiveActivity")
public func startLiveActivity(
    heartRate: Int32,
    isConnected: Bool,
    isContactOn: Bool,
    bleState: UnsafePointer<CChar>,
    isTrackingActive: Bool,
    batteryLevel: Int32,
    deviceName: UnsafePointer<CChar>,
    elapsedTime: Int64
) -> UnsafePointer<CChar>? {
    let bleStateStr = String(cString: bleState)
    let device = String(cString: deviceName)

    let activityId = LiveActivityBridgeImpl.startActivity(
        heartRate: Int(heartRate),
        isConnected: isConnected,
        isContactOn: isContactOn,
        bleState: bleStateStr,
        isTrackingActive: isTrackingActive,
        batteryLevel: Int(batteryLevel),
        deviceName: device,
        elapsedTime: elapsedTime
    )

    guard let id = activityId else {
        return nil
    }
    let cString = strdup(id)
    return UnsafePointer(cString)
}

@_cdecl("updateLiveActivity")
public func updateLiveActivity(
    heartRate: Int32,
    isConnected: Bool,
    isContactOn: Bool,
    bleState: UnsafePointer<CChar>,
    isTrackingActive: Bool,
    batteryLevel: Int32,
    deviceName: UnsafePointer<CChar>,
    elapsedTime: Int64
) {
    let bleStateStr = String(cString: bleState)
    let device = String(cString: deviceName)

    LiveActivityBridgeImpl.updateActivity(
        heartRate: Int(heartRate),
        isConnected: isConnected,
        isContactOn: isContactOn,
        bleState: bleStateStr,
        isTrackingActive: isTrackingActive,
        batteryLevel: Int(batteryLevel),
        deviceName: device,
        elapsedTime: elapsedTime
    )
}

@_cdecl("endLiveActivity")
public func endLiveActivity() {
    LiveActivityBridgeImpl.endActivity()
}

// MARK: - Implementation Class

@objc(LiveActivityBridgeImpl)
public class LiveActivityBridgeImpl: NSObject {
    private static var activity: Activity<HRActivityAttributes>?

    @objc public static func startActivity(
        heartRate: Int,
        isConnected: Bool,
        isContactOn: Bool,
        bleState: String,
        isTrackingActive: Bool,
        batteryLevel: Int,
        deviceName: String,
        elapsedTime: Int64
    ) -> String? {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else {
            print("Live Activities are not enabled")
            return nil
        }

        // Check if there's already an active activity for this app
        // If yes, return its ID instead of creating a new one
        if activity != nil {
            print("Live Activity already exists with ID: \(activity?.id ?? ""), reusing it")
            return activity?.id
        }

        // Parse bleState string to BleStateType enum
        let bleStateType = BleStateType.from(state: bleState)
        let elapsedTimeString = DateUtilsKt.formatElapsedTime(elapsedTimeSeconds: elapsedTime)

        let attributes = HRActivityAttributes()
        let contentState = HRActivityAttributes.ContentState(
            heartRate: heartRate,
            isConnected: isConnected,
            isContactOn: isContactOn,
            bleState: bleStateType.displayText(deviceName: deviceName, connectionLost: !isConnected, isContactOn: isContactOn),
            isTrackingActive: isTrackingActive,
            batteryLevel: batteryLevel,
            deviceName: deviceName,
            iconName: iconName(bleState: bleStateType, isConnected: isConnected, isContactOn: isContactOn),
            elapsedTimeString: elapsedTimeString
        )

        do {
            activity = try Activity<HRActivityAttributes>.request(
                attributes: attributes,
                content: .init(state: contentState, staleDate: nil),
                pushType: nil
            )

            guard let existingActivity = activity else {
                print("Failed to create Live Activity")
                return nil
            }

            // Monitor activity state changes to detect dismissal
            Task {
                for await state in existingActivity.activityStateUpdates {
                    print("Live Activity state changed to: \(state)")

                    if state == .dismissed || state == .ended {
                        self.activity = nil
                    }
                }
            }

            return existingActivity.id
        } catch {
            print("Failed to start Live Activity: \(error.localizedDescription)")
            return nil
        }
    }

    @objc public static func updateActivity(
        heartRate: Int,
        isConnected: Bool,
        isContactOn: Bool,
        bleState: String,
        isTrackingActive: Bool,
        batteryLevel: Int,
        deviceName: String,
        elapsedTime: Int64
    ) {
        guard let activity = activity else {
            print("No active Live Activity found to update")
            return
        }

        // Parse bleState string to BleStateType enum
        let bleStateType = BleStateType.from(state: bleState)
        let elapsedTimeString = DateUtilsKt.formatElapsedTime(elapsedTimeSeconds: elapsedTime)

        let contentState = HRActivityAttributes.ContentState(
            heartRate: heartRate,
            isConnected: isConnected,
            isContactOn: isContactOn,
            bleState: bleStateType.displayText(deviceName: deviceName, connectionLost: !isConnected, isContactOn: isContactOn),
            isTrackingActive: isTrackingActive,
            batteryLevel: batteryLevel,
            deviceName: deviceName,
            iconName: iconName(bleState: bleStateType, isConnected: isConnected, isContactOn: isContactOn),
            elapsedTimeString: elapsedTimeString
        )

        Task {
            await activity.update(using: contentState)
        }
    }

    @objc public static func endActivity() {
        guard let activity = activity else {
            print("No active Live Activity found")
            return
        }

        Task {
            await activity.end(
                using: activity.contentState,
                dismissalPolicy: .immediate
            )
            print("Live Activity ended: \(activity.id)")
            self.activity = nil
        }
    }
}

func iconName(bleState: BleStateType, isConnected: Bool, isContactOn: Bool) -> String {
    if [.scanningStarted, .connecting, .scanningCompleted, .scanningError, .scanningUpdate].contains(bleState) {
        "dot.radiowaves.right"
    } else if !isConnected {
        "heart.slash.fill"
    } else if !isContactOn {
        "heart.fill"
    } else {
        "heart.fill"
    }
}

public extension BleStateType {
    func displayText(deviceName: String = "", connectionLost: Bool, isContactOn: Bool) -> String {
        switch self {
        case .scanningStarted:
            return String(localized: "ble.scanning_started", defaultValue: "Scanning...")
        case .scanningUpdate:
            if deviceName.isEmpty {
                return String(localized: "ble.device_found", defaultValue: "Device found")
            } else {
                let format = String(localized: "ble.found_device", defaultValue: "Found: %@")
                return String(format: format, deviceName)
            }
        case .scanningCompleted:
            return String(localized: "ble.scan_complete", defaultValue: "Scan complete")
        case .scanningError:
            return String(localized: "ble.scan_error", defaultValue: "Scan error")
        case .connecting:
            if deviceName.isEmpty {
                return String(localized: "ble.connecting", defaultValue: "Connecting...")
            } else {
                let format = String(localized: "ble.connecting_to_device", defaultValue: "Connecting to %@")
                return String(format: format, deviceName)
            }
        case .connected, .notificationUpdate:
            return if(connectionLost) {
                String(localized: "ble.connection.lost", defaultValue: "Connection Lost")
            } else if(!isContactOn) {
                String(localized: "ble.contact.off", defaultValue: "Contact off")
            }else {
                String(localized: "ble.connected", defaultValue: "Connected")
            }
        case .disconnected:
            return String(localized: "ble.disconnected", defaultValue: "Disconnected")
        default:
            return ""
        }
    }
}

extension BleStateType {
    static func from(state: String) -> BleStateType {
        return BleStateType.companion.from(bleState: state)
    }
}
