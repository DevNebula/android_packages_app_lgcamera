package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ICameraCallback.CineZoomCallback;
import com.lge.camera.device.ICameraCallback.ZoomChangeCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import java.util.Timer;
import java.util.TimerTask;

public class AdvancedZoomManager {
    private final int CZ_CMD_ZOOM_IN = 1;
    private final int CZ_CMD_ZOOM_NONE = 0;
    private final int CZ_CMD_ZOOM_OUT = 2;
    private final int CZ_CMD_ZOOM_PAUSE = 3;
    private final int CZ_CMD_ZOOM_RESET = 4;
    protected final int CZ_STATUS_IDLE = 0;
    protected final int CZ_STATUS_IN = 1;
    protected final int CZ_STATUS_IN_DONE = 2;
    protected final int CZ_STATUS_OUT = 3;
    protected final int CZ_STATUS_OUT_DONE = 4;
    protected final int CZ_STATUS_PAUSE = 5;
    private int FRAMEWORK_HEIGHT = 3492;
    private int FRAMEWORK_TOP = 0;
    private final int FRAMEWORK_WIDTH = 4656;
    private final int JOGZOOM_MAX_WAIT = 7;
    private Timer JogZoomTimer = null;
    private TimerTask JogZoomTimerTask = null;
    private Timer PointZoomTimer = null;
    private TimerTask PointZoomTimerTask = null;
    private final int ZOOM_REPEAT_UNIT = 30;
    private ICameraOps mCameraOps = null;
    private CineZoomCallback mCineZoomCallback;
    private int mCurrJogZoomSpeed = 0;
    private int mCurrZoomLevel = 0;
    private Handler mHandler = null;
    private boolean mIsJogZoomRunning = false;
    private int mJogZoomRequestCount = 0;
    private int mJogZoomRequestId;
    private boolean mJogZoomResultStarted = false;
    private int mMaxZoomLevel = 0;
    private int mPointZoomCMD = 0;
    private Rect mPointZoomCropRect;
    private RectF mPointZoomCropRectF;
    private Rect mPointZoomOrgRect;
    private int mPointZoomSpeed = 200;
    private int mPointZoomStatus = 0;
    private Rect mPointZoomTargetRect;
    private int mPrevJogZoomSpeed = 0;
    private int mPrevPointZoomCMD = 0;
    private ZoomChangeCallback mZoomChangeCallback;

    /* renamed from: com.lge.camera.device.api2.AdvancedZoomManager$1 */
    class C06851 extends TimerTask {

        /* renamed from: com.lge.camera.device.api2.AdvancedZoomManager$1$1 */
        class C06841 implements Runnable {
            C06841() {
            }

            public void run() {
                AdvancedZoomManager.this.setPointZoom();
            }
        }

        C06851() {
        }

        public void run() {
            if (AdvancedZoomManager.this.PointZoomTimer != null && AdvancedZoomManager.this.PointZoomTimerTask != null) {
                if (AdvancedZoomManager.this.mJogZoomRequestCount > 7) {
                    CamLog.m3d(CameraConstants.TAG, "[jog] getJogZoomWaitingNum over.   Do nothing!");
                } else {
                    AdvancedZoomManager.this.mHandler.post(new C06841());
                }
            }
        }
    }

    /* renamed from: com.lge.camera.device.api2.AdvancedZoomManager$2 */
    class C06872 extends TimerTask {

        /* renamed from: com.lge.camera.device.api2.AdvancedZoomManager$2$1 */
        class C06861 implements Runnable {
            C06861() {
            }

            public void run() {
                AdvancedZoomManager.this.mCameraOps.setJogZoom(AdvancedZoomManager.this.mCurrZoomLevel);
            }
        }

        C06872() {
        }

        public void run() {
            if (AdvancedZoomManager.this.JogZoomTimer != null && AdvancedZoomManager.this.JogZoomTimerTask != null && AdvancedZoomManager.this.mIsJogZoomRunning) {
                if (AdvancedZoomManager.this.mJogZoomRequestCount > 7) {
                    CamLog.m3d(CameraConstants.TAG, "[jog] getJogZoomWaitingNum over.   Do nothing!");
                    return;
                }
                AdvancedZoomManager.this.mCurrZoomLevel = AdvancedZoomManager.this.mCurrZoomLevel + AdvancedZoomManager.this.mCurrJogZoomSpeed;
                if (AdvancedZoomManager.this.mCurrZoomLevel > AdvancedZoomManager.this.mMaxZoomLevel) {
                    AdvancedZoomManager.this.mCurrZoomLevel = AdvancedZoomManager.this.mMaxZoomLevel;
                }
                if (AdvancedZoomManager.this.mCurrZoomLevel < 0) {
                    AdvancedZoomManager.this.mCurrZoomLevel = 0;
                }
                if (AdvancedZoomManager.this.mZoomChangeCallback != null) {
                    AdvancedZoomManager.this.mZoomChangeCallback.onZoomChange(AdvancedZoomManager.this.mCurrZoomLevel);
                }
                AdvancedZoomManager.this.mHandler.post(new C06861());
            }
        }
    }

    public AdvancedZoomManager(Handler handler, ICameraOps ops) {
        this.mHandler = handler;
        this.mCameraOps = ops;
        this.mPointZoomOrgRect = new Rect(0, this.FRAMEWORK_TOP, 4656, this.FRAMEWORK_TOP + this.FRAMEWORK_HEIGHT);
        this.mPointZoomCropRect = new Rect(0, this.FRAMEWORK_TOP, 4656, this.FRAMEWORK_TOP + this.FRAMEWORK_HEIGHT);
        this.mPointZoomCropRectF = new RectF(0.0f, ((float) this.FRAMEWORK_TOP) * 1.0f, 4656.0f, ((float) (this.FRAMEWORK_TOP + this.FRAMEWORK_HEIGHT)) * 1.0f);
        this.mPointZoomTargetRect = new Rect(1746, this.FRAMEWORK_TOP + ((this.FRAMEWORK_HEIGHT * 3) / 8), 2910, this.FRAMEWORK_TOP + ((this.FRAMEWORK_HEIGHT * 5) / 8));
    }

    public void checkAdvancedZoom(Parameters2 param) {
        if (!checkJogZoom(param) && checkPointZoom(param)) {
        }
    }

    private boolean checkPointZoom(Parameters2 param) {
        String value = param.get(ParamConstants.KEY_POINT_ZOOM);
        if (value == null) {
            return false;
        }
        String[] values = value.split(" ");
        if (values != null) {
            initPointZoom(values);
            if (this.mPointZoomCMD == 1 || this.mPointZoomCMD == 2) {
                if (this.mPointZoomStatus == 0) {
                    setPointZoomTarget(values);
                    startPointZoom();
                } else if (this.mPointZoomStatus == 5) {
                    startPointZoom();
                }
            } else if (this.mPointZoomCMD == 3) {
                if (!(this.mPointZoomStatus == 5 || this.mPointZoomStatus == 0)) {
                    stopPointZoom();
                }
            } else if (this.mPointZoomCMD == 4 && this.mPointZoomStatus != 0) {
                resetPointZoom();
            }
        }
        this.mPrevPointZoomCMD = this.mPointZoomCMD;
        return true;
    }

    private void setPointZoomStatus(int status) {
        if ((!this.mIsJogZoomRunning && (status == 1 || status == 3 || status == 2 || status == 4)) || this.mPointZoomStatus == status) {
            return;
        }
        if (this.mPointZoomStatus != 2 || status != 1) {
            if (this.mPointZoomStatus != 4 || status != 3) {
                this.mPointZoomStatus = status;
            }
        }
    }

    private void initPointZoom(String[] values) {
        if (values != null && values.length > 5) {
            this.mPointZoomCMD = Integer.parseInt(values[0]);
            this.mPointZoomSpeed = Integer.parseInt(values[1]);
        }
    }

    private void setPointZoomTarget(String[] values) {
        if (values != null && values.length > 5 && this.mPointZoomTargetRect != null) {
            this.mPointZoomTargetRect.set(Integer.parseInt(values[2]), this.FRAMEWORK_TOP + Integer.parseInt(values[3]), Integer.parseInt(values[4]), this.FRAMEWORK_TOP + Integer.parseInt(values[5]));
        }
    }

    public void resetPointZoom() {
        if (!(this.mPointZoomOrgRect == null || this.mPointZoomTargetRect == null || this.mPointZoomCropRect == null || this.mPointZoomCropRectF == null || this.mCameraOps == null)) {
            this.mPointZoomCropRectF.set(this.mPointZoomOrgRect);
            this.mPointZoomCropRect.set(this.mPointZoomOrgRect);
            this.mIsJogZoomRunning = true;
            this.mCameraOps.startZoomAction();
            this.mCameraOps.setPointZoom(this.mPointZoomOrgRect);
            this.mCameraOps.setPointZoom(null);
            this.mIsJogZoomRunning = false;
            this.mCameraOps.stopZoomAction();
        }
        setPointZoomStatus(0);
    }

    public boolean setPointZoom() {
        boolean result = false;
        float factor = 1.0f;
        if (!this.mIsJogZoomRunning) {
            return 0;
        }
        if (this.mPointZoomCMD == 1) {
            factor = 1.0f;
            setPointZoomStatus(1);
        } else if (this.mPointZoomCMD == 2) {
            factor = -1.0f;
            setPointZoomStatus(3);
        }
        if (!(this.mPointZoomOrgRect == null || this.mPointZoomCropRect == null || this.mPointZoomCropRectF == null || this.mPointZoomTargetRect == null)) {
            this.mPointZoomCropRectF.set(this.mPointZoomCropRectF.left + ((((float) this.mPointZoomTargetRect.left) * factor) / ((float) this.mPointZoomSpeed)), this.mPointZoomCropRectF.top + ((((float) (this.mPointZoomTargetRect.top - this.FRAMEWORK_TOP)) * factor) / ((float) this.mPointZoomSpeed)), this.mPointZoomCropRectF.right - ((((float) (4656 - this.mPointZoomTargetRect.right)) * factor) / ((float) this.mPointZoomSpeed)), this.mPointZoomCropRectF.bottom - ((((float) ((this.FRAMEWORK_HEIGHT + this.FRAMEWORK_TOP) - this.mPointZoomTargetRect.bottom)) * factor) / ((float) this.mPointZoomSpeed)));
            this.mPointZoomCropRect.set((int) this.mPointZoomCropRectF.left, (int) this.mPointZoomCropRectF.top, (int) this.mPointZoomCropRectF.right, (int) this.mPointZoomCropRectF.bottom);
            if (this.mPointZoomOrgRect.contains(this.mPointZoomCropRect) && this.mPointZoomCropRect.contains(this.mPointZoomTargetRect)) {
                result = true;
            } else if (!this.mPointZoomOrgRect.contains(this.mPointZoomCropRect)) {
                this.mPointZoomCropRectF.set(this.mPointZoomOrgRect);
                this.mPointZoomCropRect.set(this.mPointZoomOrgRect);
                setPointZoomStatus(4);
            } else if (!this.mPointZoomCropRect.contains(this.mPointZoomTargetRect)) {
                this.mPointZoomCropRectF.set(this.mPointZoomTargetRect);
                this.mPointZoomCropRect.set(this.mPointZoomTargetRect);
                setPointZoomStatus(2);
            }
            this.mCameraOps.setPointZoom(this.mPointZoomCropRect);
            if (this.mCineZoomCallback != null) {
                this.mCineZoomCallback.onCineZoom(this.mPointZoomStatus);
            }
        }
        return result;
    }

    private void stopPointZoom() {
        CamLog.m7i(CameraConstants.TAG, "stopPointZoom: ");
        this.mIsJogZoomRunning = false;
        resetJogZoomRequest();
        this.mCameraOps.stopZoomAction();
        if (this.PointZoomTimerTask != null) {
            this.PointZoomTimerTask.cancel();
            this.PointZoomTimerTask = null;
        }
        if (this.PointZoomTimer != null) {
            this.PointZoomTimer.cancel();
            this.PointZoomTimer = null;
        }
        if (this.mPointZoomStatus == 4) {
            resetPointZoom();
        } else {
            setPointZoomStatus(5);
        }
    }

    private void startPointZoom() {
        this.mIsJogZoomRunning = true;
        resetJogZoomRequest();
        this.mCameraOps.startZoomAction();
        this.PointZoomTimer = new Timer();
        this.PointZoomTimerTask = new C06851();
        this.PointZoomTimer.schedule(this.PointZoomTimerTask, 1, 30);
    }

    private boolean checkJogZoom(Parameters2 param) {
        boolean handled = false;
        String jogZoom = param.get(ParamConstants.KEY_JOG_ZOOM);
        if (jogZoom == null || "not found".equals(jogZoom)) {
            return false;
        }
        this.mCurrJogZoomSpeed = Integer.parseInt(jogZoom);
        if (this.mPrevJogZoomSpeed == 0) {
            if (this.mCurrJogZoomSpeed != 0) {
                startJogZoom(param);
                handled = true;
            }
        } else if (this.mCurrJogZoomSpeed == 0) {
            stopJogZoom();
            handled = true;
        }
        this.mPrevJogZoomSpeed = this.mCurrJogZoomSpeed;
        return handled;
    }

    private void startJogZoom(Parameters2 param) {
        this.mIsJogZoomRunning = true;
        this.mCurrZoomLevel = Integer.parseInt(param.get("zoom"));
        this.mMaxZoomLevel = Integer.parseInt(param.get(ParamConstants.KEY_MAX_ZOOM));
        this.mCameraOps.startZoomAction();
        resetJogZoomRequest();
        this.JogZoomTimer = new Timer();
        this.JogZoomTimerTask = new C06872();
        this.JogZoomTimer.schedule(this.JogZoomTimerTask, 1, 30);
    }

    private void stopJogZoom() {
        this.mIsJogZoomRunning = false;
        resetJogZoomRequest();
        this.mCameraOps.stopZoomAction();
        if (this.JogZoomTimerTask != null) {
            this.JogZoomTimerTask.cancel();
            this.JogZoomTimerTask = null;
        }
        if (this.JogZoomTimer != null) {
            this.JogZoomTimer.cancel();
            this.JogZoomTimer = null;
        }
    }

    public boolean isJogZoomRunning() {
        return this.mIsJogZoomRunning;
    }

    public void resetJogZoomRequest() {
        this.mJogZoomRequestId = -1;
        this.mJogZoomRequestCount = 0;
        this.mJogZoomResultStarted = false;
    }

    public void addJogZoomRequest(int req_id) {
        if (this.mJogZoomRequestId == -1) {
            this.mJogZoomRequestId = req_id;
            this.mJogZoomRequestCount = 0;
            this.mJogZoomResultStarted = false;
        }
        this.mJogZoomRequestCount++;
    }

    public void checkJogZoomRequest(TotalCaptureResult result) {
        if (this.mJogZoomRequestId != -1 && result.getRequest().hashCode() == this.mJogZoomRequestId) {
            CamLog.m3d(CameraConstants.TAG, "[jog] result start! ");
            this.mJogZoomResultStarted = true;
        }
        if (this.mJogZoomResultStarted) {
            this.mJogZoomRequestCount--;
        }
    }

    public void setZoomChangeCallback(ZoomChangeCallback zoomChangeCallback) {
        CamLog.m7i(CameraConstants.TAG, "setZoomChangeCallback : " + zoomChangeCallback);
        this.mZoomChangeCallback = zoomChangeCallback;
    }

    public void setCineZoomCallback(CineZoomCallback cineZoomCallback) {
        this.mCineZoomCallback = cineZoomCallback;
    }

    public void reset() {
        resetJogZoomRequest();
        this.mIsJogZoomRunning = false;
        if (this.JogZoomTimerTask != null) {
            this.JogZoomTimerTask.cancel();
            this.JogZoomTimerTask = null;
        }
        if (this.JogZoomTimer != null) {
            this.JogZoomTimer.cancel();
            this.JogZoomTimer = null;
        }
    }
}
