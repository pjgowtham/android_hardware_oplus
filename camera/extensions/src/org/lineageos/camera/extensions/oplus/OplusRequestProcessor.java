/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.camera.extensions.oplus;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;

import androidx.camera.extensions.impl.advanced.ImageReferenceImpl;
import androidx.camera.extensions.impl.advanced.RequestProcessorImpl;

import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitImageProcessorImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitImageReferenceImpl;
import com.oplus.ocs.camera.extensions.unitinterface.advanced.UnitRequestProcessorImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lets the Oplus session processor drive the camera through the androidx request processor. */
class OplusRequestProcessor implements UnitRequestProcessorImpl {
    private final RequestProcessorImpl mProcessor;
    private final boolean mForceFastNoiseReduction;

    OplusRequestProcessor(RequestProcessorImpl processor, int mode) {
        mProcessor = processor;
        // The portrait still request makes the HAL run its multi-frame noise reduction, whose
        // MfsrPrefilter node fails on the extension's YUV stream. APS processes the frame anyway.
        mForceFastNoiseReduction = mode == OplusExtensions.MODE_BOKEH;
    }

    @Override
    public void setImageProcessor(int outputConfigId, UnitImageProcessorImpl imageProcessor) {
        mProcessor.setImageProcessor(outputConfigId,
                (id, timestampNs, imageReference, physicalCameraId) ->
                        imageProcessor.onNextImageAvailable(id, timestampNs,
                                wrap(imageReference), physicalCameraId));
    }

    @Override
    public int submit(Request request, Callback callback) {
        return mProcessor.submit(wrap(request), new CallbackWrapper(callback));
    }

    @Override
    public int submit(List<Request> requests, Callback callback) {
        List<RequestProcessorImpl.Request> wrapped = new ArrayList<>();
        for (Request request : requests) {
            wrapped.add(wrap(request));
        }
        return mProcessor.submit(wrapped, new CallbackWrapper(callback));
    }

    @Override
    public int setRepeating(Request request, Callback callback) {
        return mProcessor.setRepeating(wrap(request), new CallbackWrapper(callback));
    }

    @Override
    public void abortCaptures() {
        mProcessor.abortCaptures();
    }

    @Override
    public void stopRepeating() {
        mProcessor.stopRepeating();
    }

    private RequestWrapper wrap(Request request) {
        return new RequestWrapper(request, mForceFastNoiseReduction);
    }

    private static UnitImageReferenceImpl wrap(ImageReferenceImpl reference) {
        return new UnitImageReferenceImpl() {
            @Override
            public boolean increment() {
                return reference.increment();
            }

            @Override
            public boolean decrement() {
                return reference.decrement();
            }

            @Override
            public Image get() {
                return reference.get();
            }
        };
    }

    private static class RequestWrapper implements RequestProcessorImpl.Request {
        final Request mUnit;
        private final boolean mForceFastNoiseReduction;

        RequestWrapper(Request unit, boolean forceFastNoiseReduction) {
            mUnit = unit;
            mForceFastNoiseReduction = forceFastNoiseReduction;
        }

        @Override
        public List<Integer> getTargetOutputConfigIds() {
            return mUnit.getTargetOutputConfigIds();
        }

        @Override
        public Map<CaptureRequest.Key<?>, Object> getParameters() {
            Map<CaptureRequest.Key<?>, Object> params = Keys.filterRequest(mUnit.getParameters());
            if (mForceFastNoiseReduction
                    && Integer.valueOf(CameraDevice.TEMPLATE_STILL_CAPTURE).equals(getTemplateId())) {
                params.put(CaptureRequest.NOISE_REDUCTION_MODE,
                        CaptureRequest.NOISE_REDUCTION_MODE_FAST);
            }
            return params;
        }

        @Override
        public Integer getTemplateId() {
            return mUnit.getTemplateId();
        }
    }

    private static class CallbackWrapper implements RequestProcessorImpl.Callback {
        private final Callback mUnit;

        CallbackWrapper(Callback unit) {
            mUnit = unit;
        }

        private static Request unwrap(RequestProcessorImpl.Request request) {
            return request instanceof RequestWrapper ? ((RequestWrapper) request).mUnit : null;
        }

        @Override
        public void onCaptureStarted(RequestProcessorImpl.Request request, long frameNumber,
                long timestamp) {
            mUnit.onCaptureStarted(unwrap(request), frameNumber, timestamp);
        }

        @Override
        public void onCaptureProgressed(RequestProcessorImpl.Request request,
                CaptureResult partialResult) {
            mUnit.onCaptureProgressed(unwrap(request), partialResult);
        }

        @Override
        public void onCaptureCompleted(RequestProcessorImpl.Request request,
                TotalCaptureResult totalCaptureResult) {
            mUnit.onCaptureCompleted(unwrap(request), totalCaptureResult);
        }

        @Override
        public void onCaptureFailed(RequestProcessorImpl.Request request,
                CaptureFailure captureFailure) {
            mUnit.onCaptureFailed(unwrap(request), captureFailure);
        }

        @Override
        public void onCaptureBufferLost(RequestProcessorImpl.Request request, long frameNumber,
                int outputStreamId) {
            mUnit.onCaptureBufferLost(unwrap(request), frameNumber, outputStreamId);
        }

        @Override
        public void onCaptureSequenceCompleted(int sequenceId, long frameNumber) {
            mUnit.onCaptureSequenceCompleted(sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(int sequenceId) {
            mUnit.onCaptureSequenceAborted(sequenceId);
        }
    }
}
