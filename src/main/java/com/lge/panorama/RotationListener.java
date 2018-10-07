package com.lge.panorama;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;

public class RotationListener implements SensorEventListener {
    private static final double NS2S = 9.999999717180685E-10d;
    private static final int SENSOR_TYPE = 15;
    private double RAD2DGR = 57.29577951308232d;
    private long mFirstSensorTimestampOffset = -1;
    private long mFirstSysTimestampOffset = -1;
    public PanoramaRotaionSensorListener mListener = null;
    private double mPitch = 0.0d;
    private double mPrevTimeStamp = 0.0d;
    private final float[] mQuaternion = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
    private boolean mRegistered = false;
    private double mRoll = 0.0d;
    private final float[] mRotationMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private Sensor mSensor = null;
    private SensorManager mSensorManager = null;
    private double mYaw = 0.0d;

    public interface PanoramaRotaionSensorListener {
        void feedInertialSensorData(float[] fArr, long j);
    }

    public RotationListener(Context context) {
        this.mSensorManager = (SensorManager) context.getSystemService(ParamConstants.VALUE_BINNING_SENSOR);
        this.mSensor = this.mSensorManager.getDefaultSensor(15);
    }

    public void setListener(PanoramaRotaionSensorListener listener) {
        this.mListener = listener;
    }

    public void register() {
        if (!this.mRegistered && this.mSensorManager != null && this.mSensor != null) {
            this.mSensorManager.registerListener(this, this.mSensor, 1);
            this.mRegistered = true;
        }
    }

    public void resetTimestamps() {
        this.mFirstSysTimestampOffset = -1;
        this.mFirstSensorTimestampOffset = -1;
        this.mPitch = 0.0d;
        this.mRoll = 0.0d;
        this.mYaw = 0.0d;
        this.mPrevTimeStamp = 0.0d;
    }

    public void unregister() {
        if (this.mRegistered && this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this);
            this.mRegistered = false;
        }
    }

    public void unbind() {
        unregister();
        this.mSensorManager = null;
        this.mSensor = null;
        this.mListener = null;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event == null) {
            Log.w(CameraConstants.TAG, "event is null.");
            return;
        }
        int sensorType = event.sensor.getType();
        if (sensorType == 15 || sensorType == 11) {
            synchronized (this) {
                SensorManager.getRotationMatrixFromVector(this.mRotationMatrix, event.values);
                SensorManager.getQuaternionFromVector(this.mQuaternion, event.values);
                if (this.mListener != null) {
                    if (this.mFirstSysTimestampOffset == -1) {
                        this.mFirstSysTimestampOffset = System.currentTimeMillis();
                        this.mFirstSensorTimestampOffset = event.timestamp;
                    }
                    this.mListener.feedInertialSensorData(this.mQuaternion, Long.valueOf(this.mFirstSysTimestampOffset + ((event.timestamp - this.mFirstSensorTimestampOffset) / 1000000)).longValue());
                }
            }
        } else if (sensorType == 4 || sensorType == 16) {
            synchronized (this) {
                if (this.mListener != null) {
                    if (this.mPrevTimeStamp == 0.0d) {
                        this.mPrevTimeStamp = (double) event.timestamp;
                    } else {
                        double dt = (((double) event.timestamp) - this.mPrevTimeStamp) * NS2S;
                        this.mPrevTimeStamp = (double) event.timestamp;
                        this.mPitch += ((double) event.values[1]) * dt;
                        this.mRoll += ((double) event.values[0]) * dt;
                        this.mYaw += ((double) event.values[2]) * dt;
                        double cy = Math.cos(this.mYaw * 0.5d);
                        double sy = Math.sin(this.mYaw * 0.5d);
                        double cr = Math.cos(this.mRoll * 0.5d);
                        double sr = Math.sin(this.mRoll * 0.5d);
                        double cp = Math.cos(this.mPitch * 0.5d);
                        double sp = Math.sin(this.mPitch * 0.5d);
                        this.mQuaternion[0] = (float) (((cy * cr) * cp) + ((sy * sr) * sp));
                        this.mQuaternion[1] = (float) (((cy * sr) * cp) - ((sy * cr) * sp));
                        this.mQuaternion[2] = (float) (((cy * cr) * sp) + ((sy * sr) * cp));
                        this.mQuaternion[3] = (float) (((sy * cr) * cp) - ((cy * sr) * sp));
                        if (this.mFirstSysTimestampOffset == -1) {
                            this.mFirstSysTimestampOffset = System.currentTimeMillis();
                            this.mFirstSensorTimestampOffset = event.timestamp;
                        }
                        this.mListener.feedInertialSensorData(this.mQuaternion, Long.valueOf(this.mFirstSysTimestampOffset + ((event.timestamp - this.mFirstSensorTimestampOffset) / 1000000)).longValue());
                    }
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void getRotationMatrix(float[] m) {
        synchronized (this) {
            for (int i = 0; i < this.mRotationMatrix.length; i++) {
                m[i] = this.mRotationMatrix[i];
            }
        }
    }

    public void getQuaternion(float[] m) {
        synchronized (this) {
            for (int i = 0; i < this.mQuaternion.length; i++) {
                m[i] = this.mQuaternion[i];
            }
        }
    }
}
