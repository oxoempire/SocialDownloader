#!/bin/bash
set -e

WORKSPACE=$(pwd)

# 1. Download OpenJDK 17
if [ ! -d "jdk-17" ]; then
    echo "Downloading JDK 17..."
    wget -q "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz" -O jdk.tar.gz
    mkdir -p jdk-17
    tar -xzf jdk.tar.gz -C jdk-17 --strip-components=1
fi
export JAVA_HOME="$WORKSPACE/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"

# 2. Download Android SDK
export ANDROID_HOME="$WORKSPACE/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"

if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "Downloading Android SDK..."
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline.zip
    unzip -q cmdline.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
fi

# 3. Install required Android packages
echo "Installing Android platform tools..."
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-34" "build-tools;34.0.0" > /dev/null

# 4. Download Gradle 8.2
if [ ! -d "gradle-8.2" ]; then
    echo "Downloading Gradle..."
    wget -q https://services.gradle.org/distributions/gradle-8.2-bin.zip
    unzip -q gradle-8.2-bin.zip
fi

# 5. Build APK
echo "Building APK..."
./gradle-8.2/bin/gradle assembleDebug --no-daemon

# 6. Copy output
cp app/build/outputs/apk/debug/app-debug.apk ./SocialDownloader.apk
echo "Success! APK compiled at ./SocialDownloader.apk"
