package com.lge.camera.device.api2;

import android.hardware.camera2.DngCreator;
import android.util.Size;
import java.nio.ByteBuffer;

public interface CaptureListener {
    boolean isValidRawPictureCallback();

    void onCaptureAvailable(DngCreator dngCreator, ByteBuffer byteBuffer, Size size);

    void onCaptureAvailable(byte[] bArr, byte[] bArr2);
}
