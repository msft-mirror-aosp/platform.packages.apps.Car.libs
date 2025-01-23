#!/bin/bash
set -eou pipefail

SCRIPTS_DIR=$(realpath "${0%/*}")
. "$SCRIPTS_DIR/envsetup.sh"

# Functions defined in envsetup.sh
setup_build_environment

# Set default version codes and names, allow overrides from the calling shell
DEFAULT_VERSION_CODE=34
DEFAULT_VERSION_NAME=${BUILD_NUMBER:-34}

# Run the build
# (Keep each line separate to keep merges clean)
# (oem-token-lib's verify task is currently broken and is excluded)
# Adding the javaToolchains task to log the JDKs that the build can see
./gradlew \
    -PversionCode="${VERSION_CODE:-$DEFAULT_VERSION_CODE}" \
    -PversionName="${VERSION_NAME:-$DEFAULT_VERSION_NAME}" \
    -x :oem-token-lib:verifyReleaseResources \
    :buildLogic:javaToolchains \
    assemble \
    assembleAndroidTest \
    :buildLogic:check

# Functions defined in envsetup.sh
wrap_up_build $?
