#!/usr/bin/env bash

set -e

./gradlew :instrumentation:assembleDebug :instrumentation:assembleDebugAndroidTest --parallel

echo "Starting emulator for $COMPONENT tests"
./scripts/travis_create_emulator.sh

echo "Waiting for emulator..."
./scripts/android-wait-for-emulator.sh

for i in {1..3}; do ./gradlew :instrumentation:connectedDebugAndroidTest && break; done

