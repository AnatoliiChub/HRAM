//
//  HRLiveActivity.swift
//  HRAM
//
//  Live Activity and Dynamic Island implementation for heart rate monitoring
//

import SwiftUI
import ActivityKit
import WidgetKit

// MARK: - Constants

private enum LayoutConstants {
    static let mainPadding: CGFloat = 16
    static let mainSpacing: CGFloat = 8
    static let statusSpacing: CGFloat = 2
    static let iconSpacing: CGFloat = 4
    static let batterySpacing: CGFloat = 4
    static let dynamicIslandTopPadding: CGFloat = 4
    static let dynamicIslandMidPadding: CGFloat = 8
    static let dynamicIslandSmallPadding: CGFloat = 2
    static let dynamicIslandBottomPadding: CGFloat = 6
    static let dynamicIslandSpacing: CGFloat = 8
    static let backgroundOpacity: Double = 0.3
}

// MARK: - Lock Screen View

public struct HRLiveActivityView: View {
    let context: ActivityViewContext<HRActivityAttributes>

    public var body: some View {
        VStack(alignment: .leading, spacing: LayoutConstants.mainSpacing) {
            mainContent

            if context.state.isConnected {
                deviceInfo
            }
        }
        .padding(LayoutConstants.mainPadding)
        .activityBackgroundTint(Color.black.opacity(LayoutConstants.backgroundOpacity))
        .activitySystemActionForegroundColor(.red)
    }

    // MARK: - View Components

    private var mainContent: some View {
        HStack {
            statusSection
            Spacer()
            metricsSection
        }
    }

    private var statusSection: some View {
        VStack(alignment: .leading, spacing: LayoutConstants.statusSpacing) {
            StatusIconView(
                iconName: context.state.iconName,
                isContactOn: context.state.isContactOn
            )
            TrackingStatusText(trackingState: context.state.bleState)
        }
    }

    private var metricsSection: some View {
        VStack {
            HeartRateView(
                heartRate: context.state.heartRate,
                isConnected: context.state.isConnected,
                isContactOn: context.state.isContactOn
            )
            ElapsedTimeView(
                elapsedTime: context.state.elapsedTimeString,
                isActive: context.state.isTrackingActive
            )
        }
    }

    private var deviceInfo: some View {
        HStack(alignment: .bottom) {
            Text(context.state.deviceName)
                .font(.caption2.bold())
                .foregroundColor(.secondary)

            Spacer()

            BatteryView(level: context.state.batteryLevel)
        }
    }
}

// MARK: - Tracking Status Text

struct TrackingStatusText: View {
    let trackingState: String

    var body: some View {

        Text(trackingState)
            .font(.caption.bold())
            .foregroundColor(.green)
    }
}

// MARK: - Live Activity Widget

public struct HRLiveActivity: Widget {
    public init() {}

    public var body: some WidgetConfiguration {
        ActivityConfiguration(for: HRActivityAttributes.self) { context in
            HRLiveActivityView(context: context)
        } dynamicIsland: { context in
            DynamicIsland {
                expandedLeading(context: context)
                expandedTrailing(context: context)
            } compactLeading: {
                compactLeading(context: context)
            } compactTrailing: {
                compactTrailing(context: context)
            } minimal: {
                minimal(context: context)
            }
            .keylineTint(.red)
        }
    }

    // MARK: - Dynamic Island Expanded Regions

    @DynamicIslandExpandedContentBuilder
    private func expandedLeading(context: ActivityViewContext<HRActivityAttributes>) -> DynamicIslandExpandedContent<some View> {
        DynamicIslandExpandedRegion(.leading) {
            VStack(spacing: LayoutConstants.statusSpacing) {
                StatusIconView(
                    iconName: context.state.iconName,
                    isContactOn: context.state.isContactOn
                )
                .padding(.top, LayoutConstants.dynamicIslandTopPadding)

                Text(context.state.bleState)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .padding(.top, LayoutConstants.dynamicIslandMidPadding)

                ElapsedTimeView(
                    elapsedTime: context.state.elapsedTimeString,
                    isActive: context.state.isTrackingActive
                )
                .padding(.top, LayoutConstants.dynamicIslandSmallPadding)

                Text(context.state.deviceName)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .padding(.top, LayoutConstants.dynamicIslandBottomPadding)
            }
        }
    }

    @DynamicIslandExpandedContentBuilder
    private func expandedTrailing(context: ActivityViewContext<HRActivityAttributes>) -> DynamicIslandExpandedContent<some View> {
        DynamicIslandExpandedRegion(.trailing) {
            VStack(spacing: LayoutConstants.dynamicIslandSpacing) {
                HeartRateView(
                    heartRate: context.state.heartRate,
                    isConnected: context.state.isConnected,
                    isContactOn: context.state.isContactOn
                )
                ElapsedTimeView(
                    elapsedTime: context.state.elapsedTimeString,
                    isActive: context.state.isTrackingActive
                )
                BatteryView(level: context.state.batteryLevel)
            }
        }
    }

    // MARK: - Dynamic Island Compact Views

    @ViewBuilder
    private func compactLeading(context: ActivityViewContext<HRActivityAttributes>) -> some View {
        StatusIconView(
            iconName: context.state.iconName,
            isContactOn: context.state.isContactOn
        )
        .accessibilityLabel("Heart rate status")
    }

    @ViewBuilder
    private func compactTrailing(context: ActivityViewContext<HRActivityAttributes>) -> some View {
        if context.state.isConnected && context.state.isContactOn {
            Text("\(context.state.heartRate)")
                .font(.caption.bold())
                .foregroundColor(.green)
                .accessibilityLabel("Heart rate \(context.state.heartRate) beats per minute")
        } else {
            Text("--")
                .font(.caption)
                .foregroundColor(.red)
                .accessibilityLabel("No heart rate data")
        }
    }

    // MARK: - Dynamic Island Minimal View

    @ViewBuilder
    private func minimal(context: ActivityViewContext<HRActivityAttributes>) -> some View {
        StatusIconView(
            iconName: context.state.iconName,
            isContactOn: context.state.isContactOn
        )
        .accessibilityLabel("Heart rate monitoring")
    }
}

// MARK: - Battery View

struct BatteryView: View {
    let level: Int

    private enum BatteryThreshold {
        static let full = 75
        static let high = 50
        static let medium = 25
        static let low = 10
        static let critical = 20
    }

    var body: some View {
        HStack(spacing: LayoutConstants.batterySpacing) {
            Image(systemName: batteryIcon)
                .foregroundColor(batteryColor)

            Text("\(level)%")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Battery \(level) percent")
    }

    // MARK: - Computed Properties

    private var batteryIcon: String {
        switch level {
        case BatteryThreshold.full...:
            return "battery.100"
        case BatteryThreshold.high..<BatteryThreshold.full:
            return "battery.75"
        case BatteryThreshold.medium..<BatteryThreshold.high:
            return "battery.50"
        case BatteryThreshold.low..<BatteryThreshold.medium:
            return "battery.25"
        default:
            return "battery.0"
        }
    }

    private var batteryColor: Color {
        level > BatteryThreshold.critical ? .green : .red
    }
}

// MARK: - Status Icon View

struct StatusIconView: View {
    let iconName: String
    let isContactOn: Bool

    var body: some View {
        Image(systemName: iconName)
            .imageScale(.large)
            .foregroundColor(iconColor)
    }

    // MARK: - Computed Properties

    private var iconColor: Color {
        switch iconName {
        case "dot.radiowaves.right":
            return .gray
        case "heart.slash.fill":
            return .gray
        case "heart.fill":
            return isContactOn ? .red : .yellow
        default:
            return .gray
        }
    }
}

// MARK: - Heart Rate View

struct HeartRateView: View {
    let heartRate: Int
    let isConnected: Bool
    let isContactOn: Bool

    var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: LayoutConstants.iconSpacing) {
            Text(heartRateLabel)
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .foregroundColor(heartRateColor)
                .opacity(isConnected ? 1 : 0)

            Text(unitLabel)
                .font(.caption.bold())
                .foregroundColor(heartRateColor)
                .opacity(isConnected ? 1 : 0)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(accessibilityText)
    }

    // MARK: - Computed Properties

    private var heartRateLabel: String {
        isConnected && isContactOn
            ? "\(heartRate)"
            : String(localized: "--", defaultValue: "--")
    }

    private var heartRateColor: Color {
        isConnected && isContactOn ? .green : .red
    }

    private var unitLabel: String {
        isConnected ? String(localized: "bpm", defaultValue: "bpm") : ""
    }

    private var accessibilityText: String {
        if isConnected && isContactOn {
            return "Heart rate \(heartRate) beats per minute"
        } else {
            return "No heart rate data available"
        }
    }
}

// MARK: - Elapsed Time View

struct ElapsedTimeView: View {
    let elapsedTime: String
    let isActive: Bool

    var body: some View {
        Text(elapsedTime)
            .font(.caption.bold())
            .foregroundColor(isActive ? .green : .red)
            .accessibilityLabel("Elapsed time \(elapsedTime)")
    }
}
