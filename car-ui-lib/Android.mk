#
# Copyright (C) 2019 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Including generate_rros.mk utility.
# Usage:
#
# LOCAL_PATH := $(call my-dir)
#
# CAR_UI_RRO_SET_NAME := sample
# CAR_UI_RESOURCE_DIR := $(LOCAL_PATH)/res
# CAR_UI_RRO_TARGETS := \
#   com.your.package.name.1 \
#   com.your.package.name.2 \
#   com.your.package.name.3
#
# include $(CAR_UI_GENERATE_RRO_SET)
#
# Your AndroidManifest must use {{TARGET_PACKAGE_NAME}} and {{RRO_PACKAGE_NAME}}
# tags, which will be replaced accordingly during build.

CAR_UI_GENERATE_RRO_SET := $(call my-dir)/generate_rros.mk

# Build car-ui library

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_MODULE := car-ui-lib

LOCAL_MODULE_TAGS := optional

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_USE_AAPT2 := true

LOCAL_JAVA_LIBRARIES += android.car

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_ANDROID_LIBRARIES += \
    androidx.annotation_annotation \
    androidx.appcompat_appcompat \
    androidx-constraintlayout_constraintlayout \
    androidx.preference_preference \
    androidx.recyclerview_recyclerview \
    androidx.asynclayoutinflater_asynclayoutinflater \

LOCAL_STATIC_JAVA_LIBRARIES += \
    androidx-constraintlayout_constraintlayout-solver \

include $(BUILD_STATIC_JAVA_LIBRARY)

ifeq (,$(ONE_SHOT_MAKEFILE))
    include $(call all-makefiles-under,$(LOCAL_PATH))
endif
