import Foundation
import ActivityKit

// MARK: - Kotlin/Native C Bridge Functions
// These @_cdecl functions allow Kotlin to call Swift code
import ComposeApp

@_cdecl("startLiveActivity")
public func startLiveActivity(
    activityName: UnsafePointer<CChar>,
    heartRate: Int32,
    isConnected: Bool,
    isContactOn: Bool,
    bleState: UnsafePointer<CChar>,
    isTrackingActive: Bool,
    batteryLevel: Int32,
    deviceName: UnsafePointer<CChar>,
    elapsedTime: Int64
) -> UnsafePointer<CChar>? {
    let name = String(cString: activityName)
    let bleStateStr = String(cString: bleState)
    let device = String(cString: deviceName)

    let activityId = LiveActivityBridgeImpl.startActivity(
        activityName: name,
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
    activityId: UnsafePointer<CChar>,
    heartRate: Int32,
    isConnected: Bool,
    isContactOn: Bool,
    bleState: UnsafePointer<CChar>,
    isTrackingActive: Bool,
    batteryLevel: Int32,
    deviceName: UnsafePointer<CChar>,
    elapsedTime: Int64
) {
    let id = String(cString: activityId)
    let bleStateStr = String(cString: bleState)
    let device = String(cString: deviceName)

    LiveActivityBridgeImpl.updateActivity(
        activityId: id,
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
public func endLiveActivity(activityId: UnsafePointer<CChar>) {
    let id = String(cString: activityId)
    LiveActivityBridgeImpl.endActivity(activityId: id)
}

// MARK: - Implementation Class

@objc(LiveActivityBridgeImpl)
public class LiveActivityBridgeImpl: NSObject {
    private static var activeActivities: [String: Activity<HRActivityAttributes>] = [:]
    private static var currentActivityId: String?

    @objc public static func startActivity(
        activityName: String,
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
        if let existingId = currentActivityId, activeActivities[existingId] != nil {
            print("Live Activity already exists with ID: \(existingId), reusing it")
            return existingId
        }

        // Parse bleState string to BleStateType enum
        let bleStateType = BleStateType.from(state: bleState)
        let elapsedTimeString = DateUtilsKt.formatElapsedTime(elapsedTimeSeconds: elapsedTime)

        let attributes = HRActivityAttributes(activityName: activityName)
        let contentState = HRActivityAttributes.ContentState(
            heartRate: heartRate,
            isConnected: isConnected,
            isContactOn: isContactOn,
            bleState: bleStateType.displayText(deviceName: deviceName),
            isTrackingActive: isTrackingActive,
            batteryLevel: batteryLevel,
            deviceName: deviceName,
            iconName: iconName(bleState: bleStateType, isConnected: isConnected, isContactOn: isContactOn),
            elapsedTimeString: elapsedTimeString
        )

        do {
            let activity = try Activity<HRActivityAttributes>.request(
                attributes: attributes,
                content: .init(state: contentState, staleDate: nil),
                pushType: nil
            )

            let actId = activity.id
            activeActivities[actId] = activity
            currentActivityId = actId

            // Monitor activity state changes to detect dismissal
            Task {
                for await state in activity.activityStateUpdates {
                    print("Live Activity state changed to: \(state)")

                    if state == .dismissed || state == .ended {
                        print("Live Activity was \(state)")
                        activeActivities.removeValue(forKey: actId)
                        if currentActivityId == actId {
                            currentActivityId = nil
                        }
                        break
                    }
                }
            }

            print("Live Activity started with ID: \(actId)")
            return actId
        } catch {
            print("Failed to start Live Activity: \(error.localizedDescription)")
            return nil
        }
    }

    @objc public static func updateActivity(
        activityId: String,
        heartRate: Int,
        isConnected: Bool,
        isContactOn: Bool,
        bleState: String,
        isTrackingActive: Bool,
        batteryLevel: Int,
        deviceName: String,
        elapsedTime: Int64
    ) {
        guard let activity = activeActivities[activityId] else {
            print("No active Live Activity found with ID: \(activityId)")
            return
        }

        // Parse bleState string to BleStateType enum
        let bleStateType = BleStateType.from(state: bleState)
        let elapsedTimeString = DateUtilsKt.formatElapsedTime(elapsedTimeSeconds: elapsedTime)

        let contentState = HRActivityAttributes.ContentState(
            heartRate: heartRate,
            isConnected: isConnected,
            isContactOn: isContactOn,
            bleState: bleStateType.displayText(deviceName: deviceName),
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

    @objc public static func endActivity(activityId: String) {
        guard let activity = activeActivities[activityId] else {
            print("No active Live Activity found with ID: \(activityId)")
            return
        }

        Task {
            await activity.end(
                using: activity.contentState,
                dismissalPolicy: .after(.now + 3)
            )
            activeActivities.removeValue(forKey: activityId)
            if currentActivityId == activityId {
                currentActivityId = nil
            }
            print("Live Activity ended: \(activityId)")
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
    func displayText(deviceName: String = "") -> String {
        switch self {
        case .scanningStarted:
            return "Scanning..."
        case .scanningUpdate:
            return deviceName.isEmpty ? "Device found" : "Found: \(deviceName)"
        case .scanningCompleted:
            return "Scan complete"
        case .scanningError:
            return "Scan error"
        case .connecting:
            return deviceName.isEmpty ? "Connecting..." : "Connecting to \(deviceName)"
        case .connected, .notificationUpdate:
            return "Connected"
        case .disconnected:
            return "Disconnected"
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
