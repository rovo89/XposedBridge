LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := XposedBridge

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        $(call all-java-files-under, lib/apache-commons-lang/external)

include $(BUILD_JAVA_LIBRARY)
$(LOCAL_INTERMEDIATE_TARGETS): PRIVATE_EXTRA_JAR_ARGS := -C "$(LOCAL_PATH)" assets -C "$(LOCAL_PATH)" NOTICE.txt

include $(call all-makefiles-under,$(LOCAL_PATH))
