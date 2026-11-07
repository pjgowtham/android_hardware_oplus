package com.oplus.ocs.camera.extensions.unitinterface;

import android.content.Context;

import java.util.concurrent.Executor;

public class UnitInitializerImpl {
    public static void init(String version, Context context,
            OnExtensionsInitializedCallback callback, Executor executor) {
        throw new RuntimeException("Stub");
    }

    public static void deinit(OnExtensionsDeinitializedCallback callback, Executor executor) {
        throw new RuntimeException("Stub");
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
