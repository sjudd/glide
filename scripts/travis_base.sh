#!/usr/bin/env bash

set -e

# First build the sample apps and library
./gradlew :samples:flickr:build \
  :samples:giphy:build \
  :samples:contacturi:build \
  :samples:gallery:build \
  :samples:imgur:build \
  :samples:svg:build \
  --parallel &
pid=$!

# Then install firebase.
if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  wait $pid
  echo "Unable to run Firebase tests for pull requests, skipping"
  ./scripts/travis_unit.sh
  exit 0
else
  ./scripts/install_firebase.sh
  wait $pid
fi

./scripts/travis_unit.sh &
unit_pid=$!

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

wait $unit_pid

for current in "${firebase_pids[@]}"
do
  wait $current
done


