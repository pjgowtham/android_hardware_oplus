package com.oplus.ocs.camera.extensions.unitinterface.advanced;

import android.media.Image;

public interface UnitImageReferenceImpl {
    boolean increment();
    boolean decrement();
    Image get();
}
