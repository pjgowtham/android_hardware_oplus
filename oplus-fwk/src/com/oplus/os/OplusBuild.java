/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.os;

import android.os.SystemProperties;

public class OplusBuild {
    public static class VERSION {
        public static final String RELEASE = SystemProperties.get("ro.build.version.oplusrom", "");
        public static final int SDK_VERSION = SystemProperties.getInt("ro.build.version.oplus.api", 0);
        public static final int SDK_SUB_VERSION = SystemProperties.getInt("ro.build.version.oplus.sub_api", 1);
    }
}
