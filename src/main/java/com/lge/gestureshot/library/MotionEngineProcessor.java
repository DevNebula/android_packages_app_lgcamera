package com.lge.gestureshot.library;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import com.lge.camera.device.ParamConstants;

public class MotionEngineProcessor {
    private static final int MOTION_ENGINE_ERROR_MSG = 1;
    private static final int MOTION_ENGINE_PULL_MSG = 2;
    private static final int MOTION_ENGINE_PUSH_MSG = 3;
    private static final int MOTION_ERROR = -1;
    private static final int MOTION_PULL = 0;
    private static final int MOTION_PUSH = 1;
    private final String TAG = "GestureShot";
    private Sensor mAccelerometer;
    private final Context mContext;
    private MotionCallback mMotionCallback;
    private MotionEngineListener mMotionEngineListener;
    private Handler mMotionMessageHandler = new Handler(new C14441());
    private SensorManager mSensorManager;

    public interface MotionCallback {
        void onMotionEnginePullCallback();

        void onMotionEnginePushCallback();
    }

    /* renamed from: com.lge.gestureshot.library.MotionEngineProcessor$1 */
    class C14441 implements Callback {
        C14441() {
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d("GestureShot", "MOTION_ERROR : -1");
                    break;
                case 2:
                    MotionEngineProcessor.this.mMotionCallback.onMotionEnginePullCallback();
                    break;
                case 3:
                    MotionEngineProcessor.this.mMotionCallback.onMotionEnginePushCallback();
                    break;
            }
            return false;
        }
    }

    private class MotionEngineListener implements SensorEventListener {
        float[] m_SensorData;

        private MotionEngineListener() {
            this.m_SensorData = new float[3];
        }

        /* synthetic */ MotionEngineListener(MotionEngineProcessor x0, C14441 x1) {
            this();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                this.m_SensorData[0] = event.values[0];
                this.m_SensorData[1] = event.values[1];
                this.m_SensorData[2] = event.values[2];
                int nRes = GestureEngine.motionUpdate(this.m_SensorData);
                if (MotionEngineProcessor.this.mMotionMessageHandler == null) {
                    return;
                }
                if (nRes == 0) {
                    Log.d("GestureShot", "###onSensorChanged MOTION_PULL ");
                    MotionEngineProcessor.this.mMotionMessageHandler.sendEmptyMessage(2);
                } else if (nRes == 1) {
                    Log.d("GestureShot", "###onSensorChanged MOTION_PUSH ");
                    MotionEngineProcessor.this.mMotionMessageHandler.sendEmptyMessage(3);
                }
            }
        }
    }

    public MotionEngineProcessor(Context context, MotionCallback motionCallback) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService(ParamConstants.VALUE_BINNING_SENSOR);
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        this.mMotionEngineListener = new MotionEngineListener(this, null);
        this.mMotionCallback = motionCallback;
    }

    public void Start() {
        GestureEngine.motionInitialize();
        this.mSensorManager.registerListener(this.mMotionEngineListener, this.mAccelerometer, 2);
    }

    public void ResumePush() {
        GestureEngine.motionResumePush();
    }

    public void Stop() {
        this.mSensorManager.unregisterListener(this.mMotionEngineListener);
        GestureEngine.motionRelease();
    }

    public void Reset() {
        GestureEngine.motionReset();
    }
}
