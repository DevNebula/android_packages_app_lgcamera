package com.lge.shutterlessshot.library;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.lang.reflect.Array;

public class TiltDetector {
    private static final int ACCEL_BUFFER_SIZE = 10;
    private static final int ACCEL_SAMPLING_PERIOD_US = 40000;
    private static float DEVICE_MAX_HORIZONTAL_ANGLE = 30.0f;
    private static float DEVICE_TILT_IN_ANGLE = 60.0f;
    private static float DEVICE_TILT_MAX_ANGLE = 90.0f;
    private static float DEVICE_TILT_MIN_ANGLE = 15.0f;
    private static float DEVICE_TILT_OUT_ANGLE = 45.0f;
    public static boolean ENABLE_LOG = false;
    private static final int MIN_SENSOR_DURATION_NS = 33000000;
    private static final int STATE_TILT_IN = 1;
    private static final int STATE_TILT_OUT = 2;
    private static final int STATE_TILT_UNKNOWN = 0;
    public static final String TAG = "TiltDetector";
    public static String VERSION = "1.4.6";
    private final float[][] mAccelData = ((float[][]) Array.newInstance(Float.TYPE, new int[]{10, 3}));
    private final float[] mAccelMean = new float[3];
    private Sensor mAccelSensor;
    private long mLastAccelSensorTimestamp;
    private final SensorEventListener mSensorEventListener = new C00301();
    private SensorManager mSensorManager;
    private boolean mStart;
    private int mState;
    private float mTiltAngle;

    /* renamed from: com.lge.shutterlessshot.library.TiltDetector$1 */
    class C00301 implements SensorEventListener {
        C00301() {
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (TiltDetector.this.mStart && event.timestamp - TiltDetector.this.mLastAccelSensorTimestamp >= 33000000) {
                int i;
                float[] access$4;
                TiltDetector.this.mLastAccelSensorTimestamp = event.timestamp;
                float ax = event.values[0];
                float ay = event.values[1];
                float az = event.values[2];
                int length = TiltDetector.this.mAccelData.length;
                for (i = length - 1; i > 0; i--) {
                    TiltDetector.this.mAccelData[i][0] = TiltDetector.this.mAccelData[i - 1][0];
                    TiltDetector.this.mAccelData[i][1] = TiltDetector.this.mAccelData[i - 1][1];
                    TiltDetector.this.mAccelData[i][2] = TiltDetector.this.mAccelData[i - 1][2];
                }
                TiltDetector.this.mAccelData[0][0] = ax;
                TiltDetector.this.mAccelData[0][1] = ay;
                TiltDetector.this.mAccelData[0][2] = az;
                TiltDetector.this.mAccelMean[0] = 0.0f;
                TiltDetector.this.mAccelMean[1] = 0.0f;
                TiltDetector.this.mAccelMean[2] = 0.0f;
                for (i = 0; i < length; i++) {
                    access$4 = TiltDetector.this.mAccelMean;
                    access$4[0] = access$4[0] + TiltDetector.this.mAccelData[i][0];
                    access$4 = TiltDetector.this.mAccelMean;
                    access$4[1] = access$4[1] + TiltDetector.this.mAccelData[i][1];
                    access$4 = TiltDetector.this.mAccelMean;
                    access$4[2] = access$4[2] + TiltDetector.this.mAccelData[i][2];
                }
                access$4 = TiltDetector.this.mAccelMean;
                access$4[0] = access$4[0] / ((float) length);
                access$4 = TiltDetector.this.mAccelMean;
                access$4[1] = access$4[1] / ((float) length);
                access$4 = TiltDetector.this.mAccelMean;
                access$4[2] = access$4[2] / ((float) length);
                float planeAccel = (TiltDetector.this.mAccelMean[0] * TiltDetector.this.mAccelMean[0]) + (TiltDetector.this.mAccelMean[1] * TiltDetector.this.mAccelMean[1]);
                TiltDetector.this.mTiltAngle = (float) TiltDetector.this.arcSinApprox((float) Math.sqrt((double) (planeAccel / (planeAccel + (az * az)))));
                if (TiltDetector.this.mAccelMean[2] < 0.0f) {
                    TiltDetector.this.mTiltAngle = 180.0f - TiltDetector.this.mTiltAngle;
                }
                TiltDetector.this.evaluateTiltState();
            }
        }
    }

    public TiltDetector(SensorManager manager) {
        this.mSensorManager = manager;
        this.mAccelSensor = this.mSensorManager.getDefaultSensor(1, true);
        this.mAccelSensor = this.mAccelSensor == null ? this.mSensorManager.getDefaultSensor(1) : this.mAccelSensor;
        this.mState = 0;
        this.mTiltAngle = 0.0f;
        this.mStart = false;
    }

    public void start() {
        if (!this.mStart) {
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mAccelSensor, ACCEL_SAMPLING_PERIOD_US, ACCEL_SAMPLING_PERIOD_US);
            this.mStart = true;
            this.mTiltAngle = 0.0f;
            this.mState = 2;
            for (int i = 0; i < 10; i++) {
                this.mAccelData[i][0] = 0.0f;
                this.mAccelData[i][1] = 0.0f;
                this.mAccelData[i][2] = 0.0f;
            }
        }
    }

    public void stop() {
        if (this.mStart) {
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mStart = false;
        }
    }

    public boolean isTilting() {
        return this.mState == 1;
    }

    public boolean isHorizontal() {
        return this.mTiltAngle < DEVICE_MAX_HORIZONTAL_ANGLE;
    }

    public void setTiltAngle(float angle) {
        float angleLimited = Math.min(Math.max(DEVICE_TILT_MIN_ANGLE, angle), DEVICE_TILT_MAX_ANGLE);
        DEVICE_TILT_IN_ANGLE = angleLimited;
        DEVICE_TILT_OUT_ANGLE = angleLimited - 15.0f;
        evaluateTiltState();
    }

    private void evaluateTiltState() {
        if (this.mState == 1) {
            if (this.mTiltAngle < DEVICE_TILT_OUT_ANGLE) {
                this.mState = 2;
            }
        } else if (this.mState == 2 && this.mTiltAngle >= DEVICE_TILT_IN_ANGLE) {
            this.mState = 1;
        }
    }

    private int arcSinApprox(float sinValue) {
        float sin = sinValue;
        float[] arcSinTable = new float[]{0.0f, 0.017452f, 0.034899f, 0.052336f, 0.069756f, 0.087156f, 0.104528f, 0.121869f, 0.139173f, 0.156434f, 0.173648f, 0.190809f, 0.207912f, 0.224951f, 0.241922f, 0.258819f, 0.275637f, 0.292372f, 0.309017f, 0.325568f, 0.34202f, 0.358368f, 0.374607f, 0.390731f, 0.406737f, 0.422618f, 0.438371f, 0.45399f, 0.469472f, 0.48481f, 0.5f, 0.515038f, 0.529919f, 0.544639f, 0.559193f, 0.573576f, 0.587785f, 0.601815f, 0.615661f, 0.62932f, 0.642788f, 0.656059f, 0.669131f, 0.681998f, 0.694658f, 0.707107f, 0.71934f, 0.731354f, 0.743145f, 0.75471f, 0.766044f, 0.777146f, 0.788011f, 0.798636f, 0.809017f, 0.819152f, 0.829038f, 0.838671f, 0.848048f, 0.857167f, 0.866025f, 0.87462f, 0.882948f, 0.891007f, 0.898794f, 0.906308f, 0.913545f, 0.920505f, 0.927184f, 0.93358f, 0.939693f, 0.945519f, 0.951057f, 0.956305f, 0.961262f, 0.965926f, 0.970296f, 0.97437f, 0.978148f, 0.981627f, 0.984808f, 0.987688f, 0.990268f, 0.992546f, 0.994522f, 0.996195f, 0.997564f, 0.99863f, 0.999391f, 0.999848f, 1.0f};
        int length = arcSinTable.length - 1;
        int i = 0;
        while (i < length) {
            if (arcSinTable[i] <= sin && sin < arcSinTable[i + 1]) {
                return i;
            }
            i++;
        }
        return 90;
    }

    private void printLog(String msg) {
        if (ENABLE_LOG) {
            Log.v(TAG, msg);
        }
    }
}
