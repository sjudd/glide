#!/usr/bin/env bash

set -e

target="system-images;android-${ANDROID_TARGET};default;armeabi-v7a"
echo y | sdkmanager --update
echo y | sdkmanager --install $target
which sdkmanager
which avdmanager
sdkmanager --version
sdkmanager --list | grep emulator
echo no | avdmanager create avd --force -n test -k $target -c 2048M --abi default/armeabi-v7a
QEMU_AUDIO_DRV=none $ANDROID_HOME/emulator/emulator -avd test -no-window -memory 2048 &

exit 0
