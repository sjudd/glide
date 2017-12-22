#!/usr/bin/env bash

set -e

target="system-images;android-${ANDROID_TARGET};default;armeabi-v7a"
echo y | sdkmanager --update
echo y | sdkmanager --install $target
which sdkmanager
which avdmanager
sdkmanager --version
sdkmanager --list | grep emulator
avdmanager create avd --force -n test -k $target --device "Nexus 5X" -c 1024M
QEMU_AUDIO_DRV=none $ANDROID_HOME/emulator/emulator -avd test -no-window -memory 1024 -no-snapshot -force-32bit &

exit 0
