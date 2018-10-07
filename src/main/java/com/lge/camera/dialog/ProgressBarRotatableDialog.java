package com.lge.camera.dialog;

import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressBarRotatableDialog extends RotateDialog {
    private final int TIMER_PERIOD = 100;
    private TextView mEditText = null;
    private Handler mHandler = null;
    private boolean mIsShowing = false;
    private ProgressBar mProgressBar = null;
    private int mRemainingTime = 0;
    private TimerTask mTask = null;
    private int mTime = 1000;

    /* renamed from: com.lge.camera.dialog.ProgressBarRotatableDialog$1 */
    class C07531 implements OnClickListener {
        C07531() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "[dialog] cancel button click....");
            ProgressBarRotatableDialog.this.mGet.doCancleClickDuringSlomoSaving();
            ProgressBarRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.ProgressBarRotatableDialog$2 */
    class C07542 implements Runnable {
        C07542() {
        }

        public void run() {
            ProgressBarRotatableDialog.this.mIsShowing = false;
        }
    }

    /* renamed from: com.lge.camera.dialog.ProgressBarRotatableDialog$3 */
    class C07553 extends TimerTask {
        C07553() {
        }

        public void run() {
            ProgressBarRotatableDialog.this.mRemainingTime = ProgressBarRotatableDialog.this.mRemainingTime - 100;
            if (ProgressBarRotatableDialog.this.mRemainingTime < 0) {
                ProgressBarRotatableDialog.this.mRemainingTime = 0;
            }
        }
    }

    /* renamed from: com.lge.camera.dialog.ProgressBarRotatableDialog$4 */
    class C07564 implements Runnable {
        C07564() {
        }

        public void run() {
            ProgressBarRotatableDialog.this.onDismiss();
        }
    }

    public ProgressBarRotatableDialog(CamDialogInterface function) {
        super(function);
        init();
    }

    public void create(int rId) {
        View v = this.mGet.inflateView(C0088R.layout.progress_bar_rotate_dialog);
        this.mProgressBar = (ProgressBar) v.findViewById(C0088R.id.progress_bar_for_dialog);
        this.mEditText = (TextView) v.findViewById(C0088R.id.text_in_progress);
        ((Button) v.findViewById(C0088R.id.cancel_button)).setOnClickListener(new C07531());
        super.create(v, false, true);
    }

    private void init() {
        this.mIsShowing = true;
        this.mRemainingTime = this.mTime;
        this.mHandler = new Handler();
        this.mHandler.postDelayed(new C07542(), (long) this.mTime);
        this.mTask = new C07553();
        new Timer().schedule(this.mTask, 0, 100);
    }

    public boolean onDismiss(boolean immediately) {
        CamLog.m3d(CameraConstants.TAG, "[dialog] ProgressBarRotatableDialog onDismiss immediately =" + immediately);
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
            this.mHandler.postDelayed(new C07564(), (long) this.mRemainingTime);
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

    public void setProgress(int progress) {
        if (this.mProgressBar != null && this.mEditText != null) {
            this.mProgressBar.setProgress(progress);
            this.mEditText.setText(((int) (100.0d * (((double) progress) / 100.0d))) + "%");
        }
    }
}
