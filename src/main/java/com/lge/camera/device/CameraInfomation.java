package com.lge.camera.device;

import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraCharacteristics;

public class CameraInfomation {
    public static final int CAMERA_FACING_BACK = 1;
    public static final int CAMERA_FACING_EXTANAL = 2;
    public static final int CAMERA_FACING_FRONT = 0;
    private final int mFacing;
    private final int mOrientation;

    private CameraInfomation(int facing, int orientation) {
        this.mFacing = facing;
        this.mOrientation = orientation;
    }

    public int getCameraFacing() {
        return this.mFacing;
    }

    public int getCameraOrientation() {
        return this.mOrientation;
    }

    public static CameraInfomation createCameraInfo(CameraInfo info) {
        int convertFacing = 0;
        if (info == null) {
            return new CameraInfomation(1, 0);
        }
        if (info.facing != 1) {
            convertFacing = 1;
        }
        return new CameraInfomation(convertFacing, info.orientation);
    }

    public static CameraInfomation createCameraInfo(CameraCharacteristics info) {
        if (info == null) {
            return new CameraInfomation(1, 0);
        }
        return new CameraInfomation(((Integer) info.get(CameraCharacteristics.LENS_FACING)).intValue(), ((Integer) info.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue());
    }
}
