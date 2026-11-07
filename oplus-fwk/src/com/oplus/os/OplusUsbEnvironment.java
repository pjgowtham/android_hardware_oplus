/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.os;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class OplusUsbEnvironment {
    public static File getInternalSdDirectory(Context context) {
        return Environment.getExternalStorageDirectory();
    }
}
