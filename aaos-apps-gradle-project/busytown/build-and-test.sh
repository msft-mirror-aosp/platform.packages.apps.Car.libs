#!/bin/bash
set -eou pipefail

SCRIPTS_DIR=$(realpath "${0%/*}")

echo "Starting $0 at $(date)"
. "$SCRIPTS_DIR/envsetup.sh"

# Set default version codes and names, allow overrides from the calling shell
DEFAULT_VERSION_CODE=33
DEFAULT_VERSION_NAME=33

cd "$GRADLE_PROJ_DIR"

# Run the build
# (Keep each line separate to keep merges clean)
# (oem-token-lib's verify task is currently broken and is excluded)
./gradlew \
    -PversionCode="${VERSION_CODE:-$DEFAULT_VERSION_CODE}" \
    -PversionName="${VERSION_NAME:-$DEFAULT_VERSION_NAME}" \
    -x :oem-token-lib:verifyReleaseResources \
    assemble \
    assembleAndroidTest \
    test

echo "Completing $0 at $(date)"