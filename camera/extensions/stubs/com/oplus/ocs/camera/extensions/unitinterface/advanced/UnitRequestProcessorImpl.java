package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import java.util.List;
import java.util.Map;

public interface UnitRequestProcessorImpl {
    void setImageProcessor(int outputConfigId, UnitImageProcessorImpl imageProcessor);
    int submit(Request request, Callback callback);
    int submit(List<Request> requests, Callback callback);
    int setRepeating(Request request, Callback callback);
    void abortCaptures();
    void stopRepeating();

    interface Request {
        List<Integer> getTargetOutputConfigIds();
        Map<CaptureRequest.Key<?>, Object> getParameters();
        Integer getTemplateId();
    }

    interface Callback {
        void onCaptureStarted(Request request, long frameNumber, long timestamp);
        void onCaptureProgressed(Request request, CaptureResult partialResult);
        void onCaptureCompleted(Request request, TotalCaptureResult totalCaptureResult);
        void onCaptureFailed(Request request, CaptureFailure captureFailure);
        void onCaptureBufferLost(Request request, long frameNumber, int outputStreamId);
        void onCaptureSequenceCompleted(int sequenceId, long frameNumber);
        void onCaptureSequenceAborted(int sequenceId);
    }
}
