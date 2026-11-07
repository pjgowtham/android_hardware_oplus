/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package android.hardware.camera2;

import android.content.Context;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * The parts of the Oplus camera manager the camera unit SDK calls. Stock forwards the client
 * bookkeeping to an Oplus cameraserver, which doesn't exist here.
 */
public final class OplusCameraManager {
    private static final String TAG = "OplusCameraManager";
    private static final OplusCameraManager sInstance = new OplusCameraManager();

    private OplusCameraManager() {
    }

    public static OplusCameraManager getInstance() {
        return sInstance;
    }

    public static Object getEmptyCameraMetadataNative(long[] metadataPtr) {
        CameraMetadataNative meta = new CameraMetadataNative();
        if (metadataPtr != null && metadataPtr.length > 0) {
            metadataPtr[0] = meta.getMetadataPtr();
        }
        return meta;
    }

    public static TotalCaptureResult generateTotalCaptureResult(Object meta, long frameId) {
        if (!(meta instanceof CameraMetadataNative)) {
            return null;
        }
        TotalCaptureResult result = new TotalCaptureResult((CameraMetadataNative) meta, 0);
        try {
            Field frameNumber = CaptureResult.class.getDeclaredField("mFrameNumber");
            frameNumber.setAccessible(true);
            frameNumber.setLong(result, frameId);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "Failed to set the frame number", e);
        }
        return result;
    }

    public boolean isAuthedClient(Context context) {
        return false;
    }

    public void preOpenCamera(Context context) {
    }

    public void setCallInfo() {
    }

    public void setDeathRecipient(IBinder client) {
    }
}
