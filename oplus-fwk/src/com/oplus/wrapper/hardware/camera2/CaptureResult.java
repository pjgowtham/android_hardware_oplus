/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.wrapper.hardware.camera2;

import com.oplus.wrapper.hardware.camera2.impl.CameraMetadataNative;

import java.lang.reflect.Field;

public class CaptureResult {
    private final android.hardware.camera2.CaptureResult mCaptureResult;

    public CaptureResult(android.hardware.camera2.CaptureResult captureResult) {
        mCaptureResult = captureResult;
    }

    // Stock adds CaptureResult.getNativeMetadata() for this; read the field it returns instead.
    public CameraMetadataNative getNativeMetadata() {
        try {
            Field results = android.hardware.camera2.CaptureResult.class.getDeclaredField("mResults");
            results.setAccessible(true);
            Object metadata = results.get(mCaptureResult);
            return metadata == null ? null : new CameraMetadataNative(
                    (android.hardware.camera2.impl.CameraMetadataNative) metadata);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
