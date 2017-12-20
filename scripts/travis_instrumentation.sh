#!/usr/bin/env bash

set -e

echo "Starting emulator for $COMPONENT tests"
./scripts/travis_create_emulator.sh &

echo "Waiting for emulator..."
android-wait-for-emulator

./gradlew :instrumentation:assembleDebug :instrumentation:assembleDebugAndroidTest --parallel

for i in {1..3}; do ./gradlew :instrumentation:connectedDebugAndroidTest && break; done

