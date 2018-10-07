package com.lge.camera.app.ext;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Message;
import android.text.SpannableString;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.AnimatedPictureEncoder;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.AnimatedPictureOutputInfo;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.ShiftImageSpan;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorConverter;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.Utils;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PopoutFrameModuleExpand extends PopoutFrameModule {
    protected static final int ANIMATED_PICTURE_BITRATE = 4000000;
    protected static final int ANIMATED_PICTURE_DURATION = 6;
    protected static final int ANIMATED_PICTURE_FPS = 24;
    protected AnimatedPictureOutputInfo mAnimatedOutputInfo;
    protected AnimatedPictureTask mAnimatedPictureSaveTask;
    protected int[] mAnimatedPictureSize = new int[]{1280, CameraConstantsEx.HD_SCREEN_RESOLUTION};
    protected Timer mAnimatedPictureTimer = null;
    private BlockingQueue<Bitmap> mCapturedDataQueue;
    private boolean mCleanViewByAction = false;
    private int mCurScreenCaptureCnt = 0;
    private int mCurYuvDataCnt = 0;
    private byte[][] mEncodingDataArray;
    private DataEncodingThread mEncodingThread = null;
    private View mPopoutFrameBaseView = null;

    /* renamed from: com.lge.camera.app.ext.PopoutFrameModuleExpand$1 */
    class C04781 extends Thread {
        C04781() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[popout] Wait saving animated picture");
            int waitCnt = 0;
            while (PopoutFrameModuleExpand.this.mAnimatedPictureState == 4 && waitCnt <= 100) {
                try {
                    C04781.sleep(100);
                    waitCnt++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PopoutFrameModuleExpand$2 */
    class C04792 extends TimerTask {
        C04792() {
        }

        public void run() {
            if (PopoutFrameModuleExpand.this.mCapturedDataQueue == null) {
                PopoutFrameModuleExpand.this.stopTakingAnimatedPicture(false);
            } else if (PopoutFrameModuleExpand.this.mCurScreenCaptureCnt >= 24) {
                PopoutFrameModuleExpand.this.stopTakingAnimatedPicture(true);
            } else {
                try {
                    PopoutFrameModuleExpand.this.mCapturedDataQueue.put(Utils.getScreenShot(PopoutFrameModuleExpand.this.mAnimatedPictureSize[0], PopoutFrameModuleExpand.this.mAnimatedPictureSize[1], true, 3));
                    PopoutFrameModuleExpand.this.mCurScreenCaptureCnt = PopoutFrameModuleExpand.this.mCurScreenCaptureCnt + 1;
                    CamLog.m7i(CameraConstants.TAG, "[popout] screen capture count : " + PopoutFrameModuleExpand.this.mCurScreenCaptureCnt);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AnimatedPictureTask extends AsyncTask<Void, Void, Void> {
        private AnimatedPictureTask() {
        }

        /* synthetic */ AnimatedPictureTask(PopoutFrameModuleExpand x0, C04781 x1) {
            this();
        }

        protected Void doInBackground(Void... arg0) {
            String extend = ".mp4";
            String dir = PopoutFrameModuleExpand.this.getCurDir();
            int storage = PopoutFrameModuleExpand.this.getCurStorage();
            if (PopoutFrameModuleExpand.this.mRecordingUIManager != null) {
                PopoutFrameModuleExpand.this.mRecordingUIManager.initVideoTime();
            }
            String fileName = PopoutFrameModuleExpand.this.makeFileName(1, storage, dir, false, PopoutFrameModuleExpand.this.getSettingValue(Setting.KEY_MODE));
            String outFilePath = dir + fileName + extend;
            CamLog.m3d(CameraConstants.TAG, "[popout] output file is : " + outFilePath);
            File outFile = new File(outFilePath);
            PopoutFrameModuleExpand.this.mAnimatedOutputInfo = new AnimatedPictureOutputInfo(PopoutFrameModuleExpand.this.mAnimatedPictureSize[0], PopoutFrameModuleExpand.this.mAnimatedPictureSize[1], 24, PopoutFrameModuleExpand.ANIMATED_PICTURE_BITRATE, 1, 6, (PopoutFrameModuleExpand.this.getOrientationDegree() + 90) % 360);
            AnimatedPictureEncoder animatedPictureEncoder = new AnimatedPictureEncoder(PopoutFrameModuleExpand.this.mEncodingDataArray, outFile, PopoutFrameModuleExpand.this.mAnimatedOutputInfo);
            if (animatedPictureEncoder != null) {
                try {
                    animatedPictureEncoder.encodeVideoFromBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!"on".equals(PopoutFrameModuleExpand.this.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(PopoutFrameModuleExpand.this.getAppContext(), false);
            }
            if (PopoutFrameModuleExpand.this.mQuickClipManager != null) {
                PopoutFrameModuleExpand.this.mQuickClipManager.setAfterShot();
                PopoutFrameModuleExpand.this.setQuickClipIcon(false, false);
            }
            if (PopoutFrameModuleExpand.this.mGet.getMediaSaveService() != null) {
                PopoutFrameModuleExpand.this.mGet.getMediaSaveService().addVideo(PopoutFrameModuleExpand.this.mGet.getAppContext(), PopoutFrameModuleExpand.this.mGet.getAppContext().getContentResolver(), dir, fileName, PopoutFrameModuleExpand.this.mAnimatedOutputInfo.getVideoWidth() + "x" + PopoutFrameModuleExpand.this.mAnimatedOutputInfo.getVideoHeight(), (long) (PopoutFrameModuleExpand.this.mAnimatedOutputInfo.getVideoTimeLength() * 1000), outFile.length(), PopoutFrameModuleExpand.this.mLocationServiceManager.getCurrentLocation(), 1, PopoutFrameModuleExpand.this.mOnMediaSavedListener, 15);
            }
            PopoutFrameModuleExpand.this.unbindCapturedData();
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PopoutFrameModuleExpand.this.showSavingDialog(false, 0);
        }
    }

    private class DataEncodingThread extends Thread {
        private DataEncodingThread() {
        }

        /* synthetic */ DataEncodingThread(PopoutFrameModuleExpand x0, C04781 x1) {
            this();
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[popout] Bitmap to YUV encoding - start ");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PopoutFrameModuleExpand.this.mEncodingDataArray[PopoutFrameModuleExpand.this.mCurYuvDataCnt] = new byte[(((PopoutFrameModuleExpand.this.mAnimatedPictureSize[0] * PopoutFrameModuleExpand.this.mAnimatedPictureSize[1]) * 3) / 2)];
                    Bitmap bmp = (Bitmap) PopoutFrameModuleExpand.this.mCapturedDataQueue.take();
                    PopoutFrameModuleExpand.this.getNV21(PopoutFrameModuleExpand.this.mEncodingDataArray[PopoutFrameModuleExpand.this.mCurYuvDataCnt], PopoutFrameModuleExpand.this.mAnimatedPictureSize[0], PopoutFrameModuleExpand.this.mAnimatedPictureSize[1], bmp);
                    bmp.recycle();
                    PopoutFrameModuleExpand.this.mCurYuvDataCnt = PopoutFrameModuleExpand.this.mCurYuvDataCnt + 1;
                    CamLog.m3d(CameraConstants.TAG, "[popout] Take - end! + " + PopoutFrameModuleExpand.this.mCurYuvDataCnt);
                    if (PopoutFrameModuleExpand.this.mCurYuvDataCnt == 24) {
                        CamLog.m3d(CameraConstants.TAG, "[popout] Bitmap to YUV encoding - end");
                        PopoutFrameModuleExpand.this.mAnimatedPictureSaveTask = new AnimatedPictureTask(PopoutFrameModuleExpand.this, null);
                        PopoutFrameModuleExpand.this.mAnimatedPictureSaveTask.execute(new Void[0]);
                        PopoutFrameModuleExpand.this.mCurYuvDataCnt = 0;
                        return;
                    }
                } catch (InterruptedException e) {
                    CamLog.m3d(CameraConstants.TAG, "[popout] DataEncodingThread Interrupted");
                }
            }
            CamLog.m3d(CameraConstants.TAG, "[popout] Bitmap to YUV encoding - interruped");
            PopoutFrameModuleExpand.this.unbindCapturedData();
        }
    }

    public PopoutFrameModuleExpand(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mAnimatedPictureSaveTask != null && this.mAnimatedPictureSaveTask.getStatus() == Status.RUNNING) {
            showSavingDialog(true, 0);
        }
    }

    public void onPauseBefore() {
        if (this.mAnimatedPictureState == 2) {
            stopTakingAnimatedPicture(false);
        }
        super.onPauseBefore();
        this.mCleanViewByAction = false;
        setPopoutFrameGuideVisibility(false);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mAnimatedPictureState == 4) {
            Thread waitSavingPictureThread = new C04781();
            waitSavingPictureThread.start();
            try {
                waitSavingPictureThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean isFocusOnTouchEvent() {
        return false;
    }

    protected void showZoomBar() {
        if (this.mAnimatedPictureState != 4 && !this.mPopoutFrameManager.isNormalPreviewMoving()) {
            super.showZoomBar();
        }
    }

    public void onScaleGesture(int gapSpan, int totalSpan) {
        if (this.mAnimatedPictureState != 4) {
            super.onScaleGesture(gapSpan, totalSpan);
        }
    }

    public void showSavingDialog(boolean show, int delay) {
        if (this.mAnimatedPictureState != 4 || show) {
            super.showSavingDialog(show, delay);
        }
    }

    public void onGestureFlicking(MotionEvent e1, MotionEvent e2, int gestureType) {
        if (this.mAnimatedPictureState != 4) {
            super.onGestureFlicking(e1, e2, gestureType);
        }
    }

    public void onGestureCleanViewDetected() {
        if (this.mAnimatedPictureState != 4) {
            super.onGestureCleanViewDetected();
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mAnimatedPictureState != 1) {
            CamLog.m3d(CameraConstants.TAG, "[popout] Animated picture state : " + this.mAnimatedPictureState + ", so return");
            return false;
        }
        if (this.mPopoutFrameManager != null) {
            this.mPopoutFrameManager.hideSubWindowResizeHandler();
        }
        boolean result = super.onCameraShutterButtonClicked();
        if (isRotateDialogVisible()) {
            return result;
        }
        setPopoutFrameGuideViewVisibility(false);
        return result;
    }

    protected void doTakePicture() {
        super.doTakePicture();
        AppControlUtil.setPopoutFirstTakePicture(true);
    }

    public boolean onRecordStartButtonClicked() {
        setPopoutFrameGuideViewVisibility(false);
        return super.onRecordStartButtonClicked();
    }

    public synchronized void runStartRecorder(boolean useThread) {
        super.runStartRecorder(useThread);
        AppControlUtil.setPopoutFirstTakePicture(true);
    }

    public void onVideoStopClickedBefore() {
        if (this.mAnimatedPictureState == 1) {
            super.onVideoStopClickedBefore();
            if (this.mPopoutFrameManager != null) {
                this.mPopoutFrameManager.hideSubWindowResizeHandler();
            }
        }
    }

    protected void changeToAnimatedPictureUI() {
        if (this.mPopoutFrameManager != null) {
            this.mPopoutFrameManager.hideSubWindowResizeHandler();
        }
        if (this.mPopoutCameraManager != null) {
            setPopoutLayoutVisibility(false);
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonVisibility(8, getShutterButtonType(), false);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setThumbnailVisibility(8);
        }
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.setBackButton(false);
        }
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.backup();
            this.mQuickButtonManager.hide(false, false, false);
        }
        if (isRotateDialogVisible()) {
            removeRotateDialog();
        }
        access$400(CameraConstants.MENU_TYPE_ALL, false, true);
        setQuickClipIcon(true, false);
        hideZoomBar();
    }

    protected void changeToNomalUI() {
        if (this.mCameraState > 0) {
            setCameraState(1);
        }
        hideZoomBar();
        if (this.mPopoutCameraManager != null) {
            setPopoutLayoutVisibility(true);
        }
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.hide();
            this.mRecordingUIManager.destroyLayout();
            this.mRecordingUIManager.setRec3sec(false);
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonVisibility(0, getShutterButtonType(), false);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setThumbnailVisibility(0);
        }
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.setBackButton(false);
        }
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.restore(true);
            this.mQuickButtonManager.show(false, false, true);
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        if (!checkAvailableAnimatedPicture()) {
            return false;
        }
        if (this.mEncodingThread == null || !this.mEncodingThread.isAlive()) {
            CamLog.m3d(CameraConstants.TAG, "[popout] start taking animated photo");
            this.mCapturedDataQueue = new ArrayBlockingQueue(30);
            this.mEncodingDataArray = new byte[24][];
            this.mAnimatedPictureState = 2;
            this.mCurScreenCaptureCnt = 0;
            changeToAnimatedPictureUI();
            AudioUtil.setAudioFocus(getAppContext(), true);
            setCameraState(6);
            this.mRecordingUIManager.setRec3sec(true);
            this.mRecordingUIManager.initLayout();
            this.mRecordingUIManager.updateRecStatusIcon();
            this.mRecordingUIManager.show(access$900());
            playRecordingSound(true);
            doSnapshotEffect(true, 0.3f, 100);
            if (this.mPopoutFrameEngine != null) {
                this.mPopoutFrameEngine.pauseWideView();
            }
            TimerTask longShutter = new C04792();
            CamLog.m7i(CameraConstants.TAG, "[popout] screen capture timer duration :  " + 83);
            this.mAnimatedPictureTimer = new Timer("timer_animated_picture");
            this.mAnimatedPictureTimer.scheduleAtFixedRate(longShutter, 0, (long) 83);
            CamLog.m7i(CameraConstants.TAG, "[popout] make new encoding thread");
            this.mEncodingThread = new DataEncodingThread(this, null);
            this.mEncodingThread.start();
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[popout] Encoding thread is alive. so return");
        return false;
    }

    private boolean checkAvailableAnimatedPicture() {
        if (checkModuleValidate(223) && !isRotateDialogVisible() && this.mCameraDevice != null && ((this.mTimerManager == null || !this.mTimerManager.isTimerShotCountdown()) && (this.mReviewThumbnailManager == null || !this.mReviewThumbnailManager.isQuickViewAniStarted()))) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[popout] Cannot take animated picture");
        return false;
    }

    private synchronized void stopTakingAnimatedPicture(final boolean isSave) {
        CamLog.m3d(CameraConstants.TAG, "[popout] stopTakingAnimatedPicture, isSave : " + isSave);
        if (isSave) {
            playRecordingSound(false);
        }
        if (this.mPopoutFrameEngine != null) {
            this.mPopoutFrameEngine.resumeWideView();
        }
        if (this.mAnimatedPictureTimer != null) {
            this.mAnimatedPictureTimer.cancel();
            this.mAnimatedPictureTimer.purge();
            this.mAnimatedPictureTimer = null;
        }
        if (!isSave) {
            if (this.mEncodingThread != null) {
                this.mEncodingThread.interrupt();
            }
            if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(getAppContext(), false);
            }
            setQuickClipIcon(true, true);
        }
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (PopoutFrameModuleExpand.this.mCaptureButtonManager != null) {
                    PopoutFrameModuleExpand.this.mCaptureButtonManager.setShutterButtonPressed(false, 2);
                }
                if (isSave) {
                    AppControlUtil.setPopoutFirstTakePicture(true);
                }
                PopoutFrameModuleExpand.this.changeToNomalUI();
                if (isSave && PopoutFrameModuleExpand.this.mAnimatedPictureState == 2) {
                    PopoutFrameModuleExpand.this.mAnimatedPictureState = 4;
                    PopoutFrameModuleExpand.this.showSavingDialog(true, 0);
                }
            }
        });
    }

    protected void setPopoutFrameGuideVisibility(boolean show) {
        if (!show || VideoRecorder.isRecording() || !checkModuleValidate(64)) {
            View mGuidView = findViewById(C0088R.id.popout_frame_guide_layout);
            if (mGuidView != null) {
                ((ViewGroup) mGuidView.getParent()).removeView(mGuidView);
            }
        } else if (findViewById(C0088R.id.popout_frame_guide_layout) == null) {
            this.mPopoutFrameBaseView = this.mGet.layoutInflate(C0088R.layout.popout_animated_photo_guide, (FrameLayout) findViewById(C0088R.id.contents_base));
            TextView mGuideView = (TextView) findViewById(C0088R.id.popout_frame_guide_layout_textview);
            if (mGuideView != null) {
                String mChangeString = "(###)";
                String mTextString = (String) mGuideView.getText();
                mGuideView.setContentDescription(mTextString.replace(mChangeString, this.mGet.getAppContext().getString(C0088R.string.shutter_button)));
                int spanStartIndex = mTextString.indexOf(mChangeString);
                int spanEndIndex = spanStartIndex + mChangeString.length();
                SpannableString ss = new SpannableString(mTextString);
                Drawable d = getAppContext().getResources().getDrawable(C0088R.drawable.camera_guide_spannable_shutter);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                ss.setSpan(new ShiftImageSpan(this.mGet.getAppContext(), d, 1), spanStartIndex, spanEndIndex, 17);
                mGuideView.setText(ss);
                mGuideView.append("\n ");
                updatePopoutFrameGuideViewDegree(getOrientationDegree());
            }
        }
    }

    protected void setPopoutFrameGuideViewVisibility(boolean show) {
        CamLog.m3d(CameraConstants.TAG, "setPopoutFrameGuideViewVisibility : " + show);
        if (this.mPopoutFrameBaseView != null) {
            updatePopoutFrameGuideViewDegree(getOrientationDegree());
            int visibility = (!show || VideoRecorder.isRecording() || AppControlUtil.isPopoutFirstTakePicture() || !checkModuleValidate(64)) ? 8 : 0;
            View guideTextView = this.mPopoutFrameBaseView.findViewById(C0088R.id.popout_frame_guide_layout_textview);
            if (guideTextView != null) {
                guideTextView.setVisibility(visibility);
            }
        }
    }

    public void updatePopoutFrameGuideViewDegree(int degree) {
        RotateLayout mGuideTextLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.popout_frame_guide_layout_rotate);
        TextView textView = (TextView) this.mGet.findViewById(C0088R.id.popout_frame_guide_layout_textview);
        if (mGuideTextLayout != null && textView != null) {
            mGuideTextLayout.rotateLayout(degree);
            LayoutParams lp = (LayoutParams) mGuideTextLayout.getLayoutParams();
            LayoutParams textLp = (LayoutParams) textView.getLayoutParams();
            textLp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.popout_guide_text_port_width);
            Utils.resetLayoutParameter(lp);
            switch (Utils.convertDegree(this.mGet.getAppContext().getResources(), degree)) {
                case 0:
                    lp.addRule(12, 1);
                    lp.addRule(14, 1);
                    lp.bottomMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_guid_text_port_marginBottom) - textView.getLineHeight();
                    break;
                case 90:
                    lp.addRule(21, 1);
                    lp.addRule(15, 1);
                    lp.setMarginEnd(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.popout_guide_text_land_reverse_marginBottom) - textView.getLineHeight());
                    textLp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_guid_text_land_width);
                    break;
                case 180:
                    lp.addRule(12, 1);
                    lp.addRule(14, 1);
                    lp.bottomMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_guid_text_port_marginBottom);
                    break;
                case 270:
                    lp.addRule(21, 1);
                    lp.addRule(15, 1);
                    lp.setMarginEnd(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.popout_guide_text_land_marginBottom));
                    textLp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_guid_text_land_width);
                    break;
            }
            mGuideTextLayout.setLayoutParams(lp);
            textView.setLayoutParams(textLp);
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (this.mTimerManager.isTimerShotCountdown() || isModeMenuVisible() || isSettingMenuVisible() || isZoomBarVisible() || isHelpListVisible()) {
            setPopoutFrameGuideViewVisibility(false);
            return;
        }
        setPopoutFrameGuideViewVisibility(true);
        updatePopoutFrameGuideViewDegree(degree);
        setVisibleGuideTextforQuickClip();
    }

    protected void setVisibleGuideTextforQuickClip() {
        if (isSupportedQuickClip() && !this.mCleanViewByAction && !isModeMenuVisible() && !isHelpListVisible()) {
            boolean isPortrait;
            if (getOrientationDegree() == 0 || getOrientationDegree() == 180) {
                isPortrait = true;
            } else {
                isPortrait = false;
            }
            if ((isPortrait && this.mQuickClipManager.isOpened()) || isTimerShotCountdown()) {
                setPopoutFrameGuideViewVisibility(false);
            } else {
                setPopoutFrameGuideViewVisibility(true);
            }
        }
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (doByAction) {
            this.mCleanViewByAction = true;
            setPopoutFrameGuideViewVisibility(false);
        } else {
            setPopoutFrameGuideViewVisibility(true);
        }
        super.doCleanView(doByAction, useAnimation, saveState);
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (checkModuleValidate(128)) {
            setPopoutFrameGuideVisibility(true);
            setPopoutFrameGuideViewVisibility(true);
        }
    }

    public void onZoomHide() {
        super.onZoomHide();
        if (this.mTimerManager != null && !this.mTimerManager.isTimerShotCountdown() && !isModeMenuVisible() && !isSettingMenuVisible() && !isHelpListVisible()) {
            setPopoutFrameGuideViewVisibility(true);
        }
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (!isRecordingState()) {
            setPopoutFrameGuideViewVisibility(false);
        }
    }

    protected void setPopoutLayoutVisibility(boolean visible) {
        super.setPopoutLayoutVisibility(visible);
        setPopoutFrameGuideVisibility(visible);
        setPopoutFrameGuideViewVisibility(visible);
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        switch (msg.what) {
            case 71:
                this.mRecordingUIManager.getArcProgress().updateArcProgress(2);
                return true;
            default:
                return super.mainHandlerHandleMessage(msg);
        }
    }

    public boolean doBackKey() {
        CamLog.m3d(CameraConstants.TAG, "[popout] doBackKey");
        if (this.mAnimatedPictureState == 2) {
            stopTakingAnimatedPicture(false);
            return true;
        }
        if (this.mAnimatedPictureSaveTask != null && this.mAnimatedPictureSaveTask.getStatus() == Status.RUNNING) {
            this.mAnimatedPictureSaveTask.cancel(true);
            this.mAnimatedPictureSaveTask = null;
            this.mAnimatedPictureState = 1;
        }
        return super.doBackKey();
    }

    private void unbindCapturedData() {
        if (!(this.mCapturedDataQueue == null || this.mCapturedDataQueue.isEmpty())) {
            CamLog.m3d(CameraConstants.TAG, "[popout] unbindCapturedData, remain data : " + this.mCapturedDataQueue.size());
            for (int i = 0; i < this.mCapturedDataQueue.size(); i++) {
                ((Bitmap) this.mCapturedDataQueue.peek()).recycle();
            }
            this.mCapturedDataQueue.clear();
            this.mCapturedDataQueue = null;
        }
        this.mCurYuvDataCnt = 0;
        this.mEncodingDataArray = (byte[][]) null;
        this.mEncodingThread = null;
        this.mAnimatedPictureState = 1;
        showSavingDialog(false, 0);
    }

    private void getNV21(byte[] frameData, int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[(inputWidth * inputHeight)];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        ColorConverter.RGBToYuv420sp(argb, frameData, inputWidth, inputHeight);
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        setPopoutLayoutVisibility(true);
        return true;
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        setPopoutLayoutVisibility(false);
    }
}
