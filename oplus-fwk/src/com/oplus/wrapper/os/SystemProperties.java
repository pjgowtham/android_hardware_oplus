/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.wrapper.os;

public class SystemProperties {
    public static boolean getBoolean(String key, boolean def) {
        return android.os.SystemProperties.getBoolean(key, def);
    }

    public static int getInt(String key, int def) {
        return android.os.SystemProperties.getInt(key, def);
    }
}
