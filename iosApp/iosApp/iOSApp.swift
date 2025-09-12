import ComposeApp
import SwiftUI

@main
struct iOSApp: App {

    init() {
        KoinHelperKt.doInit()
    }


    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}