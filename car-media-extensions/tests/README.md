Tests can be ran with SOONG/atest or Gradle:

SOONG:
mmma -j64 packages/apps/Car/libs/car-media-extensions
atest car-media-extensions-unit-tests

Gradle:
cd ../libs/aaos-apps-gradle-project
./gradlew  :car-media-extensions:test