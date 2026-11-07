package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Range;
import android.util.Size;

import java.util.List;
import java.util.Map;

public interface UnitAdvancedExtenderImpl {
    boolean isExtensionAvailable(String cameraId, Map<String, CameraCharacteristics> chars);
    void init(String cameraId, Map<String, CameraCharacteristics> chars);
    Range<Long> getEstimatedCaptureLatencyRange(String cameraId, Size size, int format);
    Map<Integer, List<Size>> getSupportedPreviewOutputResolutions(String cameraId);
    Map<Integer, List<Size>> getSupportedCaptureOutputResolutions(String cameraId);
    Map<Integer, List<Size>> getSupportedPostviewResolutions(Size captureSize);
    List<Size> getSupportedYuvAnalysisResolutions(String cameraId);
    UnitSessionProcessorImpl createSessionProcessor();
    List<CaptureRequest.Key> getAvailableCaptureRequestKeys();
    List<CaptureResult.Key> getAvailableCaptureResultKeys();
    boolean isCaptureProcessProgressAvailable();
    boolean isPostviewAvailable();
}
