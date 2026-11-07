package com.oplus.ocs.camera.extensions.unitinterface.advanced;

public interface UnitImageProcessorImpl {
    void onNextImageAvailable(int outputConfigId, long timestampNs,
            UnitImageReferenceImpl imageReference, String physicalCameraId);
}
