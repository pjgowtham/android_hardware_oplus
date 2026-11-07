/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package androidx.camera.extensions.impl.advanced;

import org.lineageos.camera.extensions.oplus.OplusAdvancedExtender;
import org.lineageos.camera.extensions.oplus.OplusExtensions;

public class BeautyAdvancedExtenderImpl extends OplusAdvancedExtender {
    public BeautyAdvancedExtenderImpl() {
        super(OplusExtensions.MODE_FACE_RETOUCH);
    }
}
