package com.lge.camera.dialog;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressCircleRotatableDialog extends RotateDialog {
    private final int TIMER_PERIOD = 100;
    private Handler mHandler = null;
    private boolean mIsShowing = false;
    private int mRemainingTime = 0;
    private TimerTask mTask = null;
    private int mTime = 1000;

    /* renamed from: com.lge.camera.dialog.ProgressCircleRotatableDialog$1 */
    class C07571 implements Runnable {
        C07571() {
        }

        public void run() {
            ProgressCircleRotatableDialog.this.mIsShowing = false;
        }
    }

    /* renamed from: com.lge.camera.dialog.ProgressCircleRotatableDialog$2 */
    class C07582 extends TimerTask {
        C07582() {
        }

        public void run() {
            ProgressCircleRotatableDialog.this.mRemainingTime = ProgressCircleRotatableDialog.this.mRemainingTime - 100;
            if (ProgressCircleRotatableDialog.this.mRemainingTime < 0) {
                ProgressCircleRotatableDialog.this.mRemainingTime = 0;
            }
        }
    }

    /* renamed from: com.lge.camera.dialog.ProgressCircleRotatableDialog$3 */
    class C07593 implements Runnable {
        C07593() {
        }

        public void run() {
            ProgressCircleRotatableDialog.this.onDismiss();
        }
    }

    public ProgressCircleRotatableDialog(CamDialogInterface function) {
        super(function);
        init();
    }

    public void create(int rId) {
        View v = this.mGet.inflateView(C0088R.layout.progress_circle_rotate_dialog);
        ((TextView) v.findViewById(C0088R.id.message_text)).setText(rId);
        super.create(v, false, true);
    }

    private void init() {
        this.mIsShowing = true;
        this.mRemainingTime = this.mTime;
        this.mHandler = new Handler();
        this.mHandler.postDelayed(new C07571(), (long) this.mTime);
        this.mTask = new C07582();
        new Timer().schedule(this.mTask, 0, 100);
    }

    public boolean onDismiss(boolean immediately) {
        CamLog.m3d(CameraConstants.TAG, "[dialog] ProgressRotatableDialog onDismiss immediately =" + immediately);
        if (!immediately) {
            return onDismiss();
        }
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask = null;
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler = null;
        }
        return super.onDismiss();
    }

    public boolean onDismiss() {
        if (this.mIsShowing) {
            this.mHandler.postDelayed(new C07593(), (long) this.mRemainingTime);
            return false;
        }
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask = null;
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler = null;
        }
        return super.onDismiss();
    }
}
