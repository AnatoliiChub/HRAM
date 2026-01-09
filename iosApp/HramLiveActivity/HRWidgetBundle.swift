//
//  HRWidgetBundle.swift
//  HRWidget
//
//  Created by Anatolii Chub on 04/01/2026.
//

import SwiftUI
import WidgetKit


// MARK: - Widget Bundle Entry Point
// Add this file to your Widget Extension target
@main
struct HRWidgetBundle: WidgetBundle {
    var body: some Widget {
            HRLiveActivity()
    }
}

// MARK: - Preview Provider (Optional)
#if DEBUG
struct HRLiveActivityPreviews: PreviewProvider {
    static let attributes = HRActivityAttributes(activityName: "Preview Activity")
    static let contentState = HRActivityAttributes.ContentState(
        heartRate: 72,
        isConnected: true,
        isContactOn: true,
        bleState: "Connected",
        trackingState: "00:05:23",
        batteryLevel: 85,
        deviceName: "HRM Belt",
        iconName: "heart.fill"
    )

    static var previews: some View {
        // Lock Screen Preview
        attributes
            .previewContext(contentState, viewKind: .content)
            .previewDisplayName("Lock Screen")

        // Dynamic Island Previews
        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.compact))
            .previewDisplayName("Dynamic Island - Compact")

        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.expanded))
            .previewDisplayName("Dynamic Island - Expanded")

        attributes
            .previewContext(contentState, viewKind: .dynamicIsland(.minimal))
            .previewDisplayName("Dynamic Island - Minimal")
    }
}
#endif
