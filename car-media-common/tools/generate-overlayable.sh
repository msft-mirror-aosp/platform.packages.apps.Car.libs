#!/bin/bash
#  Copyright (C) 2021 The Android Open Source Project
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# Run this script to regenerate the overlayable.xml file.

cd $(dirname $0)
cd ..

PROJECT_TOP="$(pwd)"
export PROJECT_TOP
cd ../../../../..

ANDROID_BUILD_TOP="$(pwd)"
export ANDROID_BUILD_TOP

python3 $ANDROID_BUILD_TOP/packages/apps/Car/tests/tools/rro/generate-overlayable.py \
    -n car-media-common \
    -r $PROJECT_TOP/res \
    -e $PROJECT_TOP/res/values/overlayable.xml \
    -o $PROJECT_TOP/res/values/overlayable.xml
