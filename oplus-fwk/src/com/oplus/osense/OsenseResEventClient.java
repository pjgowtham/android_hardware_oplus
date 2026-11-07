/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.osense;

import com.oplus.osense.eventinfo.EventConfig;
import com.oplus.osense.eventinfo.OsenseEventCallback;

public class OsenseResEventClient {
    private static OsenseResEventClient sInstance;

    public static OsenseResEventClient getInstance() {
        if (sInstance == null) {
            sInstance = new OsenseResEventClient();
        }
        return sInstance;
    }

    public int registerEventCallback(OsenseEventCallback callback, EventConfig config) {
        return -1;
    }

    public int unregisterEventCallback(OsenseEventCallback callback) {
        return -1;
    }
}
