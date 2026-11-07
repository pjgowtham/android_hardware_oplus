/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.camera.extensions.oplus;

import android.content.Context;
import android.util.Log;

import com.oplus.ocs.camera.extensions.unitinterface.UnitInitializerImpl;

import java.util.concurrent.Executor;
import java.util.function.IntConsumer;

/** Entry point into the Oplus camera unit SDK, which implements the actual extensions. */
public final class OplusExtensions {
    private static final String TAG = "OplusCameraExtensions";

    /** The extensions interface version the Oplus SDK implements. */
    public static final String API_VERSION = "1.4.0";

    // CameraExtensionCharacteristics extension types, which the SDK uses as well.
    public static final int MODE_AUTO = 0;
    public static final int MODE_FACE_RETOUCH = 1;
    public static final int MODE_BOKEH = 2;
    public static final int MODE_HDR = 3;
    public static final int MODE_NIGHT = 4;

    private OplusExtensions() {
    }

    public static void init(Context context, Runnable onSuccess, IntConsumer onFailure,
            Executor executor) {
        try {
            UnitInitializerImpl.init(API_VERSION, context,
                    new UnitInitializerImpl.OnExtensionsInitializedCallback() {
                        @Override
                        public void onSuccess() {
                            onSuccess.run();
                        }

                        @Override
                        public void onFailure(int error) {
                            Log.e(TAG, "Initialization failed: " + error);
                            onFailure.accept(error);
                        }
                    }, executor != null ? executor : Runnable::run);
        } catch (Throwable t) {
            Log.e(TAG, "Initialization failed", t);
            onFailure.accept(0);
        }
    }

    public static void deinit(Runnable onSuccess, IntConsumer onFailure, Executor executor) {
        try {
            UnitInitializerImpl.deinit(new UnitInitializerImpl.OnExtensionsDeinitializedCallback() {
                @Override
                public void onSuccess() {
                    onSuccess.run();
                }

                @Override
                public void onFailure(int error) {
                    onFailure.accept(error);
                }
            }, executor != null ? executor : Runnable::run);
        } catch (Throwable t) {
            Log.e(TAG, "Deinitialization failed", t);
            onFailure.accept(0);
        }
    }
}
