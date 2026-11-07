package com.oplus.ocs.camera.extensions.unitinterface.advanced;

public interface UnitOutputSurfaceConfigurationImpl {
    UnitOutputSurfaceImpl getPreviewOutputSurface();
    UnitOutputSurfaceImpl getImageCaptureOutputSurface();
    UnitOutputSurfaceImpl getImageAnalysisOutputSurface();
    UnitOutputSurfaceImpl getPostviewOutputSurface();
}
