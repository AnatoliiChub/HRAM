#!/bin/bash

set -e  # Exit on error

# Default values
SDK="${1:-iphonesimulator}"
CONFIGURATION="${2:-Debug}"
SIMULATOR_NAME="${3:-iPhone 16e}"

echo "=========================================="
echo "Building iOS Xcode Project"
echo "=========================================="
echo "SDK: $SDK"
echo "Configuration: $CONFIGURATION"
echo "Simulator: $SIMULATOR_NAME"
echo "=========================================="

# Determine destination based on SDK
if [[ "$SDK" == "iphonesimulator" ]]; then
    DESTINATION="platform=iOS Simulator,name=${SIMULATOR_NAME}"
elif [[ "$SDK" == "iphoneos" ]]; then
    DESTINATION="generic/platform=iOS"
else
    echo "Error: Unknown SDK '$SDK'. Use 'iphonesimulator' or 'iphoneos'"
    exit 1
fi

# Build Xcode project
echo ""
echo "Building Xcode project..."
xcodebuild \
    -project iosApp/iosApp.xcodeproj \
    -scheme iosApp \
    -configuration "$CONFIGURATION" \
    -sdk "$SDK" \
    -destination "$DESTINATION" \
    -derivedDataPath "./build/ios-${SDK}" \
    ONLY_ACTIVE_ARCH=YES \
    CODE_SIGNING_ALLOWED=NO \
    CODE_SIGNING_REQUIRED=NO \
    CODE_SIGN_IDENTITY="" \
    -verbose \
    clean build

echo ""
echo "=========================================="
echo "✅ Xcode build completed successfully!"
echo "=========================================="
echo "Output: ./build/ios-${SDK}/Build/Products/${CONFIGURATION}-${SDK}/"
echo "=========================================="

