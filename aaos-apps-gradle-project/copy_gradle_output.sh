#!/bin/bash

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

cd "$(dirname "$0")" || exit
# Keep in sync with ./build.gradle
OUTPUT_DIR=../../../../../out/aaos-apps-gradle-build

# APKs
cp $OUTPUT_DIR/car-calendar-app/outputs/apk/prod/release/car-calendar-app-prod-release.apk $1/CarCalendarApp.apk
cp $OUTPUT_DIR/car-calendar-app/outputs/apk/platform/release/car-calendar-app-platform-release.apk $1/CarCalendarApp_platform_cert.apk
cp $OUTPUT_DIR/car-dialer-app/outputs/apk/production/release/car-dialer-app-production-release.apk $1/CarDialerApp.apk
cp $OUTPUT_DIR/car-dialer-app/outputs/apk/platform/release/car-dialer-app-platform-release.apk $1/CarDialerApp_platform_cert.apk
cp $OUTPUT_DIR/car-media-app/outputs/apk/aaos/release/car-media-app-aaos-release.apk $1/CarMediaApp_aaos_cert.apk
cp $OUTPUT_DIR/car-media-app/outputs/apk/platform/release/car-media-app-platform-release.apk $1/CarMediaApp.apk
cp $OUTPUT_DIR/car-messenger-app/outputs/apk/prod/release/car-messenger-app-prod-release.apk $1/CarMessengerApp.apk
cp $OUTPUT_DIR/car-messenger-app/outputs/apk/platform/release/car-messenger-app-platform-release.apk $1/CarMessengerApp_platform_cert.apk
cp $OUTPUT_DIR/PaintBooth/outputs/apk/aaos/release/PaintBooth-aaos-release.apk $1/PaintBooth.apk
cp $OUTPUT_DIR/PaintBooth/outputs/apk/platform/release/PaintBooth-platform-release.apk $1/PaintBooth_platform_cert.apk
cp $OUTPUT_DIR/oem-token-shared-lib/outputs/apk/aaos/release/oem-token-shared-lib-aaos-release.apk $1/oem-token-shared-lib.apk
cp $OUTPUT_DIR/oem-token-shared-lib/outputs/apk/platform/release/oem-token-shared-lib-platform-release.apk $1/oem-token-shared-lib_platform_cert.apk
cp $OUTPUT_DIR/test-rotary-playground/outputs/apk/aaos/release/test-rotary-playground-aaos-release.apk $1/RotaryPlayground.apk
cp $OUTPUT_DIR/test-rotary-playground/outputs/apk/platform/release/test-rotary-playground-platform-release.apk $1/RotaryPlayground_platform_cert.apk
cp $OUTPUT_DIR/test-rotary-ime/outputs/apk/aaos/release/test-rotary-ime-aaos-release.apk $1/RotaryIME.apk
cp $OUTPUT_DIR/test-rotary-ime/outputs/apk/platform/release/test-rotary-ime-platform-release.apk $1/RotaryIME_platform_cert.apk
cp $OUTPUT_DIR/test-media-app/automotive/outputs/apk/aaos/release/automotive-aaos-release.apk $1/TestMediaApp.apk
cp $OUTPUT_DIR/test-media-app/automotive/outputs/apk/platform/release/automotive-platform-release.apk $1/TestMediaApp_platform_cert.apk
cp $OUTPUT_DIR/car-radio-app/outputs/apk/aaos/release/car-radio-app-aaos-release.apk $1/CarRadioApp.apk
cp $OUTPUT_DIR/car-bugreport-app/outputs/apk/prod/release/car-bugreport-app-prod-release.apk $1/CarBugReportApp.apk
cp $OUTPUT_DIR/car-app-card-sample-calendar-app/outputs/apk/platform/release/car-app-card-sample-calendar-app-platform-release.apk $1/car-app-card-sample-calendar-app-platform-release.apk
cp $OUTPUT_DIR/car-app-card-sample-media-app/outputs/apk/platform/release/car-app-card-sample-media-app-platform-release.apk $1/car-app-card-sample-media-app-platform-release.apk
cp $OUTPUT_DIR/car-app-card-host-sample-app/outputs/apk/platform/release/car-app-card-host-sample-app-platform-release.apk $1/car-app-card-host-sample-app-platform-release.apk

# AARs
cp $OUTPUT_DIR/car-ui-lib/outputs/aar/car-ui-lib-release.aar $1/car-ui-lib.aar
cp $OUTPUT_DIR/oem-apis/outputs/aar/oem-apis-release.aar $1/oem-apis.aar
cp $OUTPUT_DIR/oem-token-lib/outputs/aar/oem-token-lib-release.aar $1/oem-token-lib.aar
cp $OUTPUT_DIR/car-uxr-client-lib/outputs/aar/car-uxr-client-lib-release.aar $1/car-uxr-client-lib.aar
cp $OUTPUT_DIR/car-assist-lib/outputs/aar/car-assist-lib-release.aar $1/car-assist-lib.aar
cp $OUTPUT_DIR/car-apps-common/outputs/aar/car-apps-common-release.aar $1/car-apps-common.aar
cp $OUTPUT_DIR/car-media-common/outputs/aar/car-media-common-release.aar $1/car-media-common.aar
cp $OUTPUT_DIR/car-media-extensions/outputs/aar/car-media-extensions-release.aar $1/car-media-extensions.aar
cp $OUTPUT_DIR/car-telephony-common/outputs/aar/car-telephony-common-release.aar $1/car-telephony-common.aar
cp $OUTPUT_DIR/car-messenger-common/model/outputs/aar/model-release.aar $1/car-messaging-models.aar
cp $OUTPUT_DIR/car-messenger-common/outputs/aar/car-messenger-common-release.aar $1/car-messenger-common.aar
cp $OUTPUT_DIR/car-broadcastradio-support/outputs/aar/car-broadcastradio-support-release.aar $1/car-broadcastradio-support.aar
cp $OUTPUT_DIR/car-app-card-lib/outputs/aar/car-app-card-lib-release.aar $1/car-app-card-lib.aar
cp $OUTPUT_DIR/car-app-card-host-lib/outputs/aar/car-app-card-host-lib-release.aar $1/car-app-card-host-lib.aar

# Tests
cp $OUTPUT_DIR/car-calendar-app/outputs/apk/androidTest/platform/debug/car-calendar-app-platform-debug-androidTest.apk $1/CarCalendarUnitTests.apk
cp $OUTPUT_DIR/car-rotary-lib/outputs/apk/androidTest/debug/car-rotary-lib-debug-androidTest.apk $1/CarRotaryLibUnitTests.apk
cp $OUTPUT_DIR/car-ui-lib/outputs/apk/androidTest/debug/car-ui-lib-debug-androidTest.apk $1/CarUILibUnitTests.apk
cp $OUTPUT_DIR/car-dialer-app/outputs/apk/emulator/debug/car-dialer-app-emulator-debug.apk $1/CarDialerAppForTesting.apk
cp $OUTPUT_DIR/car-dialer-app/outputs/apk/androidTest/emulator/debug/car-dialer-app-emulator-debug-androidTest.apk $1/CarDialerUnitTests.apk
cp $OUTPUT_DIR/car-telephony-common/outputs/apk/androidTest/debug/car-telephony-common-debug-androidTest.apk $1/CarTelephonyLibTests.apk
cp $OUTPUT_DIR/car-messenger-app/outputs/apk/fake/debug/car-messenger-app-fake-debug.apk $1/CarMessengerAppForTesting.apk
cp $OUTPUT_DIR/car-messenger-app/outputs/apk/androidTest/fake/debug/car-messenger-app-fake-debug-androidTest.apk $1/CarMessengerUnitTests.apk

cp $OUTPUT_DIR/car-apps-common/outputs/apk/androidTest/debug/car-apps-common-debug-androidTest.apk $1/CarAppsCommonUnitTests.apk
cp $OUTPUT_DIR/car-media-common/outputs/apk/androidTest/debug/car-media-common-debug-androidTest.apk $1/CarMediaCommonUnitTests.apk
cp $OUTPUT_DIR/car-media-app/outputs/apk/androidTest/platform/debug/car-media-app-platform-debug-androidTest.apk  $1/CarMediaUnitTests.apk
cp $OUTPUT_DIR/car-media-extensions/outputs/apk/androidTest/debug/car-media-extensions-debug-androidTest.apk $1/CarMediaExtensionsUnitTests.apk
cp $OUTPUT_DIR/car-broadcastradio-support/outputs/apk/androidTest/debug/car-broadcastradio-support-debug-androidTest.apk $1/CarBroadcastRadioSupportLibTests.apk
cp $OUTPUT_DIR/car-bugreport-app/outputs/apk/androidTest/platform/debug/car-bugreport-app-platform-debug-androidTest.apk $1/CarBugReportUnitTests.apk
cp $OUTPUT_DIR/car-app-card-lib/outputs/apk/androidTest/debug/car-app-card-lib-debug-androidTest.apk $1/CarAppCardLibTests.apk
cp $OUTPUT_DIR/car-app-card-host-lib/outputs/apk/androidTest/debug/car-app-card-host-lib-debug-androidTest.apk $1/CarAppCardHostLibTests.apk

# JaCoCo
mkdir $1/jacoco
mkdir $1/jacoco/car-calendar-app
mkdir $1/jacoco/car-messenger-app
mkdir $1/jacoco/car-media-app/
mkdir $1/jacoco/car-dialer-app
jar cvfM $1/jacoco/car-calendar-app/jacoco-report-classes.jar -C $OUTPUT_DIR/car-calendar-app/intermediates/jacoco_instrumented_classes/platformDebug/out .
jar cvfM $1/jacoco/car-messenger-app/jacoco-report-classes.jar -C $OUTPUT_DIR/car-messenger-app/intermediates/jacoco_instrumented_classes/platformDebug/out .
jar cvfM $1/jacoco/car-media-app/jacoco-report-classes.jar -C $OUTPUT_DIR/car-media-app/intermediates/jacoco_instrumented_classes/platformDebug/out .
jar cvfM $1/jacoco/car-dialer-app/jacoco-report-classes.jar -C $OUTPUT_DIR/car-dialer-app/intermediates/jacoco_instrumented_classes/platformDebug/out .
jar cvfM $1/jacoco-report-classes-all.zip -C $1/jacoco .
rm -r $1/jacoco
