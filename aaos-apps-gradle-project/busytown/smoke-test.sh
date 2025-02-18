#!/bin/bash
set -eou pipefail

SCRIPTS_DIR=$(realpath "${0%/*}")
. "$SCRIPTS_DIR/envsetup.sh"

# Functions defined in envsetup.sh
setup_build_environment

# Just a quick check to make sure that the build runs without errors
# Picking `car-ui-lib` because it doesn't rely on much
./gradlew \
    :buildLogic:check \
    :car-ui-lib:assembleDebug \
    :car-ui-lib:assembleDebugAndroidTest \
    :car-ui-lib:testDebug

# Functions defined in envsetup.sh
wrap_up_build $?
