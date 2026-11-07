/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package androidx.camera.extensions.impl.advanced;

import org.lineageos.camera.extensions.oplus.OplusAdvancedExtender;
import org.lineageos.camera.extensions.oplus.OplusExtensions;

public class AutoAdvancedExtenderImpl extends OplusAdvancedExtender {
    public AutoAdvancedExtenderImpl() {
        super(OplusExtensions.MODE_AUTO);
    }
}
