import ComposeApp
import SwiftUI

@main
struct iOSApp: App {

    init() {
        InitHelperKt.doInit()
    }


    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}