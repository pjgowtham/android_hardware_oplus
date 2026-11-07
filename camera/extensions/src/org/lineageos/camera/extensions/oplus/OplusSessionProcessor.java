/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.camera.extensions.oplus;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;

import androidx.camera.extensions.impl.advanced.Camera2OutputConfigImpl;
import androidx.camera.extensions.impl.advanced.Camera2SessionConfigImpl;
import androidx.camera.extensions.impl.advanced.OutputSurfaceConfigurationImpl;
import androidx.camera.extensions.impl.advanced.OutputSurfaceImpl;
import androidx.camera.extensions.impl.advanced.RequestProcessorImpl;
import androidx.camera.extensions.impl.advanced.SessionProcessorImpl;
import androidx.camera.extensions.impl.advanced.SurfaceOutputConfigImpl;

import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitCamera2OutputConfigImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitCamera2SessionConfigImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitOutputSurfaceConfigurationImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitOutputSurfaceImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitSessionProcessorImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitSurfaceOutputConfigImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Adapts an Oplus session processor to the androidx interface. */
class OplusSessionProcessor implements SessionProcessorImpl {
    private final UnitSessionProcessorImpl mUnit;
    private final int mMode;

    OplusSessionProcessor(UnitSessionProcessorImpl unit, int mode) {
        mUnit = unit;
        mMode = mode;
    }

    @Override
    public Camera2SessionConfigImpl initSession(String cameraId,
            Map<String, CameraCharacteristics> cameraCharacteristicsMap, Context context,
            OutputSurfaceConfigurationImpl surfaceConfigs,
            Map<CaptureRequest.Key<?>, Object> sessionParameters) {
        return initSession(cameraId, cameraCharacteristicsMap, context, surfaceConfigs);
    }

    @Override
    public Camera2SessionConfigImpl initSession(String cameraId,
            Map<String, CameraCharacteristics> cameraCharacteristicsMap, Context context,
            OutputSurfaceConfigurationImpl surfaceConfigs) {
        Keys.setCharacteristics(cameraCharacteristicsMap);
        return new SessionConfig(mUnit.initSession(cameraId, cameraCharacteristicsMap, context,
                new UnitOutputSurfaceConfigurationImpl() {
                    @Override
                    public UnitOutputSurfaceImpl getPreviewOutputSurface() {
                        return wrap(surfaceConfigs.getPreviewOutputSurface());
                    }

                    @Override
                    public UnitOutputSurfaceImpl getImageCaptureOutputSurface() {
                        return wrap(surfaceConfigs.getImageCaptureOutputSurface());
                    }

                    @Override
                    public UnitOutputSurfaceImpl getImageAnalysisOutputSurface() {
                        return wrap(surfaceConfigs.getImageAnalysisOutputSurface());
                    }

                    @Override
                    public UnitOutputSurfaceImpl getPostviewOutputSurface() {
                        return wrap(surfaceConfigs.getPostviewOutputSurface());
                    }
                }));
    }

    @Override
    public Camera2SessionConfigImpl initSession(String cameraId,
            Map<String, CameraCharacteristics> cameraCharacteristicsMap, Context context,
            OutputSurfaceImpl previewSurfaceConfig, OutputSurfaceImpl imageCaptureSurfaceConfig,
            OutputSurfaceImpl imageAnalysisSurfaceConfig) {
        Keys.setCharacteristics(cameraCharacteristicsMap);
        return new SessionConfig(mUnit.initSession(cameraId, cameraCharacteristicsMap, context,
                wrap(previewSurfaceConfig), wrap(imageCaptureSurfaceConfig),
                wrap(imageAnalysisSurfaceConfig)));
    }

    @Override
    public void deInitSession() {
        mUnit.deInitSession();
    }

    @Override
    public void setParameters(Map<CaptureRequest.Key<?>, Object> parameters) {
        mUnit.setParameters(parameters);
    }

    @Override
    public int startTrigger(Map<CaptureRequest.Key<?>, Object> triggers,
            CaptureCallback callback) {
        return mUnit.startTrigger(triggers, wrap(callback));
    }

    @Override
    public void onCaptureSessionStart(RequestProcessorImpl requestProcessor) {
        mUnit.onCaptureSessionStart(new OplusRequestProcessor(requestProcessor, mMode));
    }

    @Override
    public void onCaptureSessionEnd() {
        mUnit.onCaptureSessionEnd();
    }

    @Override
    public int startRepeating(CaptureCallback callback) {
        return mUnit.startRepeating(wrap(callback));
    }

    @Override
    public void stopRepeating() {
        mUnit.stopRepeating();
    }

    @Override
    public int startCapture(CaptureCallback callback) {
        return mUnit.startCapture(wrap(callback));
    }

    @Override
    public int startCaptureWithPostview(CaptureCallback callback) {
        return mUnit.startCaptureWithPostview(wrap(callback));
    }

    @Override
    public void abortCapture(int captureSequenceId) {
        mUnit.abortCapture(captureSequenceId);
    }

    @Override
    public Pair<Long, Long> getRealtimeCaptureLatency() {
        return mUnit.getRealtimeCaptureLatency();
    }

    private static UnitOutputSurfaceImpl wrap(OutputSurfaceImpl surface) {
        if (surface == null) {
            return null;
        }
        return new UnitOutputSurfaceImpl() {
            @Override
            public Surface getSurface() {
                return surface.getSurface();
            }

            @Override
            public Size getSize() {
                return surface.getSize();
            }

            @Override
            public int getImageFormat() {
                return surface.getImageFormat();
            }
        };
    }

    private static UnitSessionProcessorImpl.CaptureCallback wrap(CaptureCallback callback) {
        return new UnitSessionProcessorImpl.CaptureCallback() {
            @Override
            public void onCaptureStarted(int captureSequenceId, long timestamp) {
                callback.onCaptureStarted(captureSequenceId, timestamp);
            }

            @Override
            public void onCaptureProcessStarted(int captureSequenceId) {
                callback.onCaptureProcessStarted(captureSequenceId);
            }

            @Override
            public void onCaptureFailed(int captureSequenceId) {
                callback.onCaptureFailed(captureSequenceId);
            }

            @Override
            public void onCaptureSequenceCompleted(int captureSequenceId) {
                callback.onCaptureSequenceCompleted(captureSequenceId);
            }

            @Override
            public void onCaptureSequenceAborted(int captureSequenceId) {
                callback.onCaptureSequenceAborted(captureSequenceId);
            }

            @Override
            public void onCaptureCompleted(long timestamp, int captureSequenceId,
                    Map<CaptureResult.Key, Object> result) {
                callback.onCaptureCompleted(timestamp, captureSequenceId, Keys.filterResult(result));
            }

            @Override
            public void onCaptureProcessProgressed(int progress) {
                callback.onCaptureProcessProgressed(progress);
            }
        };
    }

    private static Camera2OutputConfigImpl wrap(UnitCamera2OutputConfigImpl config) {
        if (!(config instanceof UnitSurfaceOutputConfigImpl)) {
            throw new IllegalArgumentException("Unsupported output config " + config);
        }
        UnitSurfaceOutputConfigImpl surfaceConfig = (UnitSurfaceOutputConfigImpl) config;
        List<Camera2OutputConfigImpl> sharing = null;
        if (config.getSurfaceSharingOutputConfigs() != null) {
            sharing = new ArrayList<>();
            for (UnitCamera2OutputConfigImpl c : config.getSurfaceSharingOutputConfigs()) {
                sharing.add(wrap(c));
            }
        }
        final List<Camera2OutputConfigImpl> sharingConfigs = sharing;
        return new SurfaceOutputConfigImpl() {
            @Override
            public Surface getSurface() {
                return surfaceConfig.getSurface();
            }

            @Override
            public int getId() {
                return surfaceConfig.getId();
            }

            @Override
            public int getSurfaceGroupId() {
                return surfaceConfig.getSurfaceGroupId();
            }

            @Override
            public String getPhysicalCameraId() {
                return surfaceConfig.getPhysicalCameraId();
            }

            @Override
            public List<Camera2OutputConfigImpl> getSurfaceSharingOutputConfigs() {
                return sharingConfigs;
            }
        };
    }

    private static class SessionConfig implements Camera2SessionConfigImpl {
        private final UnitCamera2SessionConfigImpl mUnit;
        private final List<Camera2OutputConfigImpl> mOutputConfigs = new ArrayList<>();

        SessionConfig(UnitCamera2SessionConfigImpl unit) {
            mUnit = unit;
            for (UnitCamera2OutputConfigImpl config : unit.getOutputConfigs()) {
                mOutputConfigs.add(wrap(config));
            }
        }

        @Override
        public List<Camera2OutputConfigImpl> getOutputConfigs() {
            return mOutputConfigs;
        }

        @Override
        public Map<CaptureRequest.Key<?>, Object> getSessionParameters() {
            return Keys.filterRequest(mUnit.getSessionParameters());
        }

        @Override
        public int getSessionTemplateId() {
            return mUnit.getSessionTemplateId();
        }

        @Override
        public int getSessionType() {
            return mUnit.getSessionType();
        }
    }
}
