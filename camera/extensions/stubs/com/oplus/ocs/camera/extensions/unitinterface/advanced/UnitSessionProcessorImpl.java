package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Pair;

import java.util.Map;

public interface UnitSessionProcessorImpl {
    UnitCamera2SessionConfigImpl initSession(String cameraId,
            Map<String, CameraCharacteristics> chars, Context context,
            UnitOutputSurfaceConfigurationImpl surfaceConfigs);
    UnitCamera2SessionConfigImpl initSession(String cameraId,
            Map<String, CameraCharacteristics> chars, Context context,
            UnitOutputSurfaceImpl preview, UnitOutputSurfaceImpl imageCapture,
            UnitOutputSurfaceImpl imageAnalysis);
    void deInitSession();
    void setParameters(Map<CaptureRequest.Key<?>, Object> parameters);
    int startTrigger(Map<CaptureRequest.Key<?>, Object> triggers, CaptureCallback callback);
    void onCaptureSessionStart(UnitRequestProcessorImpl requestProcessor);
    void onCaptureSessionEnd();
    int startRepeating(CaptureCallback callback);
    void stopRepeating();
    int startCapture(CaptureCallback callback);
    int startCaptureWithPostview(CaptureCallback callback);
    void abortCapture(int captureSequenceId);
    Pair<Long, Long> getRealtimeCaptureLatency();

    interface CaptureCallback {
        void onCaptureStarted(int captureSequenceId, long timestamp);
        void onCaptureProcessStarted(int captureSequenceId);
        void onCaptureFailed(int captureSequenceId);
        void onCaptureSequenceCompleted(int captureSequenceId);
        void onCaptureSequenceAborted(int captureSequenceId);
        void onCaptureCompleted(long timestamp, int captureSequenceId,
                Map<CaptureResult.Key, Object> result);
        void onCaptureProcessProgressed(int progress);
    }
}
