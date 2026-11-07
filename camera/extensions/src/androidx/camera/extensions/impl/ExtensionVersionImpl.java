/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package androidx.camera.extensions.impl;

import org.lineageos.camera.extensions.oplus.OplusExtensions;

public class ExtensionVersionImpl {
    public ExtensionVersionImpl() {
    }

    public String checkApiVersion(String version) {
        return OplusExtensions.API_VERSION;
    }

    public boolean isAdvancedExtenderImplemented() {
        return true;
    }
}
