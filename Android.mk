LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Build the Blissify APK
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := Blissify
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS := optional
LOCAL_USE_AAPT2 := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_ANDROID_LIBRARIES := \
    $(ANDROID_SUPPORT_DESIGN_TARGETS) \
    android-slices-builders \
    android-slices-core \
    android-slices-view \
    android-support-compat \
    android-support-v4 \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v14-preference \
    android-support-transition \
    android-support-v7-palette \

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-arch-lifecycle-runtime \
    android-arch-lifecycle-extensions \
    guava \
    jsr305 \

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_AAPT_FLAGS := --auto-add-overlay \

include frameworks/opt/setupwizard/library/common-gingerbread.mk
include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)
