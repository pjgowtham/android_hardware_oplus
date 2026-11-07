package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import android.util.Size;
import android.view.Surface;

public interface UnitOutputSurfaceImpl {
    Surface getSurface();
    Size getSize();
    int getImageFormat();
}
