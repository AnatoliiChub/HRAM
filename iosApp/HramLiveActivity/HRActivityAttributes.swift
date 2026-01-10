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
        public var bleState: String
        public var isTrackingActive: Bool
        public var batteryLevel: Int
        public var deviceName: String
        public var iconName: String
        public var elapsedTimeString: String

        public init(heartRate: Int, isConnected: Bool, isContactOn: Bool, bleState: String,
                    isTrackingActive: Bool, batteryLevel: Int, deviceName: String, iconName: String, elapsedTimeString: String) {
            self.heartRate = heartRate
            self.isConnected = isConnected
            self.isContactOn = isContactOn
            self.bleState = bleState
            self.isTrackingActive = isTrackingActive
            self.batteryLevel = batteryLevel
            self.deviceName = deviceName
            self.iconName = iconName
            self.elapsedTimeString = elapsedTimeString
        }
    }
}
