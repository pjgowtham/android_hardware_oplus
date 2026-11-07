/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package androidx.camera.extensions.impl;

import android.content.Context;

import org.lineageos.camera.extensions.oplus.OplusExtensions;

import java.util.concurrent.Executor;

public class InitializerImpl {
    public static final int ERROR_UNKNOWN = 0;
    public static final int ERROR_INITIALIZE_VERSION_INCOMPATIBLE = 1;

    private InitializerImpl() {
    }

    public static void init(String version, Context context,
            OnExtensionsInitializedCallback callback, Executor executor) {
        OplusExtensions.init(context, callback::onSuccess, callback::onFailure, executor);
    }

    public static void deinit(OnExtensionsDeinitializedCallback callback, Executor executor) {
        OplusExtensions.deinit(callback::onSuccess, callback::onFailure, executor);
    }

    public interface OnExtensionsInitializedCallback {
        void onSuccess();
        void onFailure(int error);
    }

    public interface OnExtensionsDeinitializedCallback {
        void onSuccess();
        void onFailure(int error);
    }
}
