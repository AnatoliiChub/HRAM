//
//  HRActivityAttributes.swift
//  iosApp
//
//  Created by Anatolii Chub on 06/01/2026.
//

import ActivityKit
import WidgetKit

// MARK: - Activity Attributes
public struct HRActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        public var heartRate: Int
        public var isConnected: Bool
        public var isContactOn: Bool
        public var trackingState: String
        public var batteryLevel: Int
        public var deviceName: String

        public init(heartRate: Int, isConnected: Bool, isContactOn: Bool, trackingState: String, batteryLevel: Int, deviceName: String) {
            self.heartRate = heartRate
            self.isConnected = isConnected
            self.isContactOn = isContactOn
            self.trackingState = trackingState
            self.batteryLevel = batteryLevel
            self.deviceName = deviceName
        }
    }

    public var activityName: String

    public init(activityName: String) {
        self.activityName = activityName
    }
}
