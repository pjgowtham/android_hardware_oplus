/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package android.os;

public class OplusSystemProperties {
    public static String get(String key, String def) {
        return SystemProperties.get(key, def);
    }
}
