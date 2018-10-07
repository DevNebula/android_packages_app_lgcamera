package com.lge.shutterlessshot.library;

import android.graphics.Rect;
import android.hardware.Camera.Face;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import java.lang.reflect.Array;

public class ShutterlessEngine {
    private static final int ACCEL_BUFFER_SIZE = 30;
    private static final int ACCEL_DATA_SIZE = 30;
    private static final float ACCEL_LSR_SLOP_MOTION_OUT = 0.12f;
    private static final float ACCEL_LSR_SLOP_MOTION_STOP_IN = 0.02f;
    private static final float ACCEL_LSR_SLOP_MOTION_STOP_OUT = 0.06f;
    private static final float ACCEL_LSR_STDEV_MOTION_OUT = 8.0f;
    private static final float ACCEL_LSR_STDEV_MOTION_STOP_IN = 2.0f;
    private static final float ACCEL_LSR_STDEV_MOTION_STOP_OUT = 4.0f;
    private static final int ACCEL_ONLY_DATA_SIZE = 30;
    private static final int ACCEL_ONLY_SAMPLING_PERIOD_US = 40000;
    private static final int ACCEL_SAMPLING_PERIOD_US = 20000;
    private static final int ACCEL_STDEV_BUFFER_SIZE = 10;
    private static final float ACCEL_STDEV_MOTION_OUT = 11.0f;
    private static final float ACCEL_STDEV_MOTION_STOP_IN = 5.0f;
    private static final float ACCEL_STDEV_MOTION_STOP_OUT = 5.5f;
    public static int AFTER_SHUTTER_HOLD_TIME = 600;
    private static final int AXIS_NUM = 3;
    private static final boolean ENABLE_CANCEL_MOTION_STOP_BEFORE_TAKE_PICTURE = true;
    public static boolean ENABLE_LOG = false;
    private static final boolean ENABLE_MOTION_OUT_BEFORE_TAKE_PICTURE = true;
    private static final float FACE_HOLD_IN = 72900.0f;
    private static final int FACE_HOLD_TIME = 0;
    private static final float FACE_HOLD_UNCERTAIN = 108900.0f;
    private static final int FACE_STABLE_NUM = 2;
    private static final int GYRO_BUFFER_SIZE = 30;
    private static final float GYRO_MOTION_OUT = 8.0f;
    private static final float GYRO_MOTION_STOP_IN = 5.0f;
    private static final float GYRO_MOTION_STOP_OUT = 6.0f;
    private static final int GYRO_SAMPLING_PERIOD_US = 5000;
    private static final int INIT_FACE_HOLD_TIME = 0;
    public static int INIT_HOLD_TIME = 1500;
    private static final int INIT_MOTION_HOLD_IN_TIME = 50;
    private static final int MAX_FACE_NUM = 5;
    private static final int MIN_ACCEL_ONLY_SENSOR_DURATION_NS = 32000000;
    private static final int MIN_SENSOR_DURATION_NS = 16000000;
    private static final int MOTION_HOLD_IN_TIME = 50;
    private static final int MOTION_HOLD_OUT_TIME = 70;
    public static int MOTION_OUT_HOLD_TIME = 200;
    private static final int MSG_EVALUATE_FACE_DATA = 1;
    private static final int MSG_EVALUATE_SENSOR_DATA = 0;
    private static final int MSG_EVALUATE_SHUTTERLESS_STATUS = 2;
    private static final int ORIENTATION_LANDSCAPE = 1;
    private static final int ORIENTATION_PORTRAIT = 0;
    private static final int ORIENTATION_VERY_SMALL_TILT = 2;
    private static final float RV_ANGLE_HOLD_OUT = 30.0f;
    private static final float RV_ANGLE_HOLD_OUT_LAND = 30.0f;
    private static final int RV_SAMPLING_PERIOD_US = 20000;
    public static final int SENSOR_MODE_ACCELEROMETER_AND_GYROSCOPE = 3;
    public static final int SENSOR_MODE_ACCELEROMETER_ONLY = 1;
    public static final int SENSOR_MODE_AUTO = 0;
    private static final int SENSOR_MODE_NO_SENSORS = 4;
    private static final int SENSOR_REPORT_LATENCY_US = 20000;
    public static final int STATUS_CANCEL_TIMER_AND_IN_MOTION = 3;
    public static int STATUS_HOLD_TIME = 400;
    public static final int STATUS_INIT_AND_IN_MOTION = 1;
    public static final int STATUS_IN_MOTION_AFTER_SHUTTER = 4;
    public static final int STATUS_MOTION_STOP_FOR_INSTANT_SHUTTER = 5;
    public static final int STATUS_MOTION_STOP_FOR_TIMER_SHUTTER = 2;
    public static final int STATUS_PAUSE_SHUTTER_UNTIL_RESET_CONDITION = 6;
    public static final int STATUS_REST = 0;
    public static final String TAG = "ShutterlessEngine";
    private static final int TILT_ANGLE_HOLD_OUT = 30;
    private static final int TILT_ANGLE_HOLD_OUT_LAND = 30;
    public static String VERSION = "1.4.6";
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;
    private static final int Z_AXIS = 2;
    private int mAccelDataSize;
    private final float[][] mAccelDiffValue = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 30}));
    private final float[] mAccelMean = new float[3];
    private boolean mAccelMotionStop;
    private Sensor mAccelSensor;
    private SensorEventListener mAccelSensorEventListener;
    private final float[] mAccelStdev = new float[3];
    private final float[] mAccelStdevSum = new float[10];
    private final float[] mAccelSum = new float[3];
    private final float[][] mAccelValue = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 30}));
    private final float[] mBaseRotVecAngle = new float[3];
    private int mBaseTiltAngle;
    private int mDegree;
    private long mEngineStartTime;
    private int mFaceCount;
    private int mFaceCountCandidate;
    private long mFaceCountCandidateTime;
    private int mFaceCountPrev;
    private float mFaceCx;
    private float mFaceCy;
    private final Face[] mFaceData = new Face[5];
    private int mFaceDataCount;
    private boolean mFaceHeld;
    private boolean mFaceHeldOnPreview;
    private int mFaceHoldCount;
    private boolean mGyroMotionStop;
    private Sensor mGyroSensor;
    private SensorEventListener mGyroSensorEventListener;
    private final float[] mGyroSum = new float[3];
    private final float[][] mGyroValue = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 30}));
    private Handler mHandler;
    private boolean mIsFaceFirst;
    private boolean mIsResetFirst;
    private boolean mIsSensorFirst;
    private final LeastSquareResult mLSResult = new LeastSquareResult(this, null);
    private long mLastAccelSensorTimestamp;
    private StatusListener mListener;
    private boolean mLocked;
    private long mMotionCandidateTime;
    private boolean mMotionOut;
    private boolean mMotionStop;
    private boolean mMotionStopCandidate;
    private int mOrientation;
    private boolean mPaused;
    private final float[] mRotVecAngle = new float[3];
    private Sensor mRotVecSensor;
    private SensorEventListener mRotVecSensorEventListener;
    private boolean mRunning;
    private SensorManager mSensorManager;
    private int mSensorMode;
    private int mSensorModeReq;
    private int mStatus;
    private StatusChangedRunnable mStatusChangedRunnable;
    private boolean mStatusLocked;
    private long mStatusTime;
    private boolean mTakePictureAfter;
    private int mTiltAngle;

    /* renamed from: com.lge.shutterlessshot.library.ShutterlessEngine$1 */
    class C00261 extends Handler {
        C00261() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (ShutterlessEngine.this.mRunning) {
                long currTime = SystemClock.uptimeMillis();
                switch (msg.what) {
                    case 0:
                        ShutterlessEngine.this.evaluateSensorData(currTime);
                        return;
                    case 1:
                        ShutterlessEngine.this.evaluateFaceData(currTime);
                        return;
                    case 2:
                        ShutterlessEngine.this.evaluateShutterlessStatus(currTime);
                        return;
                    default:
                        return;
                }
            }
            Log.i(ShutterlessEngine.TAG, "handleMessage canceled by engine stop");
        }
    }

    /* renamed from: com.lge.shutterlessshot.library.ShutterlessEngine$2 */
    class C00272 implements SensorEventListener {
        C00272() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (ShutterlessEngine.this.mRunning) {
                long currTimeNs = event.timestamp;
                if (currTimeNs - ShutterlessEngine.this.mLastAccelSensorTimestamp >= ((long) (ShutterlessEngine.this.mSensorMode == 1 ? ShutterlessEngine.MIN_ACCEL_ONLY_SENSOR_DURATION_NS : ShutterlessEngine.MIN_SENSOR_DURATION_NS))) {
                    ShutterlessEngine.this.mLastAccelSensorTimestamp = currTimeNs;
                    for (int i = 0; i < 3; i++) {
                        float[] accel = ShutterlessEngine.this.mAccelValue[i];
                        float[] accelDiff = ShutterlessEngine.this.mAccelDiffValue[i];
                        float accelValue = event.values[i];
                        int accelDataSize = ShutterlessEngine.this.mAccelDataSize;
                        float[] access$16 = ShutterlessEngine.this.mAccelSum;
                        access$16[i] = access$16[i] + (accelValue - accel[0]);
                        ShutterlessEngine.this.mAccelMean[i] = ShutterlessEngine.this.mAccelSum[i] / ((float) accelDataSize);
                        ShutterlessEngine.this.enqueue(accelDiff, accelValue - accel[accelDataSize - 1], accelDataSize);
                        ShutterlessEngine.this.enqueue(accel, accelValue, accelDataSize);
                        ShutterlessEngine.this.mAccelStdev[i] = ShutterlessEngine.this.computeStdev(accel, ShutterlessEngine.this.mAccelMean[i], accelDataSize);
                    }
                    ShutterlessEngine.this.mHandler.sendEmptyMessage(0);
                    ShutterlessEngine.this.mHandler.sendEmptyMessage(2);
                    return;
                }
                return;
            }
            Log.i(ShutterlessEngine.TAG, "accel onSensorChanged canceled by engine stop");
        }
    }

    /* renamed from: com.lge.shutterlessshot.library.ShutterlessEngine$3 */
    class C00283 implements SensorEventListener {
        C00283() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (ShutterlessEngine.this.mRunning) {
                for (int i = 0; i < 3; i++) {
                    float[] gyro = ShutterlessEngine.this.mGyroValue[i];
                    float gyroValue = event.values[i];
                    float[] access$23 = ShutterlessEngine.this.mGyroSum;
                    access$23[i] = access$23[i] + (gyroValue - gyro[0]);
                    ShutterlessEngine.this.enqueue(gyro, gyroValue, 30);
                }
                return;
            }
            Log.i(ShutterlessEngine.TAG, "gyro onSensorChanged canceled by engine stop");
        }
    }

    /* renamed from: com.lge.shutterlessshot.library.ShutterlessEngine$4 */
    class C00294 implements SensorEventListener {
        C00294() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (ShutterlessEngine.this.mRunning) {
                for (int i = 0; i < 3; i++) {
                    ShutterlessEngine.this.mRotVecAngle[i] = event.values[i] * 180.0f;
                }
                return;
            }
            Log.i(ShutterlessEngine.TAG, "GameRV onSensorChanged canceled by engine stop");
        }
    }

    private class LeastSquareResult {
        public float slop;

        private LeastSquareResult() {
        }

        /* synthetic */ LeastSquareResult(ShutterlessEngine shutterlessEngine, LeastSquareResult leastSquareResult) {
            this();
        }
    }

    private class StatusChangedRunnable implements Runnable {
        public int newStatus;

        private StatusChangedRunnable() {
            this.newStatus = 0;
        }

        /* synthetic */ StatusChangedRunnable(ShutterlessEngine shutterlessEngine, StatusChangedRunnable statusChangedRunnable) {
            this();
        }

        public void run() {
            if (ShutterlessEngine.this.mStatus != 0) {
                Log.i(ShutterlessEngine.TAG, "status changed: " + ShutterlessEngine.this.getStatusString(ShutterlessEngine.this.mStatus) + " -> " + ShutterlessEngine.this.getStatusString(this.newStatus));
            }
            ShutterlessEngine.this.mListener.onStatusChanged(this.newStatus);
            ShutterlessEngine.this.mStatusLocked = false;
            ShutterlessEngine.this.mStatus = this.newStatus;
            ShutterlessEngine.this.mStatusTime = SystemClock.uptimeMillis();
        }
    }

    public interface StatusListener {
        void onStatusChanged(int i);
    }

    public ShutterlessEngine(SensorManager sensorManager) {
        this.mSensorManager = sensorManager;
        this.mAccelSensor = this.mSensorManager.getDefaultSensor(1, true);
        this.mGyroSensor = this.mSensorManager.getDefaultSensor(4, true);
        this.mRotVecSensor = this.mSensorManager.getDefaultSensor(15, true);
        this.mSensorModeReq = 0;
        this.mAccelSensor = this.mAccelSensor == null ? this.mSensorManager.getDefaultSensor(1) : this.mAccelSensor;
        this.mGyroSensor = this.mGyroSensor == null ? this.mSensorManager.getDefaultSensor(4) : this.mGyroSensor;
        this.mRotVecSensor = this.mRotVecSensor == null ? this.mSensorManager.getDefaultSensor(15) : this.mRotVecSensor;
        this.mStatusChangedRunnable = new StatusChangedRunnable(this, null);
        this.mHandler = new C00261();
        this.mAccelSensorEventListener = new C00272();
        this.mGyroSensorEventListener = new C00283();
        this.mRotVecSensorEventListener = new C00294();
        for (int i = 0; i < 5; i++) {
            this.mFaceData[i] = new Face();
            this.mFaceData[i].rect = new Rect();
        }
        this.mRunning = false;
    }

    public void registerListener(StatusListener listener) {
        if (this.mRunning) {
            Log.e(TAG, "cannot register listener when the engine is running");
        } else {
            this.mListener = listener;
        }
    }

    public void unregisterListener() {
        if (this.mRunning) {
            Log.e(TAG, "cannot unregister listener when the engine is running");
        } else {
            this.mListener = null;
        }
    }

    public void onFaceDetection(Face[] faces) {
        if (this.mRunning) {
            if (faces == null) {
                this.mFaceDataCount = 0;
            } else {
                int faceDataCount = Math.min(faces.length, 5);
                for (int i = 0; i < faceDataCount; i++) {
                    Rect dstRect = this.mFaceData[i].rect;
                    Rect srcRect = faces[i].rect;
                    dstRect.left = srcRect.left;
                    dstRect.top = srcRect.top;
                    dstRect.right = srcRect.right;
                    dstRect.bottom = srcRect.bottom;
                }
                this.mFaceDataCount = faceDataCount;
            }
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void setDegree(int degree) {
        printLog("degree changed: " + this.mDegree + " -> " + degree);
        this.mDegree = degree;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public boolean isLocked() {
        return this.mLocked;
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public void start(int degree) {
        if (this.mRunning) {
            Log.e(TAG, "already engine started");
            return;
        }
        setSensorMode();
        registerSensors();
        this.mRunning = true;
        this.mIsResetFirst = true;
        this.mLocked = false;
        this.mPaused = false;
        this.mStatusLocked = false;
        this.mDegree = degree;
        this.mStatus = 0;
        this.mEngineStartTime = SystemClock.uptimeMillis();
        Log.i(TAG, "start shutterless engine");
    }

    public void stop() {
        if (this.mRunning) {
            unregisterSensors();
            emptyHandler();
            this.mRunning = false;
            Log.i(TAG, "stop shutterless engine");
            return;
        }
        Log.e(TAG, "already engine stopped");
    }

    public void requestSensorMode(int mode) {
        this.mSensorModeReq = mode;
        setSensorMode();
    }

    public int getSensorMode() {
        return this.mSensorMode;
    }

    public void lock() {
        if (!this.mLocked) {
            this.mLocked = true;
            this.mStatusTime = SystemClock.uptimeMillis();
            this.mBaseTiltAngle = this.mTiltAngle;
            for (int i = 0; i < 3; i++) {
                this.mBaseRotVecAngle[i] = this.mRotVecAngle[i];
            }
            Log.i(TAG, "engine locked");
        }
    }

    public void unlock() {
        if (this.mLocked) {
            this.mLocked = false;
            this.mStatusTime = SystemClock.uptimeMillis();
            Log.i(TAG, "engine unlocked");
        }
    }

    public void pause() {
        if (!this.mPaused) {
            this.mPaused = true;
            this.mStatusTime = SystemClock.uptimeMillis();
            Log.i(TAG, "engine paused");
        }
    }

    public void unpause() {
        if (this.mPaused) {
            long currTime = SystemClock.uptimeMillis();
            this.mPaused = false;
            this.mStatusTime = currTime;
            int i;
            switch (this.mStatus) {
                case 1:
                    initAndResetInternal();
                    break;
                case 2:
                case 5:
                    this.mFaceHeldOnPreview = true;
                    this.mFaceHeld = true;
                    this.mFaceCount = this.mFaceDataCount;
                    if (this.mFaceCount != 0) {
                        this.mFaceHoldCount = 2;
                    }
                    this.mFaceCountPrev = this.mFaceDataCount;
                    this.mFaceCountCandidate = this.mFaceDataCount;
                    this.mFaceCountCandidateTime = currTime;
                    this.mAccelMotionStop = true;
                    this.mGyroMotionStop = true;
                    this.mMotionStop = true;
                    this.mMotionStopCandidate = true;
                    this.mMotionOut = false;
                    this.mBaseTiltAngle = this.mTiltAngle;
                    this.mMotionCandidateTime = currTime;
                    for (i = 0; i < 3; i++) {
                        this.mBaseRotVecAngle[i] = this.mRotVecAngle[i];
                    }
                    break;
                case 4:
                    this.mMotionOut = false;
                    this.mBaseTiltAngle = this.mTiltAngle;
                    this.mMotionCandidateTime = currTime;
                    for (i = 0; i < 3; i++) {
                        this.mBaseRotVecAngle[i] = this.mRotVecAngle[i];
                    }
                    break;
            }
            Log.i(TAG, "engine unpaused");
        }
    }

    public void onTakePicture() {
        this.mTakePictureAfter = true;
        this.mBaseTiltAngle = this.mTiltAngle;
        for (int i = 0; i < 3; i++) {
            this.mBaseRotVecAngle[i] = this.mRotVecAngle[i];
        }
        Log.i(TAG, "engine onTakePicture");
    }

    public void reset() {
        Log.i(TAG, "engine reset");
        this.mStatus = 0;
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    public void pauseShutterUntilResetCondition() {
        Log.i(TAG, "pause shutterless engine until reset condition");
        this.mStatusChangedRunnable.newStatus = 6;
        this.mStatusLocked = true;
        this.mMotionOut = false;
        this.mFaceHeldOnPreview = true;
        this.mHandler.post(this.mStatusChangedRunnable);
    }

    private void setSensorMode() {
        if (this.mAccelSensor == null) {
            this.mSensorMode = 4;
            this.mAccelDataSize = 0;
        } else if (this.mGyroSensor == null) {
            this.mSensorMode = 1;
        } else if (this.mSensorModeReq == 0) {
            this.mSensorMode = 3;
        } else {
            this.mSensorMode = this.mSensorModeReq;
        }
        switch (this.mSensorMode) {
            case 1:
                this.mAccelDataSize = 30;
                Log.i(TAG, "Shutterless Engine works with an accelerometer only (requestSensorMode: " + this.mSensorModeReq + ")");
                return;
            case 3:
                this.mAccelDataSize = 30;
                Log.i(TAG, "Shutterless Engine works with accelerometer and gyroscope (requestSensorMode: " + this.mSensorModeReq + ")");
                return;
            default:
                this.mAccelDataSize = 0;
                Log.e(TAG, "Shutterless Engine cannot work because of no sensors");
                return;
        }
    }

    private void registerSensors() {
        switch (this.mSensorMode) {
            case 1:
                this.mSensorManager.registerListener(this.mAccelSensorEventListener, this.mAccelSensor, ACCEL_ONLY_SAMPLING_PERIOD_US, ACCEL_ONLY_SAMPLING_PERIOD_US);
                return;
            case 3:
                this.mSensorManager.registerListener(this.mAccelSensorEventListener, this.mAccelSensor, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
                this.mSensorManager.registerListener(this.mGyroSensorEventListener, this.mGyroSensor, 5000, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
                this.mSensorManager.registerListener(this.mRotVecSensorEventListener, this.mRotVecSensor, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
                return;
            default:
                return;
        }
    }

    private void unregisterSensors() {
        switch (this.mSensorMode) {
            case 1:
                this.mSensorManager.unregisterListener(this.mAccelSensorEventListener);
                return;
            case 3:
                this.mSensorManager.unregisterListener(this.mAccelSensorEventListener);
                this.mSensorManager.unregisterListener(this.mGyroSensorEventListener);
                this.mSensorManager.unregisterListener(this.mRotVecSensorEventListener);
                return;
            default:
                return;
        }
    }

    private void evaluateShutterlessStatus(long time) {
        long currTime = time;
        if (!this.mStatusLocked && !this.mPaused) {
            int newStatus = this.mStatus;
            int status = this.mStatus;
            int statusElapsedTime = (int) (currTime - this.mStatusTime);
            boolean motionStopDetected = this.mMotionStop && this.mFaceHeld;
            boolean statusHoldTimePassed = statusElapsedTime > STATUS_HOLD_TIME;
            switch (status) {
                case 0:
                    Log.i(TAG, "Shutterless Engine processes STATUS_REST");
                    newStatus = 1;
                    initAndResetInternal();
                    this.mHandler.sendEmptyMessage(0);
                    this.mHandler.sendEmptyMessage(1);
                    break;
                case 1:
                case 3:
                case 4:
                    boolean initHoldTimePassed = currTime - this.mEngineStartTime > ((long) INIT_HOLD_TIME);
                    if (!this.mLocked && motionStopDetected) {
                        if (status != 1 && status != 3) {
                            if (status != 5) {
                                newStatus = 5;
                                this.mMotionOut = false;
                                this.mTakePictureAfter = false;
                                this.mFaceHeldOnPreview = true;
                                this.mMotionOut = false;
                                break;
                            }
                        } else if (initHoldTimePassed && statusHoldTimePassed) {
                            newStatus = 5;
                            this.mTakePictureAfter = false;
                            this.mFaceHeldOnPreview = true;
                            this.mMotionOut = false;
                            break;
                        }
                    }
                    break;
                case 2:
                case 5:
                    boolean afterShutterHoldTimePassed = statusElapsedTime > AFTER_SHUTTER_HOLD_TIME;
                    if (this.mLocked) {
                        if (!this.mTakePictureAfter) {
                            if (!this.mFaceHeldOnPreview) {
                                Log.i(TAG, "motion stop canceled because of no face");
                                newStatus = 4;
                                resetFaceCount();
                            }
                            if (this.mMotionOut) {
                                Log.i(TAG, "motion out detected at locked state");
                                newStatus = 3;
                                break;
                            }
                        }
                    }
                    if (!this.mTakePictureAfter) {
                        if (!this.mFaceHeldOnPreview) {
                            newStatus = 4;
                            resetFaceCount();
                        }
                        if (this.mMotionOut) {
                            Log.i(TAG, "motion out detected at unlocked state");
                            newStatus = 3;
                        }
                    }
                    if (this.mStatus == newStatus && (afterShutterHoldTimePassed || (statusHoldTimePassed && motionStopDetected))) {
                        newStatus = 4;
                        resetFaceCount();
                        break;
                    }
                    break;
                case 6:
                    if (statusHoldTimePassed && !this.mFaceHeldOnPreview) {
                        newStatus = 4;
                        resetFaceCount();
                        break;
                    }
            }
            printLog("status=" + getStatusString(status) + ", elapsed=" + statusElapsedTime + "ms, mLocked=" + this.mLocked + ", mMotionStop=" + this.mMotionStop + ", mTakePictureAfter=" + this.mTakePictureAfter + ", mFaceHeld=" + this.mFaceHeld + ", mFaceHeldOnPreview=" + this.mFaceHeldOnPreview + ", mMotionOut=" + this.mMotionOut);
            if (this.mStatus != newStatus) {
                this.mStatusChangedRunnable.newStatus = newStatus;
                this.mStatusLocked = true;
                this.mMotionOut = false;
                this.mHandler.post(this.mStatusChangedRunnable);
            }
        }
    }

    private void evaluateFaceData(long time) {
        long currTime = time;
        int faceCount = this.mFaceDataCount;
        int faceCountElapsedTime = (int) (currTime - this.mFaceCountCandidateTime);
        if (faceCount != this.mFaceCount) {
            Log.i(TAG, "face count changed: " + this.mFaceCount + " -> " + faceCount);
            this.mFaceCount = faceCount;
            this.mFaceCountPrev = faceCount;
            this.mFaceCountCandidate = faceCount;
            if (this.mFaceCount > 0) {
                this.mIsFaceFirst = false;
            }
            if (this.mFaceCount == 0) {
                this.mFaceHoldCount = 0;
            }
        } else {
            this.mFaceCountCandidate = this.mFaceCount;
            this.mFaceCountCandidateTime = currTime;
        }
        float cx = 0.0f;
        float cy = 0.0f;
        boolean faceUpdated = false;
        if (faceCount > 0 && faceCount == this.mFaceCount) {
            Rect faceRect = this.mFaceData[0].rect;
            float fw = (float) (faceRect.right - faceRect.left);
            float fh = (float) (faceRect.bottom - faceRect.top);
            float faceSize = (fw * fw) + (fh * fh);
            if (faceCount > 1) {
                for (int i = 1; i < faceCount; i++) {
                    Rect currFaceRect = this.mFaceData[i].rect;
                    float currFw = (float) (currFaceRect.right - currFaceRect.left);
                    float currFh = (float) (currFaceRect.bottom - currFaceRect.top);
                    float currFaceSize = (currFw * currFw) + (currFh * currFh);
                    if (currFaceSize > faceSize) {
                        faceRect = currFaceRect;
                        faceSize = currFaceSize;
                    }
                }
            }
            cx = ((float) (faceRect.left + faceRect.right)) * 0.5f;
            cy = ((float) (faceRect.top + faceRect.bottom)) * 0.5f;
            faceUpdated = true;
        }
        if (faceUpdated && this.mFaceCount > 0 && this.mFaceCount == this.mFaceCountPrev) {
            float diffCx = cx - this.mFaceCx;
            float diffCy = cy - this.mFaceCy;
            float diffDist = (diffCx * diffCx) + (diffCy * diffCy);
            if (diffDist <= FACE_HOLD_IN) {
                this.mFaceHoldCount = Math.min(this.mFaceHoldCount + 1, 2);
            } else if (diffDist <= FACE_HOLD_UNCERTAIN) {
                this.mFaceHoldCount /= 2;
            } else {
                Log.i(TAG, "reset mFaceHoldCount because of unstable face position (delta: " + FloatMath.sqrt(diffDist) + ")");
                this.mFaceHoldCount = 0;
            }
            this.mFaceCx = cx;
            this.mFaceCy = cy;
        } else {
            if (this.mFaceCountPrev != this.mFaceCount) {
                this.mFaceCountPrev = this.mFaceCount;
                this.mFaceHoldCount = 0;
                Log.i(TAG, "reset mFaceHoldCount because of face count changed");
            }
        }
        this.mFaceHeld = this.mFaceHoldCount >= 2;
        if (this.mFaceCount == 0) {
            printLog("mFaceHeldOnPreview set false because of no face");
            this.mFaceHeldOnPreview = false;
        }
        printLog("mFaceCount=" + this.mFaceCount + ", mFaceHoldCount=" + this.mFaceHoldCount + ", mFaceCx=" + this.mFaceCx + ", mFaceCy=" + this.mFaceCy);
    }

    private void resetFaceCount() {
        this.mFaceDataCount = 0;
        this.mFaceCount = 0;
        this.mFaceHoldCount = 0;
        this.mFaceHeld = false;
    }

    private void evaluateSensorData(long time) {
        long currTime = time;
        if (checkTiltAngleIsSmall(this.mAccelMean)) {
            this.mOrientation = 2;
        } else if (this.mDegree == 0 || this.mDegree == 180) {
            this.mOrientation = 0;
        } else {
            this.mOrientation = 1;
        }
        float gyroSum = (float) Math.sqrt((double) (((this.mGyroSum[0] * this.mGyroSum[0]) + (this.mGyroSum[1] * this.mGyroSum[1])) + (this.mGyroSum[2] * this.mGyroSum[2])));
        float accelStdev = (float) Math.sqrt((double) (((this.mAccelStdev[0] * this.mAccelStdev[0]) + (this.mAccelStdev[1] * this.mAccelStdev[1])) + (this.mAccelStdev[2] * this.mAccelStdev[2])));
        enqueue(this.mAccelStdevSum, accelStdev, 10);
        computeLeastSquare(this.mAccelStdevSum, 10, this.mLSResult);
        boolean accelMotionStop = this.mAccelMotionStop;
        boolean gyroMotionStop = this.mGyroMotionStop;
        if (this.mSensorMode == 3) {
            if (!accelMotionStop && accelStdev <= 5.0f) {
                this.mAccelMotionStop = true;
            } else if (accelMotionStop && accelStdev > ACCEL_STDEV_MOTION_STOP_OUT) {
                this.mAccelMotionStop = false;
            }
            if (!gyroMotionStop && gyroSum <= 5.0f) {
                this.mGyroMotionStop = true;
            } else if (gyroMotionStop && gyroSum >= GYRO_MOTION_STOP_OUT) {
                this.mGyroMotionStop = false;
            }
        } else {
            float limitedStdev = Math.min(accelStdev, 2.0f);
            float actualAccelSlop = Math.abs((this.mLSResult.slop * 2.0f) / ((limitedStdev * limitedStdev) + 1.0f));
            if (!accelMotionStop && accelStdev <= 2.0f && actualAccelSlop <= 0.02f) {
                this.mAccelMotionStop = true;
            } else if (accelMotionStop && (accelStdev > ACCEL_LSR_STDEV_MOTION_STOP_OUT || actualAccelSlop > ACCEL_LSR_SLOP_MOTION_STOP_OUT)) {
                this.mAccelMotionStop = false;
            }
            this.mGyroMotionStop = true;
        }
        boolean motionStop = this.mAccelMotionStop && this.mGyroMotionStop;
        int motionElapsedTime = (int) (currTime - this.mMotionCandidateTime);
        if (motionStop != this.mMotionStop) {
            if (this.mMotionStop == this.mMotionStopCandidate) {
                this.mMotionStopCandidate = motionStop;
                this.mMotionCandidateTime = currTime;
            } else {
                int motion_hold_time = this.mMotionStop ? 70 : this.mIsSensorFirst ? 50 : 50;
                if (motionElapsedTime > motion_hold_time) {
                    this.mMotionStop = this.mMotionStopCandidate;
                    this.mIsSensorFirst = false;
                    printLog("mMotionStop changed: " + this.mMotionStop);
                }
            }
        } else {
            this.mMotionStopCandidate = this.mMotionStop;
            this.mMotionCandidateTime = currTime;
        }
        boolean accelMotionOut = this.mSensorMode == 3 ? accelStdev > ACCEL_STDEV_MOTION_OUT : accelStdev > 8.0f || Math.abs(this.mLSResult.slop) > ACCEL_LSR_SLOP_MOTION_OUT;
        boolean gyroMotionOut = gyroSum > 8.0f;
        int tiltAngle = computeTiltAngle(this.mAccelMean);
        boolean tiltMotionOut = Math.abs(tiltAngle - this.mBaseTiltAngle) > (this.mOrientation != 1 ? 30 : 30);
        this.mTiltAngle = tiltAngle;
        boolean rotVecMotionOut = false;
        if (this.mSensorMode == 3) {
            float rvAngleHoldOut = this.mOrientation != 1 ? 30.0f : 30.0f;
            for (int i = 0; i < 3; i++) {
                float rotVecAngle1 = Math.max(this.mBaseRotVecAngle[i], this.mRotVecAngle[i]);
                float rotVecAngle2 = Math.min(this.mBaseRotVecAngle[i], this.mRotVecAngle[i]);
                if (Math.min(Math.abs(rotVecAngle1 - rotVecAngle2), Math.abs((rotVecAngle2 - rotVecAngle1) + 360.0f)) > rvAngleHoldOut) {
                    rotVecMotionOut = true;
                }
            }
        }
        boolean z = tiltMotionOut || accelMotionOut || gyroMotionOut;
        this.mMotionOut = z;
        switch (this.mSensorMode) {
            case 1:
                printLog("mOrientation=" + this.mOrientation + ", mMotionStop=" + this.mMotionStop + ", mAccelMotionStop=" + this.mAccelMotionStop + " (stdev=" + accelStdev + "), mMotionOut=" + this.mMotionOut + ", accelMotionOut=" + accelMotionOut + ", tiltMotionOut=" + tiltMotionOut);
                return;
            case 3:
                printLog("mOrientation=" + this.mOrientation + ", mMotionStop=" + this.mMotionStop + ", mAccelMotionStop=" + this.mAccelMotionStop + " (stdev=" + accelStdev + "), mGyroMotionStop=" + this.mGyroMotionStop + " (sum=" + gyroSum + "), mMotionOut=" + this.mMotionOut + ", accelMotionOut=" + accelMotionOut + ", gyroMotionOut=" + gyroMotionOut + ", rotVecMotionOut=" + rotVecMotionOut + ", tiltMotionOut=" + tiltMotionOut);
                return;
            default:
                return;
        }
    }

    private void enqueue(float[] buffer, float value, int dataLength) {
        float[] sensorData = buffer;
        int length = dataLength;
        float sensorValue = value;
        for (int i = 1; i < length; i++) {
            sensorData[i - 1] = sensorData[i];
        }
        sensorData[length - 1] = sensorValue;
    }

    private float computeStdev(float[] buffer, float mean, int dataLength) {
        float[] sensorData = buffer;
        float meanValue = mean;
        float stdev = 0.0f;
        for (int i = 0; i < dataLength; i++) {
            float diff = meanValue - sensorData[i];
            stdev += diff * diff;
        }
        return (float) Math.sqrt((double) stdev);
    }

    private void computeLeastSquare(float[] buffer, int dataLength, LeastSquareResult mLSResult) {
        float[] y = buffer;
        int length = dataLength;
        float n = (float) dataLength;
        float sumX = 0.0f;
        float sumY = 0.0f;
        float sumX2 = 0.0f;
        float sumY2 = 0.0f;
        float sumXy = 0.0f;
        for (int i = 0; ((float) i) < n; i++) {
            float x = (float) i;
            sumX += x;
            sumY += y[i];
            sumX2 += x * x;
            sumY2 += y[i] * y[i];
            sumXy += y[i] * x;
        }
        float sumYY = sumY * sumY;
        mLSResult.slop = ((n * sumXy) - (sumX * sumY)) / ((n * sumX2) - (sumX * sumX));
    }

    private boolean checkTiltAngleIsSmall(float[] accelBuffer) {
        float ax = accelBuffer[0];
        float ay = accelBuffer[1];
        float az = accelBuffer[2];
        if ((ax * ax) + (ay * ay) < (az * az) / ACCEL_LSR_STDEV_MOTION_STOP_OUT) {
            return true;
        }
        return false;
    }

    private int computeTiltAngle(float[] accelBuffer) {
        float ax = accelBuffer[0];
        float ay = accelBuffer[1];
        float az = accelBuffer[2];
        float planeAccel = (ax * ax) + (ay * ay);
        return arcSinApprox(FloatMath.sqrt(planeAccel / (planeAccel + (az * az))));
    }

    private int arcSinApprox(float sinValue) {
        float sin = sinValue;
        float[] arcSinTable = new float[]{0.870356f, 0.878817f, 0.887011f, 0.894934f, 0.902585f, 0.909961f, 0.91706f, 0.92388f, 0.930418f, 0.936672f, 0.942641f, 0.948324f, 0.953717f, 0.95882f, 0.96363f, 0.968148f, 0.97237f, 0.976296f, 0.979925f, 0.983255f, 0.986286f, 0.989016f, 0.991445f, 0.993572f, 0.995396f, 0.996917f, 0.998135f, 0.999048f, 0.999657f, 0.999962f};
        if (sin < 0.861629f) {
            float s = sin;
            float s2 = sinValue * sinValue;
            float s3 = s * s2;
            float s5 = s3 * s2;
            float s7 = s5 * s2;
            float s9 = s7 * s2;
            return Math.round((((((0.166667f * s3) + s) + (0.075f * s5)) + (0.0446429f * s7)) + (0.0303819f * s9)) + (0.0223722f * (s9 * s2)));
        }
        int length = arcSinTable.length;
        for (int i = 0; i < length; i++) {
            if (sin < arcSinTable[i]) {
                return i + 60;
            }
        }
        return 90;
    }

    private void initAndResetInternal() {
        resetInternal();
        emptyHandler();
        Log.i(TAG, "init ShutterlessEngine");
    }

    private void resetInternal() {
        resetFaceDataInternal();
        resetSensorDataInternal();
        this.mIsSensorFirst = true;
        this.mIsFaceFirst = true;
        this.mIsResetFirst = false;
        this.mTakePictureAfter = false;
    }

    private void resetFaceDataInternal() {
        this.mFaceHeld = false;
        this.mFaceHeldOnPreview = false;
        this.mFaceCount = 0;
        this.mFaceCountPrev = 0;
        this.mFaceCountCandidate = 0;
        this.mFaceDataCount = 0;
        this.mFaceHoldCount = 0;
        this.mFaceCx = 0.0f;
        this.mFaceCy = 0.0f;
        this.mFaceCountCandidateTime = this.mStatusTime;
    }

    private void resetSensorDataInternal() {
        Log.i(TAG, "resetSensorDataInternal");
        this.mAccelMotionStop = false;
        this.mGyroMotionStop = false;
        this.mMotionStop = false;
        this.mMotionStopCandidate = false;
        this.mMotionOut = false;
        this.mMotionCandidateTime = this.mStatusTime;
        this.mLastAccelSensorTimestamp = 0;
        this.mTiltAngle = 0;
        this.mBaseTiltAngle = 0;
        if (this.mIsResetFirst) {
            int i;
            int accelDataSize = this.mAccelDataSize;
            for (i = 0; i < 3; i++) {
                int j;
                float[] fArr;
                float[] accel = this.mAccelValue[i];
                for (j = 0; j < accelDataSize; j++) {
                    accel[j] = 5.0f;
                }
                float[] accelDiff = this.mAccelDiffValue[i];
                for (j = 0; j < accelDataSize; j++) {
                    accelDiff[j] = 0.0f;
                }
                float[] gyro = this.mGyroValue[i];
                for (j = 0; j < 30; j++) {
                    gyro[j] = 0.0f;
                }
                this.mAccelStdev[i] = 0.0f;
                this.mAccelSum[i] = 0.0f;
                this.mAccelMean[i] = 0.0f;
                this.mGyroSum[i] = 0.0f;
                this.mRotVecAngle[i] = 0.0f;
                this.mBaseRotVecAngle[i] = 0.0f;
                for (j = 0; j < accelDataSize; j++) {
                    fArr = this.mAccelSum;
                    fArr[i] = fArr[i] + accel[j];
                }
                fArr = this.mAccelMean;
                fArr[i] = fArr[i] / ((float) accelDataSize);
            }
            for (i = 0; i < 10; i++) {
                this.mAccelStdevSum[i] = (float) (i * 10);
            }
        }
    }

    private void emptyHandler() {
        this.mHandler.removeCallbacks(this.mStatusChangedRunnable);
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
    }

    private String getStatusString(int what) {
        switch (what) {
            case 0:
                return "STATUS_REST";
            case 1:
                return "STATUS_INIT_AND_IN_MOTION";
            case 2:
                return "STATUS_MOTION_STOP_FOR_TIMER_SHUTTER";
            case 3:
                return "STATUS_CANCEL_TIMER_AND_IN_MOTION";
            case 4:
                return "STATUS_IN_MOTION_AFTER_SHUTTER";
            case 5:
                return "STATUS_MOTION_STOP_FOR_INSTANT_SHUTTER";
            case 6:
                return "STATUS_PAUSE_SHUTTER_UNTIL_RESET_CONDITION";
            default:
                return "unknown status number";
        }
    }

    private void printLog(String msg) {
        if (ENABLE_LOG) {
            Log.v(TAG, msg);
        }
    }
}
