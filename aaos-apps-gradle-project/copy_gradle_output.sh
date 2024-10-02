#!/bin/bash

SCRIPTS_DIR=$(realpath "${0%/*}")

# Function validating the exit code of the previous command
# Usage: "check_status $?"
# Where $? returns the exit code of the previous command
# To be used after each command that needs to be validated for correctness
check_status() {
  if [[ $1 != 0 ]]
  then
    echo "check_status: non-zero exit code -> $1"
    exit $1
  else
    echo "check_status: exit code $1 continue.."
  fi
}

# Copies the aar and apks
# Usage: copy_gradle_output.sh DEST_DIR

if [ -z "${1+x}" ]
then
  echo "destination directory is required"
  exit 1
fi

if [[ -f $1 ]]
then
  echo "target $1 exists as a file!!"
  exit 1
elif [[ ! -d $1 ]]
then
  echo "creating $1 directory"
  mkdir $1
else
  echo "$1 directory already there"
fi

cd "$(dirname "$0")"
check_status $?
# Keep in sync with ./build.gradle
ROOT_DIR=$(realpath "$SCRIPTS_DIR/../../../../../")
JAVA_HOME="$ROOT_DIR/prebuilts/jdk/jdk17/linux-x86"
GRADLE_OUTPUT_DIR="$ROOT_DIR/out/aaos-apps-gradle-build"

# APKs
cp $GRADLE_OUTPUT_DIR/car-calendar-app/outputs/apk/unbundled/release/car-calendar-app-unbundled-release.apk $1/CarCalendarApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-dialer-app/outputs/apk/production/release/car-dialer-app-production-release.apk $1/CarDialerApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-app/outputs/apk/platformAosp/release/car-media-app-platformAosp-release.apk $1/CarMediaApp_aosp_cert.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-app/outputs/apk/platformGoogle/release/car-media-app-platformGoogle-release.apk $1/CarMediaApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-messenger-app/outputs/apk/prod/release/car-messenger-app-prod-release.apk $1/CarMessengerApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/PaintBooth/outputs/apk/unbundled/release/PaintBooth-unbundled-release.apk $1/PaintBooth.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/oem-token-shared-lib/outputs/apk/unbundled/release/oem-token-shared-lib-unbundled-release.apk $1/oem-token-shared-lib.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/test-rotary-playground/outputs/apk/unbundled/release/test-rotary-playground-unbundled-release.apk $1/RotaryPlayground.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/test-rotary-ime/outputs/apk/unbundled/release/test-rotary-ime-unbundled-release.apk $1/RotaryIME.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/test-media-app/automotive/outputs/apk/unbundled/release/automotive-unbundled-release.apk $1/TestMediaApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-radio-app/outputs/apk/unbundled/release/car-radio-app-unbundled-release.apk $1/CarRadioApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-bugreport-app/outputs/apk/platformGoogle/release/car-bugreport-app-platformGoogle-release.apk $1/CarBugReportApp.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-bugreport-app/outputs/apk/platformAosp/release/car-bugreport-app-platformAosp-release.apk $1/CarBugReportApp_aosp_cert.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-sample-calendar-app/outputs/apk/platform/release/car-app-card-sample-calendar-app-platform-release.apk $1/car-app-card-sample-calendar-app-platform-release.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-sample-media-app/outputs/apk/platform/release/car-app-card-sample-media-app-platform-release.apk $1/car-app-card-sample-media-app-platform-release.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-sample-weather-app/outputs/apk/platform/release/car-app-card-sample-weather-app-platform-release.apk $1/car-app-card-sample-weather-app-platform-release.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-host-sample-app/outputs/apk/platform/release/car-app-card-host-sample-app-platform-release.apk $1/car-app-card-host-sample-app-platform-release.apk
check_status $?

# AARs
cp $GRADLE_OUTPUT_DIR/car-ui-lib/outputs/aar/car-ui-lib-release.aar $1/car-ui-lib.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/oem-apis/outputs/aar/oem-apis-release.aar $1/oem-apis.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/oem-token-lib/outputs/aar/oem-token-lib-release.aar $1/oem-token-lib.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-uxr-client-lib/outputs/aar/car-uxr-client-lib-release.aar $1/car-uxr-client-lib.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-assist-lib/outputs/aar/car-assist-lib-release.aar $1/car-assist-lib.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-apps-common/outputs/aar/car-apps-common-release.aar $1/car-apps-common.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-common/outputs/aar/car-media-common-release.aar $1/car-media-common.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-extensions/outputs/aar/car-media-extensions-release.aar $1/car-media-extensions.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-telephony-common/outputs/aar/car-telephony-common-release.aar $1/car-telephony-common.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-messenger-common/model/outputs/aar/model-release.aar $1/car-messaging-models.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-messenger-common/outputs/aar/car-messenger-common-release.aar $1/car-messenger-common.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-broadcastradio-support/outputs/aar/car-broadcastradio-support-release.aar $1/car-broadcastradio-support.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-lib/outputs/aar/car-app-card-lib-release.aar $1/car-app-card-lib.aar
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-host-lib/outputs/aar/car-app-card-host-lib-release.aar $1/car-app-card-host-lib.aar
check_status $?

# Tests
cp $GRADLE_OUTPUT_DIR/car-calendar-app/outputs/apk/androidTest/unbundled/debug/car-calendar-app-unbundled-debug-androidTest.apk $1/CarCalendarUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-rotary-lib/outputs/apk/androidTest/debug/car-rotary-lib-debug-androidTest.apk $1/CarRotaryLibUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-ui-lib/outputs/apk/androidTest/debug/car-ui-lib-debug-androidTest.apk $1/CarUILibUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-dialer-app/outputs/apk/emulator/debug/car-dialer-app-emulator-debug.apk $1/CarDialerAppForTesting.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-dialer-app/outputs/apk/androidTest/emulator/debug/car-dialer-app-emulator-debug-androidTest.apk $1/CarDialerUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-telephony-common/outputs/apk/androidTest/debug/car-telephony-common-debug-androidTest.apk $1/CarTelephonyLibTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-messenger-app/outputs/apk/fake/debug/car-messenger-app-fake-debug.apk $1/CarMessengerAppForTesting.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-messenger-app/outputs/apk/androidTest/fake/debug/car-messenger-app-fake-debug-androidTest.apk $1/CarMessengerUnitTests.apk
check_status $?

cp $GRADLE_OUTPUT_DIR/car-apps-common/outputs/apk/androidTest/debug/car-apps-common-debug-androidTest.apk $1/CarAppsCommonUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-common/outputs/apk/androidTest/debug/car-media-common-debug-androidTest.apk $1/CarMediaCommonUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-app/outputs/apk/androidTest/platformGoogle/debug/car-media-app-platformGoogle-debug-androidTest.apk  $1/CarMediaUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-app/outputs/apk/androidTest/platformAosp/debug/car-media-app-platformAosp-debug-androidTest.apk  $1/CarMediaUnitTests_aosp_cert.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-media-extensions/outputs/apk/androidTest/debug/car-media-extensions-debug-androidTest.apk $1/CarMediaExtensionsUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-broadcastradio-support/outputs/apk/androidTest/debug/car-broadcastradio-support-debug-androidTest.apk $1/CarBroadcastRadioSupportLibTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-bugreport-app/outputs/apk/androidTest/platformGoogle/debug/car-bugreport-app-platformGoogle-debug-androidTest.apk $1/CarBugReportUnitTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-bugreport-app/outputs/apk/androidTest/platformAosp/debug/car-bugreport-app-platformAosp-debug-androidTest.apk $1/CarBugReportUnitTests_aosp_cert.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-lib/outputs/apk/androidTest/debug/car-app-card-lib-debug-androidTest.apk $1/CarAppCardLibTests.apk
check_status $?
cp $GRADLE_OUTPUT_DIR/car-app-card-host-lib/outputs/apk/androidTest/debug/car-app-card-host-lib-debug-androidTest.apk $1/CarAppCardHostLibTests.apk
check_status $?

# JaCoCo
mkdir $GRADLE_OUTPUT_DIR/jacoco
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-calendar-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-messenger-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-media-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-dialer-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-caruilib-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-caruilib-testing-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-uxr-client-lib-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-assist-lib-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-rotarylib-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/oem-token-lib-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-apps-common-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-media-common-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-oem-token-lib-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-telephony-common-app
check_status $?
mkdir $GRADLE_OUTPUT_DIR/jacoco/car-messenger-common-app
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-calendar-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-calendar-app/intermediates/javac/unbundledDebug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-messenger-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-messenger-app/intermediates/javac/fakeDebug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-media-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-media-app/intermediates/javac/platformGoogleDebug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-dialer-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-dialer-app/intermediates/javac/emulatorDebug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-caruilib-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-ui-lib/intermediates/javac/debug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-caruilib-testing-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-ui-lib-testing/intermediates/javac/debug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/oem-token-lib-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/oem-token-lib/intermediates/javac/debug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-uxr-client-lib-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-uxr-client-lib/intermediates/javac/debug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-assist-lib-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-assist-lib/intermediates/javac/debug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-rotarylib-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-rotary-lib/intermediates/javac/debug/classes .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-apps-common-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-apps-common/intermediates/javac/debug/classes/com/android/car/apps/common .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-media-common-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-media-common/intermediates/javac/debug/classes/com/android/car/media/common .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-oem-token-lib-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/oem-token-lib/intermediates/javac/debug/classes/com/android/car/oem/tokens .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-telephony-common-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-telephony-common/intermediates/javac/debug/classes/com/android/car/telephony .
check_status $?
$JAVA_HOME/bin/jar cvfM $GRADLE_OUTPUT_DIR/jacoco/car-messenger-common-app/jacoco-report-classes.jar -C $GRADLE_OUTPUT_DIR/car-messenger-common/intermediates/javac/debug/classes/com/android/car/messenger/common .
check_status $?
$JAVA_HOME/bin/jar cvfM $1/jacoco-report-classes-all.jar -C $GRADLE_OUTPUT_DIR/jacoco .
check_status $?
