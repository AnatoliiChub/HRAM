// Swift

import SwiftUI
import ActivityKit

import WidgetKit
import SwiftUI


public struct HRLiveActivityView: View {
    let context: ActivityViewContext<HRActivityAttributes>

    public var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    StatusIconView(iconName: context.state.iconName, isContactOn: context.state.isContactOn)
                    TrackingStatusText(trackingState: context.state.bleState)
                }
                Spacer()
                VStack {
                    HeartRateView(
                        heartRate: context.state.heartRate,
                        isConnected: context.state.isConnected,
                        isContactOn: context.state.isContactOn
                    )
                    ElapsedTimeView(elapsedTime: context.state.elapsedTimeString, isActive: context.state.isTrackingActive)
                }
            }

            if context.state.isConnected {
                HStack(alignment: .bottom) {
                    Text(context.state.deviceName)
                        .font(.caption2.bold())
                        .foregroundColor(.secondary)

                    Spacer()

                    BatteryView(level: context.state.batteryLevel)
                }
            }
        }
        .padding(16)
        .activityBackgroundTint(Color.black.opacity(0.3))
        .activitySystemActionForegroundColor(Color.red)
    }

}

struct TrackingStatusText: View {
    let trackingState: String

    var body: some View {

        Text(trackingState)
            .font(.caption.bold())
            .foregroundColor(.green)
    }
}

public struct HRLiveActivity: Widget {
    public init() {
    }

    public var body: some WidgetConfiguration {
        ActivityConfiguration(for: HRActivityAttributes.self) { context in
            // Lock screen/banner UI
            HRLiveActivityView(context: context)
        } dynamicIsland: { context in
            // Dynamic Island UI
            DynamicIsland {
                // Expanded view
                DynamicIslandExpandedRegion(.leading) {
                    VStack {
                        Spacer().frame(height: 4)
                        StatusIconView(iconName: context.state.iconName, isContactOn: context.state.isContactOn)

                        Spacer().frame(height: 8)
                        Text(context.state.bleState)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Spacer().frame(height: 2)
                        ElapsedTimeView(elapsedTime: context.state.elapsedTimeString, isActive: context.state.isTrackingActive)
                        Spacer().frame(height: 6)
                        Text(context.state.deviceName)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }

                }

                DynamicIslandExpandedRegion(.trailing) {
                    VStack(spacing: 8) {
                        HeartRateView(
                            heartRate: context.state.heartRate,
                            isConnected: context.state.isConnected,
                            isContactOn: context.state.isContactOn
                        )
                        ElapsedTimeView(elapsedTime: context.state.elapsedTimeString, isActive: context.state.isTrackingActive)
                        BatteryView(level: context.state.batteryLevel)
                    }
                }

                DynamicIslandExpandedRegion(.bottom) {

                }
            } compactLeading: {
                // Compact leading (left side of Dynamic Island)
                StatusIconView(iconName: context.state.iconName, isContactOn: context.state.isContactOn)

            } compactTrailing: {
                // Compact trailing (right side of Dynamic Island)
                if context.state.isConnected && context.state.isContactOn {
                    Text("\(context.state.heartRate)")
                        .font(.caption.bold())
                        .foregroundColor(.green)
                } else {
                    Text("--")
                        .font(.caption)
                        .foregroundColor(.red)
                }
            } minimal: {
                // Minimal view (when multiple Live Activities are active)
                StatusIconView(iconName: context.state.iconName, isContactOn: context.state.isContactOn)

            }
            .keylineTint(.red)
        }
    }
}

// swift

import SwiftUI

struct BatteryView: View {
    let level: Int

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: batteryIcon(level: level))
                .foregroundColor(batteryColor(level: level))

            Text("\(level)%")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }

    private func batteryIcon(level: Int) -> String {
        if level > 75 {
            return "battery.100"
        }
        if level > 50 {
            return "battery.75"
        }
        if level > 25 {
            return "battery.50"
        }
        if level > 10 {
            return "battery.25"
        }
        return "battery.0"
    }

    private func batteryColor(level: Int) -> Color {
        level > 20 ? .green : .red
    }
}

struct StatusIconView: View {
    let iconName: String
    let isContactOn: Bool

    var body: some View {
        Group {
            let color: Color = switch iconName {
            case "dot.radiowaves.right":
                .gray
            case "heart.slash.fill":
                .gray
            case "heart.fill":
                isContactOn ? .red : .yellow
            default:
                .gray
            }
            Image(systemName: iconName)
                .imageScale(.large)
                .foregroundColor(color)
        }
    }
}

struct HeartRateView: View {
    let heartRate: Int
    let isConnected: Bool
    let isContactOn: Bool

    var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: 4) {
            let heartRateLabel = if isConnected && isContactOn {
                "\(heartRate)"
            } else {
                String(localized: "--", defaultValue: "--")
            }
            let color = isConnected && isContactOn ? Color.green : Color.red
            let unitLabel = isConnected ? String(localized: "bpm", defaultValue: "bpm") : ""
            Text(heartRateLabel)
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .foregroundColor(color)
                .opacity(isConnected ? 1 : 0)
            Text(unitLabel)
                .font(.caption.bold())
                .foregroundColor(color)
                .opacity(isConnected ? 1 : 0)
        }
    }
}

struct ElapsedTimeView: View {
    let elapsedTime: String
    let isActive: Bool

    var body: some View {
        Text(elapsedTime)
            .font(.caption.bold())
            .foregroundColor(isActive ? .green : .red)
    }
}
