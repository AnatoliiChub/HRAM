#!/bin/bash

set -e  # Exit on error

# Default values
SDK="${1:-iphonesimulator}"
CONFIGURATION="${2:-Debug}"

echo "=========================================="
echo "Building Compose Framework"
echo "=========================================="
echo "SDK: $SDK"
echo "Configuration: $CONFIGURATION"
echo "=========================================="

# Determine architecture based on SDK
if [[ "$SDK" == "iphonesimulator" ]]; then
    ARCH="iosSimulatorArm64"
    GRADLE_TASK="IosSimulatorArm64"
    FRAMEWORK_PATH="composeApp/build/bin/iosSimulatorArm64/debugFramework"
elif [[ "$SDK" == "iphoneos" ]]; then
    ARCH="iosArm64"
    GRADLE_TASK="IosArm64"
    FRAMEWORK_PATH="composeApp/build/bin/iosArm64/debugFramework"
else
    echo "Error: Unknown SDK '$SDK'. Use 'iphonesimulator' or 'iphoneos'"
    exit 1
fi

# Step 1: Build Compose framework
echo ""
echo "Step 1: Building Compose framework for ${ARCH}..."
./gradlew :composeApp:linkDebugFramework${GRADLE_TASK} \
    --no-daemon \
    --stacktrace

# Step 2: Prepare Xcode frameworks directory
echo ""
echo "Step 2: Preparing Xcode frameworks directory..."

# Determine the SDK name for the framework path
SDK_NAME=$(xcodebuild -showsdks | grep -o "${SDK}[0-9.]*" | head -1)
echo "Detected SDK name: $SDK_NAME"

# Create the xcode-frameworks directory and copy the framework
XCODE_FRAMEWORKS_DIR="composeApp/build/xcode-frameworks/${CONFIGURATION}/${SDK_NAME}"
mkdir -p "$XCODE_FRAMEWORKS_DIR"
cp -R "${FRAMEWORK_PATH}/ComposeApp.framework" "$XCODE_FRAMEWORKS_DIR/"
echo "Framework copied to: $XCODE_FRAMEWORKS_DIR"

echo ""
echo "=========================================="
echo "✅ Framework build completed successfully!"
echo "=========================================="
echo "Framework location: $XCODE_FRAMEWORKS_DIR/ComposeApp.framework"
echo "=========================================="

