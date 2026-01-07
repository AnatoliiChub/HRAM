import Foundation
import ActivityKit

// MARK: - Kotlin/Native C Bridge Functions
// These @_cdecl functions allow Kotlin to call Swift code

@_cdecl("startLiveActivity")
public func startLiveActivity(
    activityName: UnsafePointer<CChar>,
    heartRate: Int32,
    isConnected: Bool,
    isContactOn: Bool,
    trackingState: UnsafePointer<CChar>,
    batteryLevel: Int32,
    deviceName: UnsafePointer<CChar>
) -> UnsafePointer<CChar>? {
    let name = String(cString: activityName)
    let state = String(cString: trackingState)
    let device = String(cString: deviceName)

    let activityId = LiveActivityBridgeImpl.startActivity(
        activityName: name,
        heartRate: Int(heartRate),
        isConnected: isConnected,
        isContactOn: isContactOn,
        trackingState: state,
        batteryLevel: Int(batteryLevel),
        deviceName: device
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
    trackingState: UnsafePointer<CChar>,
    batteryLevel: Int32,
    deviceName: UnsafePointer<CChar>
) {
    let id = String(cString: activityId)
    let state = String(cString: trackingState)
    let device = String(cString: deviceName)

    LiveActivityBridgeImpl.updateActivity(
        activityId: id,
        heartRate: Int(heartRate),
        isConnected: isConnected,
        isContactOn: isContactOn,
        trackingState: state,
        batteryLevel: Int(batteryLevel),
        deviceName: device
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
        trackingState: String,
        batteryLevel: Int,
        deviceName: String
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


        let attributes = HRActivityAttributes(activityName: activityName)
        let contentState = HRActivityAttributes.ContentState(
            heartRate: heartRate,
            isConnected: isConnected,
            isContactOn: isContactOn,
            trackingState: trackingState,
            batteryLevel: batteryLevel,
            deviceName: deviceName
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
        trackingState: String,
        batteryLevel: Int,
        deviceName: String
    ) {
        guard let activity = activeActivities[activityId] else {
            print("No active Live Activity found with ID: \(activityId)")
            return
        }

        let contentState = HRActivityAttributes.ContentState(
            heartRate: heartRate,
            isConnected: isConnected,
            isContactOn: isContactOn,
            trackingState: trackingState,
            batteryLevel: batteryLevel,
            deviceName: deviceName
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

