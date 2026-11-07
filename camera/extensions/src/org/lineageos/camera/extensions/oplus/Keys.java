/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.camera.extensions.oplus;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Oplus SDK targets newer HALs and refers to keys, such as com.oplus.camera.mode, that
 * older HALs lack. The framework throws on any key it cannot map to a tag, so drop them.
 */
final class Keys {
    private static final String TAG = "OplusCameraExtensions";
    private static final Map<String, Boolean> sKnown = new ConcurrentHashMap<>();
    private static volatile long sVendorId = Long.MAX_VALUE;

    private Keys() {
    }

    /** Vendor tags only resolve against the vendor id of the provider that defines them. */
    static void setCharacteristics(Map<String, CameraCharacteristics> characteristicsMap) {
        if (sVendorId != Long.MAX_VALUE || characteristicsMap == null) {
            return;
        }
        for (CameraCharacteristics characteristics : characteristicsMap.values()) {
            for (CaptureRequest.Key<?> key : characteristics.getAvailableCaptureRequestKeys()) {
                if (key.getVendorId() != Long.MAX_VALUE) {
                    sVendorId = key.getVendorId();
                    sKnown.clear();
                    return;
                }
            }
        }
    }

    static boolean isKnown(String name) {
        return sKnown.computeIfAbsent(name, n -> {
            try {
                CameraMetadataNative.getTag(n, sVendorId);
                return true;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Dropping unsupported key " + n);
                return false;
            }
        });
    }

    static <K> List<K> filter(List<K> keys) {
        if (keys == null) {
            return null;
        }
        List<K> out = new ArrayList<>();
        for (K key : keys) {
            if (isKnown(name(key))) {
                out.add(key);
            }
        }
        return out;
    }

    static <V> Map<CaptureRequest.Key<?>, V> filterRequest(Map<CaptureRequest.Key<?>, V> map) {
        if (map == null) {
            return null;
        }
        Map<CaptureRequest.Key<?>, V> out = new HashMap<>();
        map.forEach((key, value) -> {
            if (isKnown(key.getName())) {
                out.put(key, value);
            }
        });
        return out;
    }

    static <V> Map<CaptureResult.Key, V> filterResult(Map<CaptureResult.Key, V> map) {
        if (map == null) {
            return null;
        }
        Map<CaptureResult.Key, V> out = new HashMap<>();
        map.forEach((key, value) -> {
            if (isKnown(key.getName())) {
                out.put(key, value);
            }
        });
        return out;
    }

    private static String name(Object key) {
        if (key instanceof CaptureRequest.Key) {
            return ((CaptureRequest.Key<?>) key).getName();
        }
        if (key instanceof CaptureResult.Key) {
            return ((CaptureResult.Key<?>) key).getName();
        }
        return String.valueOf(key);
    }
}
