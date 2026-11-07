package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import java.util.List;

public interface UnitCamera2OutputConfigImpl {
    int getId();
    int getSurfaceGroupId();
    String getPhysicalCameraId();
    List<UnitCamera2OutputConfigImpl> getSurfaceSharingOutputConfigs();
}
