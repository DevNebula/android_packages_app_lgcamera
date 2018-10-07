package com.lge.ellievision;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;

public class MotionHandShakeReset {
    private static final int SHAKE_SKIP_TIME = 3000;
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.5f;
    private MotionHandShakeResetCallback mMotionHandShakeResetCallback = null;
    private MotionHandShakeResetListener mMotionHandShakeResetListener = null;
    private SensorManager mSensorManager = null;
    private long mShakeTime = 0;

    public interface MotionHandShakeResetCallback {
        void onMotionHandShakeResetCallback();
    }

    private class MotionHandShakeResetListener implements SensorEventListener {
        private MotionHandShakeResetListener() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1 && ((float) Math.sqrt((double) (((event.values[0] * event.values[0]) + (event.values[1] * event.values[1])) + (event.values[2] * event.values[2])))) / 9.80665f > MotionHandShakeReset.SHAKE_THRESHOLD_GRAVITY) {
                long currentTime = System.currentTimeMillis();
                if (MotionHandShakeReset.this.mShakeTime + CameraConstants.TOAST_LENGTH_MIDDLE_SHORT <= currentTime) {
                    MotionHandShakeReset.this.mShakeTime = currentTime;
                    if (MotionHandShakeReset.this.mMotionHandShakeResetCallback != null) {
                        MotionHandShakeReset.this.mMotionHandShakeResetCallback.onMotionHandShakeResetCallback();
                    }
                }
            }
        }
    }

    public MotionHandShakeReset(Context context, MotionHandShakeResetCallback motionHandShakeResetCallback) {
        this.mSensorManager = (SensorManager) context.getSystemService(ParamConstants.VALUE_BINNING_SENSOR);
        this.mMotionHandShakeResetListener = new MotionHandShakeResetListener();
        this.mMotionHandShakeResetCallback = motionHandShakeResetCallback;
    }

    public void onResume() {
        if (this.mSensorManager != null) {
            this.mSensorManager.registerListener(this.mMotionHandShakeResetListener, this.mSensorManager.getDefaultSensor(1), 3);
        }
    }

    public void onPause() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this.mMotionHandShakeResetListener);
        }
    }

    public void unbind() {
        this.mSensorManager = null;
        this.mMotionHandShakeResetListener = null;
        this.mMotionHandShakeResetCallback = null;
    }
}
