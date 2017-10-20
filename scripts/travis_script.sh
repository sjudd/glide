#!/usr/bin/env bash

set -e

if [ "$COMPONENT" == "unit" ]; then
  ./gradlew build --parallel
elif [ "$COMPONENT" == "instrumentation" ]; then
  ./scripts/travis_instrumentation.sh
else
  echo "Unrecognized component: $COMPONENT"
  exit 1
fi
