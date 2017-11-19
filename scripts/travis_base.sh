#!/usr/bin/env bash

set -e

# First build the sample apps.
./gradlew :samples:flickr:build \
  :samples:giphy:build \
  :samples:contacturi:build \
  :samples:gallery:build \
  :samples:imgur:build \
  :samples:svg:build \
  --parallel &
pid=$!

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  # Firebase tests can't be run for pull requests, so just build our unit tests and finish.
  wait $pid
  echo "Unable to run Firebase tests for pull requests, skipping"
  ./scripts/travis_unit.sh
  exit 0
fi

# For non-pull requests, continue by installing firebase.
./scripts/install_firebase.sh

# Then wait for our sample apps to finish building.
wait $pid

# And queue the sample app monkey runner tests on firebase.
declare -a samples=("flickr"
                "giphy"
                "contacturi"
                "gallery"
                "imgur"
                "svg")
firebase_pids=()

for sample in "${samples[@]}"
do
  sample_dir="samples/${sample}/build/outputs/apk/debug"
  sample_apk="${sample_dir}/${sample}-debug.apk"
  ./google-cloud-sdk/bin/gcloud firebase test android run \
    --type robo \
    --app $sample_apk \
    --device model=Nexus6P,version=26,locale=en,orientation=portrait  \
    --project android-glide \
    --no-auto-google-login \
    --timeout 5m \
    &
  firebase_pids+=("$!")
done

# Next start the instrumentation apks building in the background
./gradlew :instrumentation:assembleDebug :instrumentation:assembleDebugAndroidTest --parallel & 
build_instrumentation_pid=$!

# Then wait for the instrumentation apks to be built.
wait $build_instrumentation_pid

# And queue the instrumentation tests on firebase.
apk_dir=instrumentation/build/outputs/apk
./google-cloud-sdk/bin/gcloud firebase test android run \
  --type instrumentation \
  --app $apk_dir/debug/instrumentation-debug.apk \
  --test $apk_dir/androidTest/debug/instrumentation-debug-androidTest.apk \
  --device model=Nexus6P,version=26,locale=en,orientation=portrait \
  --device model=Nexus6P,version=25,locale=en,orientation=portrait \
  --device model=Nexus6P,version=24,locale=en,orientation=portrait \
  --device model=Nexus6P,version=23,locale=en,orientation=portrait \
  --device model=Nexus6,version=22,locale=en,orientation=portrait \
  --device model=Nexus5,version=21,locale=en,orientation=portrait \
  --device model=Nexus5,version=19,locale=en,orientation=portrait \
  --project android-glide \
  --no-auto-google-login \
  &
firebase_pids+=("$!")

# While waiting for the Firebase tests to complete, run the unit tests.
./scripts/travis_unit.sh

# Finally, wait for all the firebase tests to complete.
for current in "${firebase_pids[@]}"
do
  wait $current
done


