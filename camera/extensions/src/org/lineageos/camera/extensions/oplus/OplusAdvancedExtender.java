/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.camera.extensions.oplus;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Log;
import android.util.Pair;
import android.util.Range;
import android.util.Size;

import androidx.camera.extensions.impl.advanced.AdvancedExtenderImpl;
import androidx.camera.extensions.impl.advanced.SessionProcessorImpl;

import com.oplus.ocs.camera.extensions.ExtensionClient;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitAdvancedExtenderImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Exposes one Oplus extension mode through the androidx advanced extender interface. */
public class OplusAdvancedExtender implements AdvancedExtenderImpl {
    private static final String TAG = "OplusCameraExtensions";

    private final int mMode;
    private UnitAdvancedExtenderImpl mDelegate;

    protected OplusAdvancedExtender(int mode) {
        mMode = mode;
    }

    private synchronized UnitAdvancedExtenderImpl delegate() {
        if (mDelegate == null) {
            mDelegate = ExtensionClient.getInstance().getExtensionDelegate(mMode);
        }
        return mDelegate;
    }

    @Override
    public boolean isExtensionAvailable(String cameraId,
            Map<String, CameraCharacteristics> characteristicsMap) {
        Keys.setCharacteristics(characteristicsMap);
        try {
            UnitAdvancedExtenderImpl delegate = delegate();
            return delegate != null && delegate.isExtensionAvailable(cameraId, characteristicsMap);
        } catch (Throwable t) {
            Log.e(TAG, "isExtensionAvailable failed for mode " + mMode, t);
            return false;
        }
    }

    @Override
    public void init(String cameraId, Map<String, CameraCharacteristics> characteristicsMap) {
        Keys.setCharacteristics(characteristicsMap);
        delegate().init(cameraId, characteristicsMap);
    }

    @Override
    public Range<Long> getEstimatedCaptureLatencyRange(String cameraId, Size captureOutputSize,
            int imageFormat) {
        return delegate().getEstimatedCaptureLatencyRange(cameraId, captureOutputSize,
                imageFormat);
    }

    @Override
    public Map<Integer, List<Size>> getSupportedPreviewOutputResolutions(String cameraId) {
        return delegate().getSupportedPreviewOutputResolutions(cameraId);
    }

    @Override
    public Map<Integer, List<Size>> getSupportedCaptureOutputResolutions(String cameraId) {
        return delegate().getSupportedCaptureOutputResolutions(cameraId);
    }

    @Override
    public Map<Integer, List<Size>> getSupportedPostviewResolutions(Size captureSize) {
        return delegate().getSupportedPostviewResolutions(captureSize);
    }

    @Override
    public List<Size> getSupportedYuvAnalysisResolutions(String cameraId) {
        return delegate().getSupportedYuvAnalysisResolutions(cameraId);
    }

    @Override
    public SessionProcessorImpl createSessionProcessor() {
        return new OplusSessionProcessor(delegate().createSessionProcessor(), mMode);
    }

    @Override
    public List<CaptureRequest.Key> getAvailableCaptureRequestKeys() {
        return Keys.filter(delegate().getAvailableCaptureRequestKeys());
    }

    @Override
    public List<CaptureResult.Key> getAvailableCaptureResultKeys() {
        return Keys.filter(delegate().getAvailableCaptureResultKeys());
    }

    @Override
    public boolean isCaptureProcessProgressAvailable() {
        return delegate().isCaptureProcessProgressAvailable();
    }

    @Override
    public boolean isPostviewAvailable() {
        return delegate().isPostviewAvailable();
    }

    @Override
    public List<Pair<CameraCharacteristics.Key, Object>> getAvailableCharacteristicsKeyValues() {
        return Collections.emptyList();
    }
}
