package com.lge.camera.device;

import android.hardware.Camera;
import com.lge.hardware.LGCamera.CameraMetaDataCallback;

public abstract class LGCameraMetaDataCallback implements CameraMetaDataCallback {
    public abstract void onCameraMetaData(byte[] bArr);

    public void onCameraMetaData(byte[] data, Camera camera) {
        onCameraMetaData(data);
    }
}
