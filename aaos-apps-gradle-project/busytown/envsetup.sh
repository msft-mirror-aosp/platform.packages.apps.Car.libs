#!/bin/bash
set -eou pipefail

# Resolve various absolute paths
SCRIPTS_DIR=$(realpath "${0%/*}")
GRADLE_PROJ_DIR=$(realpath "$SCRIPTS_DIR/..")
ROOT_DIR=$(realpath "$SCRIPTS_DIR/../../../../../../")

ANDROID_HOME="$ROOT_DIR/prebuilts/fullsdk-linux"
JAVA_HOME="$ROOT_DIR/prebuilts/jdk/jdk17/linux-x86"

# The prebuilt Android SDK is included with the Gradle-only checkout but
# the Java SDK is not. (Typically you use your own JDK)
if [ ! -d "$JAVA_HOME" ]; then
    echo "Missing JAVA_HOME $JAVA_HOME." >&2
    echo "Hint: prebuilts/jdk is not included with Gradle-only checkouts" >&2
    exit 1
fi

# Disable the build daemon
# Either set Gradle opts or prepend to it with a comma (separator) if it exists
GRADLE_OPTS="-Dorg.gradle.daemon=false${GRADLE_OPTS:+,${GRADLE_OPTS}}"

# Export everything we need
export JAVA_HOME
export ANDROID_HOME
export GRADLE_PROJ_DIR
export GRADLE_OPTS
