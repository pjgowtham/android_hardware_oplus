package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import android.hardware.camera2.CaptureRequest;

import java.util.List;
import java.util.Map;

public interface UnitCamera2SessionConfigImpl {
    List<UnitCamera2OutputConfigImpl> getOutputConfigs();
    Map<CaptureRequest.Key<?>, Object> getSessionParameters();
    int getSessionTemplateId();
    int getSessionType();
}
