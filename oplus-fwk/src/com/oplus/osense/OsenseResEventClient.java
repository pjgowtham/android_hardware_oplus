/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.osense;

public class OsenseResEventClient {
    private static OsenseResEventClient sInstance;

    public static OsenseResEventClient getInstance() {
        if (sInstance == null) {
            sInstance = new OsenseResEventClient();
        }
        return sInstance;
    }
}
