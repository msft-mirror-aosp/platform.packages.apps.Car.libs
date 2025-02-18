#!/bin/bash
set -eou pipefail

# Resolve various absolute paths
SCRIPTS_DIR=$(realpath "${0%/*}")

setup_build_environment() {
    GRADLE_PROJ_DIR=$(realpath "$SCRIPTS_DIR/..")
    ROOT_DIR=$(realpath "$SCRIPTS_DIR/../../../../../../")

    OS=linux
    if [[ $(uname) = "Darwin" ]]; then OS="darwin"; fi

    ANDROID_HOME="$ROOT_DIR/prebuilts/fullsdk-$OS"
    echo "Setting ANDROID_HOME to $ANDROID_HOME"

    # Disable the build daemon
    # Either set Gradle opts or prepend to it with a comma (separator) if it exists
    GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.java.installations.auto-detect=false ${GRADLE_OPTS:+,${GRADLE_OPTS}}"

    # Export everything we need
    export ANDROID_HOME
    export GRADLE_PROJ_DIR
    export GRADLE_OPTS
    export BUSYTOWN_BUILD=true

    echo "Starting $0 at $(date)"

    # In order for $ANDROID_HOME to be used we need to temporarily remove local.properties.
    # It will be restored after the build is completed
    if [ -f "$GRADLE_PROJ_DIR/local.properties" ]; then
        # Check to see if the file already points to the ANDROID_HOME that this build will use. If it does then leave it alone.
        local_properties_android_home=$(grep "sdk.dir" "$GRADLE_PROJ_DIR/local.properties" | awk -F'=' '{print $2}')

        if [[ "$local_properties_android_home" != "$ANDROID_HOME" ]]; then
            # Rename local.properties to backup_local.properties
            mv "$GRADLE_PROJ_DIR/local.properties" "$GRADLE_PROJ_DIR/backup_local.properties"
            echo "local.properties renamed to backup_local.properties"
        else
            echo "local.properties already points to the correct ANDROID_HOME. Leaving it in place"
        fi

    else
        echo "local.properties not found."
    fi

    cd "$GRADLE_PROJ_DIR"

    # Disable exit on error so we can handle a failed build
    set +e
}

wrap_up_build() {
    RC=$1

    # Restore the original local.properties file (if there was one)
    if [ -f "$GRADLE_PROJ_DIR/backup_local.properties" ]; then
        # Rename local.properties to backup_local.properties
        mv "$GRADLE_PROJ_DIR/backup_local.properties" "$GRADLE_PROJ_DIR/local.properties"
        echo "Restored local.properties"
    else
        echo "Unable to restore local.properties. Re-launch Android Studio to recreate it"
    fi

    echo "Completing $0 at $(date)"
    if [[ $RC != 0 ]]; then
        echo "check_status: non-zero exit code -> $RC. Failing out"
        exit "$RC"
    else
        echo "check_status: exit code $RC is good. Continuing"
    fi
}
