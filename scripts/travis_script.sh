#!/usr/bin/env bash

set -e

if [ "$COMPONENT" == "base" ]; then
  ./scripts/travis_base.sh
elif [ "$COMPONENT" == "instrumentation" ]; then
  ./scripts/travis_instrumentation.sh
elif [ "$COMPONENT" == "firebase" ]; then
  ./scripts/travis_firebase.sh
else
  echo "Unrecognized component: $COMPONENT"
  exit 1
fi
