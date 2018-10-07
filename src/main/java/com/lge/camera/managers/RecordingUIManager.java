package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.p000v4.view.GravityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Space;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.ArcProgress;
import com.lge.camera.components.RecProgressBar;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.io.File;

public class RecordingUIManager extends ManagerInterfaceImpl {
    public static final float ATTACH_PROGRESS_BAR_ADJUST_RATIO = 0.02f;
    private ArcProgress mArcProgress = null;
    private long mBlinkSecondBackup = 0;
    private String mCurRecTimeString = "0";
    private String mCurTimrLapseRecTimeString = "0";
    private long mEndTime = 0;
    private RecorderInterface mInterface = null;
    private boolean mIsCineEffectOn = false;
    private boolean mIsRec3sec = false;
    private long mPauseTime = 0;
    private RecProgressBar mRecProgressBar = null;
    private View mRecView = null;
    private long mRecordingTime = 0;
    private long mStartTime = 0;
    private final int mThousand = 1000;
    private int mTimeLapseSpeedValue = 0;

    public interface RecorderInterface {
        String getFilePath();

        int getMaxDuration();

        long getMaxFileSize();
    }

    public RecordingUIManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setRecordingInterface(RecorderInterface iRef) {
        this.mInterface = iRef;
    }

    public void initLayout() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mRecView = this.mGet.inflateView(C0088R.layout.rec_indicator);
        if (this.mRecView != null) {
            if (vg != null) {
                vg.addView(this.mRecView, 0, new LayoutParams(-1, -1));
            }
            this.mRecProgressBar = (RecProgressBar) this.mRecView.findViewById(C0088R.id.progress_rec_time);
            if (this.mRecProgressBar != null && this.mGet.isAttachIntent()) {
                int marginBottom = Utils.getPx(getAppContext(), C0088R.dimen.progress_rec.marginBottom);
                int marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ModelProperties.getLCDType() > 1 ? 0.23f : 0.289f);
                if (!ModelProperties.isLongLCDModel()) {
                    marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.232f);
                }
                this.mRecProgressBar.setTopPadding(marginEnd + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.02f), marginBottom);
            }
            setTextRecordingIndicator(0, 0, 0, 0);
            this.mArcProgress = (ArcProgress) this.mRecView.findViewById(C0088R.id.arc_process);
            this.mArcProgress.setHandler(this.mGet.getHandler());
            Space dummyIconSpace = (Space) this.mRecView.findViewById(C0088R.id.dummy_icon_space);
            dummyIconSpace.setMinimumWidth(this.mGet.getActivity().getDrawable(C0088R.drawable.ic_camera_rec).getIntrinsicWidth());
            dummyIconSpace.setVisibility(0);
            setRotateDegree(270, false);
        }
    }

    private void showCineVideo() {
        TextView cineVideo = (TextView) this.mRecView.findViewById(C0088R.id.text_rec_mode);
        String text = "";
        if (FunctionProperties.isSupportedHDR10()) {
            text = this.mGet.getAppContext().getString(C0088R.string.hdr10) + "\n";
        }
        if (cineVideo != null && this.mIsCineEffectOn) {
            text = text + this.mGet.getAppContext().getString(C0088R.string.initial_guide_title_cine_effect) + " " + this.mGet.getAppContext().getString(C0088R.string.on);
        }
        cineVideo.setText(text);
        cineVideo.setVisibility(0);
    }

    public void setCineEffectOn(boolean effectOn) {
        this.mIsCineEffectOn = effectOn;
    }

    private void showTimeLapse() {
        TextView timeLapsView = (TextView) this.mRecView.findViewById(C0088R.id.text_rec_mode);
        if (timeLapsView != null && getTimeLapseSpeedValue() >= 0) {
            timeLapsView.setText(this.mGet.getAppContext().getString(C0088R.string.time_lapse_video) + " (x" + getTimeLapseSpeedValue() + ")");
            timeLapsView.setVisibility(0);
        }
    }

    public void setTimeLapseSpeedValue(int speedValue) {
        this.mTimeLapseSpeedValue = speedValue;
    }

    public int getTimeLapseSpeedValue() {
        return this.mTimeLapseSpeedValue;
    }

    private void showSnapMovie() {
        RotateTextView snapMovieView = (RotateTextView) this.mRecView.findViewById(C0088R.id.arc_progress_text);
        if (snapMovieView != null) {
            snapMovieView.setText(null);
            snapMovieView.setVisibility(0);
        }
    }

    public void show(boolean isMMS) {
        if (this.mRecView != null) {
            int visible;
            this.mRecView.findViewById(C0088R.id.rec_time_indicator).setVisibility(0);
            this.mRecProgressBar = (RecProgressBar) this.mRecView.findViewById(C0088R.id.progress_rec_time);
            if (isMMS) {
                visible = 0;
            } else {
                visible = 8;
            }
            this.mRecProgressBar.setVisibility(visible);
            if (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode())) {
                showCineVideo();
            } else if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(this.mGet.getShotMode())) {
                showTimeLapse();
            } else if (CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode())) {
                showSnapMovie();
            } else if (!this.mGet.isManualMode()) {
                TextView slowMotionView = (TextView) this.mRecView.findViewById(C0088R.id.text_rec_mode);
                String slowMotion = this.mGet.getAppContext().getString(C0088R.string.slow_motion);
                if (!(slowMotionView == null || slowMotion == null)) {
                    if (CameraConstants.MODE_SLOW_MOTION.equals(this.mGet.getShotMode())) {
                        CamLog.m3d(CameraConstants.TAG, "is slow motion mode.");
                        slowMotionView.setText(this.mGet.getAppContext().getString(C0088R.string.slo_mo));
                        slowMotionView.setVisibility(0);
                    } else {
                        slowMotionView.setVisibility(8);
                    }
                }
            }
            updateRecTimeLayout();
        }
    }

    public void hide() {
        if (this.mRecView != null) {
            this.mRecView.findViewById(C0088R.id.rec_time_indicator).setVisibility(8);
            this.mRecView.findViewById(C0088R.id.text_rec_mode).setVisibility(8);
            this.mRecView.findViewById(C0088R.id.arc_process).setVisibility(8);
        }
        if (this.mRecProgressBar != null) {
            this.mRecProgressBar.setVisibility(8);
        }
    }

    public void destroyLayout() {
        if (this.mRecView != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (!(vg == null || this.mRecView == null)) {
                vg.removeView(this.mRecView);
                MemoryUtils.releaseViews(this.mRecView);
            }
            this.mRecView = null;
        }
        this.mRecProgressBar = null;
    }

    public void updateUIRecordingTime(boolean isLimitReached, long seconds, boolean isAttachMode, int cameraState) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (60 * hours);
        long remainderSeconds = seconds - (60 * minutes);
        if (cameraState == 6) {
            recordingIconBlink(remainderSeconds);
            setTextRecordingIndicator(minutes, hours, remainderMinutes, remainderSeconds);
            showProgress(isLimitReached, seconds, isAttachMode);
        }
    }

    public void updateTimeLapseUIRecordingTime(long seconds, int cameraState) {
        long tlseconds = seconds / ((long) getTimeLapseSpeedValue());
        long tlminutes = tlseconds / 60;
        long tlhours = tlminutes / 60;
        long tlremainderMinutes = tlminutes - (60 * tlhours);
        long tlremainderSeconds = tlseconds - (60 * tlminutes);
        if (cameraState == 6) {
            setTimeLapseTextRecordingIndicator(tlminutes, tlhours, tlremainderMinutes, tlremainderSeconds);
        }
    }

    public void updateUIManualRecordingTime(boolean isLimitReached, long milliseconds, boolean isAttachMode, int cameraState) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (60 * hours);
        long remainderSeconds = seconds - (60 * minutes);
        long remainderMilliseconds = milliseconds % 1000;
        if (cameraState == 6) {
            manualRecordingIconBlink(remainderSeconds);
            setTextManualRecordingIndicator(seconds, minutes, hours, remainderMinutes, remainderSeconds, remainderMilliseconds);
        }
    }

    public int showProgress(boolean isLimitReached, long seconds, boolean isAttachMode) {
        if (this.mGet.checkModuleValidate(128) || this.mInterface == null || this.mRecProgressBar == null) {
            return 0;
        }
        int progress = 0;
        long remainderSeconds = seconds - (60 * (seconds / 60));
        int recordingDurationLimit = this.mInterface.getMaxDuration();
        long recordingSizeLimit = this.mInterface.getMaxFileSize();
        boolean isMMSIntent = this.mGet.isMMSIntent();
        boolean isAttachIntent = this.mGet.isAttachIntent();
        if ((isMMSIntent || (!isAttachIntent && isAttachMode)) && ModelProperties.getCarrierCode() == 6) {
            if (isLimitReached) {
                progress = this.mRecProgressBar.getMax();
                CamLog.m3d(CameraConstants.TAG, "Limit reached! barmax:" + progress);
            } else {
                progress = (int) ((((float) remainderSeconds) / ((float) (MultimediaProperties.getMMSMaxDuration() / 1000))) * ((float) this.mRecProgressBar.getMax()));
                if (remainderSeconds == 0 && progress != 0) {
                    progress = this.mRecProgressBar.getMax();
                }
            }
            this.mRecProgressBar.setProgress(progress);
            return progress;
        }
        File videoFile;
        if (isMMSIntent || (isAttachMode && recordingSizeLimit != 0 && (recordingDurationLimit == 0 || recordingDurationLimit == 7200000))) {
            if (this.mInterface.getFilePath() == null) {
                return 0;
            }
            videoFile = new File(this.mInterface.getFilePath());
            long videoFileSize;
            if (isLimitReached) {
                videoFileSize = recordingSizeLimit;
                progress = this.mRecProgressBar.getMax();
                CamLog.m3d(CameraConstants.TAG, "Limit reached! barmax:" + progress);
            } else if (videoFile.exists()) {
                videoFileSize = videoFile.length();
                CamLog.m3d(CameraConstants.TAG, "FileSize: " + videoFileSize);
                progress = (int) ((((long) this.mRecProgressBar.getMax()) * videoFileSize) / recordingSizeLimit);
            }
            this.mRecProgressBar.setProgress(progress);
        } else if (isAttachMode && recordingDurationLimit != 0 && recordingSizeLimit == 0) {
            if (1000 * seconds >= ((long) recordingDurationLimit)) {
                progress = this.mRecProgressBar.getMax();
                CamLog.m3d(CameraConstants.TAG, "Limit reached! barmax:" + progress);
            } else {
                CamLog.m3d(CameraConstants.TAG, "rectime: " + seconds);
                progress = (int) (((1000 * seconds) * ((long) this.mRecProgressBar.getMax())) / ((long) recordingDurationLimit));
            }
            this.mRecProgressBar.setProgress(progress);
        } else if (isAttachMode && (recordingDurationLimit != 0 || (recordingDurationLimit == 7200000 && recordingSizeLimit != 0))) {
            if (1000 * seconds >= ((long) recordingDurationLimit) || isLimitReached) {
                progress = this.mRecProgressBar.getMax();
            } else {
                videoFile = new File(this.mInterface.getFilePath());
                if (videoFile == null || !videoFile.exists()) {
                    return 0;
                }
                int progressBySize = (int) ((((long) this.mRecProgressBar.getMax()) * videoFile.length()) / recordingSizeLimit);
                int progressByDuration = (int) (((1000 * seconds) * ((long) this.mRecProgressBar.getMax())) / ((long) recordingDurationLimit));
                if (progressBySize > progressByDuration) {
                    progress = progressBySize;
                } else {
                    progress = progressByDuration;
                }
            }
            this.mRecProgressBar.setProgress(progress);
        }
        if (!isLimitReached || this.mRecProgressBar == null) {
            return progress;
        }
        this.mRecProgressBar.invalidate();
        return progress;
    }

    public void recordingIconBlink(long seconds) {
        if (this.mRecView != null) {
            if (!CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode()) || !this.mIsRec3sec) {
                if (!CameraConstants.MODE_MULTIVIEW.equals(this.mGet.getShotMode()) || !this.mIsRec3sec) {
                    if (!CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode()) || !this.mIsRec3sec) {
                        if (!CameraConstants.MODE_SQUARE_GRID.equals(this.mGet.getShotMode()) || !this.mIsRec3sec) {
                            if (!CameraConstants.MODE_POPOUT_CAMERA.equals(this.mGet.getShotMode()) || !this.mIsRec3sec) {
                                ImageView recIcon = (ImageView) this.mRecView.findViewById(C0088R.id.rec_status_icon);
                                if (recIcon != null) {
                                    recIcon.setImageResource(C0088R.drawable.ic_camera_rec);
                                    if (seconds == 0) {
                                        recIcon.setVisibility(0);
                                    } else if (recIcon.getVisibility() != 0) {
                                        recIcon.setVisibility(0);
                                    } else {
                                        recIcon.setVisibility(4);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void manualRecordingIconBlink(long seconds) {
        if (this.mRecView != null) {
            ImageView recIcon = (ImageView) this.mRecView.findViewById(C0088R.id.rec_status_icon);
            if (recIcon != null) {
                recIcon.setImageResource(C0088R.drawable.ic_camera_rec);
                if (seconds == 0) {
                    recIcon.setVisibility(0);
                } else if (this.mBlinkSecondBackup != seconds) {
                    this.mBlinkSecondBackup = seconds;
                    if (recIcon.getVisibility() != 0) {
                        recIcon.setVisibility(0);
                    } else {
                        recIcon.setVisibility(4);
                    }
                }
            }
        }
    }

    public void setTextRecordingIndicator(long minutes, long hours, long remainderMinutes, long remainderSeconds) {
        if (this.mRecView != null) {
            String talkBackText;
            RotateTextView recTimeText = (RotateTextView) this.mRecView.findViewById(this.mIsRec3sec ? C0088R.id.arc_progress_text : C0088R.id.text_rec_time);
            recTimeText.setTypeface(Typeface.DEFAULT);
            if ((this.mGet.isMMSIntent() || this.mGet.isAttachResol()) && ModelProperties.getCarrierCode() == 6) {
                if (remainderMinutes == 1) {
                    remainderSeconds = 60;
                }
                this.mCurRecTimeString = String.format("%02d/%d", new Object[]{Long.valueOf(remainderSeconds), Integer.valueOf(60)});
                talkBackText = TalkBackUtil.getSeconds(getAppContext(), (int) remainderSeconds, true) + "/" + TalkBackUtil.getSeconds(getAppContext(), 60, false);
            } else if (CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode()) || ((CameraConstants.MODE_MULTIVIEW.equals(this.mGet.getShotMode()) && this.mIsRec3sec) || ((CameraConstants.MODE_SQUARE_GRID.equals(this.mGet.getShotMode()) && this.mIsRec3sec) || (CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode()) && this.mIsRec3sec)))) {
                talkBackText = setTextRecordingIndicatorForMode(remainderMinutes, remainderSeconds);
            } else {
                this.mCurRecTimeString = String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(hours), Long.valueOf(remainderMinutes), Long.valueOf(remainderSeconds)});
                talkBackText = TalkBackUtil.getHours(getAppContext(), (int) hours) + TalkBackUtil.getMinutes(getAppContext(), (int) remainderMinutes) + TalkBackUtil.getSeconds(getAppContext(), (int) remainderSeconds, true);
            }
            recTimeText.setText(this.mCurRecTimeString);
            recTimeText.setContentDescription(talkBackText);
        }
    }

    private String setTextRecordingIndicatorForMode(long remainderMinutes, long remainderSeconds) {
        if (this.mIsRec3sec) {
            long j;
            if (remainderSeconds > ((long) 3)) {
                j = (long) 3;
            } else {
                j = remainderSeconds;
            }
            this.mCurRecTimeString = String.format("%d", new Object[]{Integer.valueOf((int) j)});
            return TalkBackUtil.getSeconds(getAppContext(), (int) j, true);
        }
        if (remainderMinutes > 0 && CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode())) {
            remainderSeconds = 0;
        }
        this.mCurRecTimeString = String.format("%d:%02d", new Object[]{Long.valueOf(remainderMinutes), Long.valueOf(remainderSeconds)});
        return TalkBackUtil.getMinutes(getAppContext(), (int) remainderMinutes) + TalkBackUtil.getSeconds(getAppContext(), (int) remainderSeconds, true);
    }

    public void setTimeLapseTextRecordingIndicator(long tlminutes, long tlhours, long tlremainderMinutes, long tlremainderSeconds) {
        if (this.mRecView != null) {
            RotateTextView recTimeLapseTimeText = (RotateTextView) this.mRecView.findViewById(C0088R.id.text_timelapse_rec_time);
            ((ImageView) this.mRecView.findViewById(C0088R.id.rec_timelapse_arrow)).setVisibility(0);
            recTimeLapseTimeText.setVisibility(0);
            recTimeLapseTimeText.setTypeface(Typeface.DEFAULT);
            this.mCurTimrLapseRecTimeString = String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(tlhours), Long.valueOf(tlremainderMinutes), Long.valueOf(tlremainderSeconds)});
            String talkBackText = TalkBackUtil.getHours(getAppContext(), (int) tlhours) + TalkBackUtil.getMinutes(getAppContext(), (int) tlremainderMinutes) + TalkBackUtil.getSeconds(getAppContext(), (int) tlremainderSeconds, true);
            recTimeLapseTimeText.setText(this.mCurTimrLapseRecTimeString);
            recTimeLapseTimeText.setContentDescription(talkBackText);
        }
    }

    public void setTextManualRecordingIndicator(long seconds, long minutes, long hours, long remainderMinutes, long remainderSeconds, long remainderMilliseconds) {
        if (this.mRecView != null) {
            RotateTextView recTimeText = (RotateTextView) this.mRecView.findViewById(C0088R.id.text_rec_time);
            recTimeText.setTypeface(Typeface.DEFAULT);
            recTimeText.setMinimumWidth(recTimeText.getWidth() + Utils.getPx(getAppContext(), C0088R.dimen.rec_timelapse_arrow.marginEnd));
            this.mCurRecTimeString = String.format("%02d:%02d:%02d:%03d", new Object[]{Long.valueOf(hours), Long.valueOf(remainderMinutes), Long.valueOf(remainderSeconds), Long.valueOf(remainderMilliseconds)});
            recTimeText.setText(this.mCurRecTimeString, false);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mRecView == null)) {
            vg.removeView(this.mRecView);
            MemoryUtils.releaseViews(this.mRecView);
            if (!this.mGet.checkModuleValidate(128)) {
                this.mRecView = this.mGet.inflateView(C0088R.layout.rec_indicator);
                vg.addView(this.mRecView, 0, new LayoutParams(-1, -1));
                updateRecStatusIcon();
                udpateCurrentRecTime();
            }
        }
        super.onConfigurationChanged(config);
    }

    public void updateRecStatusIcon() {
        if (this.mRecView != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (RecordingUIManager.this.mRecView != null) {
                        ImageView recIcon = (ImageView) RecordingUIManager.this.mRecView.findViewById(C0088R.id.rec_status_icon);
                        ArcProgress arcRecIcon = (ArcProgress) RecordingUIManager.this.mRecView.findViewById(C0088R.id.arc_process);
                        switch (RecordingUIManager.this.mGet.getCameraState()) {
                            case 6:
                                if ((!CameraConstants.MODE_SNAP.equals(RecordingUIManager.this.mGet.getShotMode()) || !RecordingUIManager.this.mIsRec3sec) && ((!CameraConstants.MODE_MULTIVIEW.equals(RecordingUIManager.this.mGet.getShotMode()) || !RecordingUIManager.this.mIsRec3sec) && ((!CameraConstants.MODE_SQUARE_GRID.equals(RecordingUIManager.this.mGet.getShotMode()) || !RecordingUIManager.this.mIsRec3sec) && ((!CameraConstants.MODE_SQUARE_SPLICE.equals(RecordingUIManager.this.mGet.getShotMode()) || !RecordingUIManager.this.mIsRec3sec) && (!CameraConstants.MODE_POPOUT_CAMERA.equals(RecordingUIManager.this.mGet.getShotMode()) || !RecordingUIManager.this.mIsRec3sec))))) {
                                    recIcon.setImageResource(C0088R.drawable.ic_camera_rec);
                                    recIcon.setVisibility(4);
                                    return;
                                } else if (arcRecIcon.isRotateAnimationPasued()) {
                                    arcRecIcon.resumeRotateAnimation();
                                    return;
                                } else if (arcRecIcon.isRotateAnimationStop()) {
                                    arcRecIcon.setVisibility(0);
                                    recIcon.setVisibility(4);
                                    arcRecIcon.startRotateAnimation();
                                    return;
                                } else {
                                    return;
                                }
                            case 7:
                                recIcon.setImageResource(C0088R.drawable.ic_camera_pause);
                                recIcon.setVisibility(0);
                                return;
                            default:
                                recIcon.setVisibility(4);
                                arcRecIcon.stopRotateAnimation();
                                arcRecIcon.setVisibility(4);
                                arcRecIcon.setVisibility(4);
                                return;
                        }
                    }
                }
            });
        }
    }

    public void udpateCurrentRecTime() {
        if (this.mRecView != null) {
            RotateTextView recTimeText = (RotateTextView) this.mRecView.findViewById(C0088R.id.text_rec_time);
            if (recTimeText != null) {
                recTimeText.setTypeface(Typeface.DEFAULT);
                recTimeText.setVisibility(0);
                recTimeText.setText(this.mCurRecTimeString);
            }
        }
    }

    public void showHdr10Text() {
        if (this.mRecView != null) {
            TextView hdr10 = (TextView) this.mRecView.findViewById(C0088R.id.text_rec_mode);
            hdr10.setText(this.mGet.getAppContext().getString(C0088R.string.hdr10));
            hdr10.setVisibility(0);
        }
    }

    public void initVideoTime() {
        this.mStartTime = 0;
        this.mPauseTime = 0;
        this.mEndTime = 0;
        this.mRecordingTime = 0;
    }

    public void updateVideoTime(int timeType, long time) {
        switch (timeType) {
            case 1:
                this.mStartTime = time;
                return;
            case 2:
                this.mPauseTime = time;
                return;
            case 3:
                this.mEndTime = time;
                return;
            case 4:
                this.mRecordingTime = time;
                return;
            default:
                return;
        }
    }

    public long getVideoTime(int timeType) {
        switch (timeType) {
            case 1:
                return this.mStartTime;
            case 2:
                return this.mPauseTime;
            case 3:
                return this.mEndTime;
            case 4:
                return this.mRecordingTime;
            default:
                return 0;
        }
    }

    public void setRecDurationTime(long compensationTime) {
        if (this.mStartTime <= 0) {
            this.mStartTime = this.mEndTime;
        }
        if (this.mPauseTime > 0) {
            this.mStartTime += (this.mEndTime - this.mPauseTime) + compensationTime;
            this.mPauseTime = 0;
            this.mStartTime = Math.max(this.mStartTime, 0);
        }
        this.mRecordingTime = Math.max(this.mEndTime - this.mStartTime, 0);
    }

    public boolean checkMinRecTime() {
        long duration = this.mRecordingTime;
        if (VideoRecorder.getLoopState() != 0) {
            duration = VideoRecorder.getLoopRecordingDuration();
        }
        CamLog.m3d(CameraConstants.TAG, "duration = " + duration);
        return duration >= ((long) MultimediaProperties.getMinRecordingTime());
    }

    public boolean checkMinRecTime(long minTime) {
        return this.mRecordingTime >= minTime;
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mRecView != null) {
            RotateLayout rectimeRotate = (RotateLayout) this.mRecView.findViewById(C0088R.id.text_rec_time_rotate);
            RotateLayout progressRotate = (RotateLayout) this.mRecView.findViewById(C0088R.id.progress_rec_time_rotate);
            RelativeLayout recTimeIndicator = (RelativeLayout) this.mRecView.findViewById(C0088R.id.rec_time_indicator);
            RotateTextView recTimeText = (RotateTextView) this.mRecView.findViewById(C0088R.id.text_rec_time);
            RelativeLayout recModeView = (RelativeLayout) this.mRecView.findViewById(C0088R.id.text_rec_mode_view);
            TextView textRecMode = (TextView) this.mRecView.findViewById(C0088R.id.text_rec_mode);
            RelativeLayout recTimeView = (RelativeLayout) this.mRecView.findViewById(C0088R.id.text_rec_time_view);
            RotateLayout arcProgressRotate = (RotateLayout) this.mRecView.findViewById(C0088R.id.arc_progress_rotate_layout);
            if (rectimeRotate != null && progressRotate != null && recTimeIndicator != null && recTimeText != null && recModeView != null && textRecMode != null && recTimeView != null && arcProgressRotate != null) {
                LayoutParams timeIndicatorParams = (LayoutParams) recTimeIndicator.getLayoutParams();
                if (timeIndicatorParams != null) {
                    int topMargin = getRotateLayoutMargin(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
                    if (!rotateManualRecordingUI(degree)) {
                        rectimeRotate.rotateLayout(degree);
                        recTimeText.measure(0, 0);
                        int topMarginPort = (RatioCalcUtil.getQuickButtonWidth(getAppContext()) - recTimeText.getMeasuredHeight()) / 2;
                        if (ModelProperties.getLCDType() == 2) {
                            topMarginPort -= RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext()) / 2;
                        }
                        timeIndicatorParams.bottomMargin = topMarginPort;
                        int gravity = (degree == 270 || degree == 180) ? GravityCompat.START : GravityCompat.END;
                        recTimeView.setGravity(gravity);
                        recModeView.setGravity(gravity);
                        textRecMode.setGravity(gravity);
                        if (degree == 270 || degree == 90) {
                            topMargin += Utils.getPx(getAppContext(), C0088R.dimen.recording_indicator.marginTop_land);
                            timeIndicatorParams.setMarginEnd(Utils.getPx(getAppContext(), C0088R.dimen.recording_indicator.marginEnd_land));
                        } else {
                            topMargin += topMarginPort;
                            timeIndicatorParams.setMarginEnd(Utils.getPx(getAppContext(), C0088R.dimen.recording_indicator.marginEnd));
                        }
                        timeIndicatorParams.topMargin = topMargin;
                        if (CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode()) && this.mIsRec3sec) {
                            recTimeText.setVisibility(4);
                            rectimeRotate.setVisibility(4);
                        }
                    }
                    if (this.mRecProgressBar != null) {
                        this.mRecProgressBar.setDegree(degree);
                    }
                    this.mRecView.findViewById(C0088R.id.rec_time_indicator).setLayoutParams(timeIndicatorParams);
                    progressRotate.rotateLayout(degree);
                    LayoutParams lp = (LayoutParams) arcProgressRotate.getLayoutParams();
                    if (lp != null) {
                        if (ModelProperties.getLCDType() != 2) {
                            lp.topMargin = (RatioCalcUtil.getQuickButtonWidth(getAppContext()) - Utils.getPx(getAppContext(), C0088R.dimen.arc_progress_height)) / 2;
                        } else if (CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode())) {
                            lp.topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), 16, 9, 0) + ((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0468f) - Utils.getPx(getAppContext(), C0088R.dimen.arc_progress_height)) / 2);
                        } else {
                            lp.topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), 18, 9, 0) + ((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0468f) - Utils.getPx(getAppContext(), C0088R.dimen.arc_progress_height)) / 2);
                        }
                        arcProgressRotate.setLayoutParams(lp);
                    }
                    arcProgressRotate.rotateLayout(degree);
                }
            }
        }
    }

    private int getRotateLayoutMargin(String settingKey) {
        int topMargin = 0;
        if (!this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            if (ModelProperties.isLongLCDModel()) {
                int[] size = Utils.sizeStringToArray(this.mGet.getSettingValue(settingKey));
                if (this.mGet.isStickerSelected()) {
                    size = Utils.sizeStringToArray(this.mGet.getSettingValue("picture-size"));
                }
                topMargin = 0 + RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), size[0], size[1], 0);
            }
            return topMargin;
        } else if (ModelProperties.getLCDType() == 2) {
            return RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        } else {
            return 0;
        }
    }

    private boolean rotateManualRecordingUI(int degree) {
        if (!ManualUtil.isManualVideoMode(this.mGet.getShotMode())) {
            return false;
        }
        RotateLayout rectimeRotate = (RotateLayout) this.mRecView.findViewById(C0088R.id.text_rec_time_rotate);
        RotateTextView recTimeText = (RotateTextView) this.mRecView.findViewById(C0088R.id.text_rec_time);
        TextView recModeText = (TextView) this.mRecView.findViewById(C0088R.id.text_rec_mode);
        RelativeLayout recTimeView = (RelativeLayout) this.mRecView.findViewById(C0088R.id.text_rec_time_view);
        if (rectimeRotate == null || recTimeText == null || recTimeView == null || recModeText == null) {
            return false;
        }
        if (degree == 90 || degree == 270) {
            rectimeRotate.rotateLayout(degree);
        }
        int currentLayoutAngle = rectimeRotate.getAngle();
        int gravity = GravityCompat.END;
        if (currentLayoutAngle == 90) {
            gravity = GravityCompat.END;
        } else if (currentLayoutAngle == 270) {
            gravity = GravityCompat.START;
        }
        recTimeView.setGravity(gravity);
        recModeText.setGravity(gravity);
        setLayoutMarginsForManualVideoMode(degree);
        return true;
    }

    private void setLayoutMarginsForManualVideoMode(int degree) {
        boolean showRatioGuide = true;
        String contentSize = this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
        if (contentSize != null && !"not found".equals(contentSize)) {
            int[] contentSizeArr = Utils.sizeStringToArray(contentSize);
            if (contentSizeArr != null && contentSizeArr.length > 1) {
                int marginEnd;
                int marginTop;
                if (!ManualUtil.isCinemaSize(this.mGet.getAppContext(), contentSizeArr[0], contentSizeArr[1]) || ModelProperties.isLongLCDModel()) {
                    showRatioGuide = false;
                }
                if (degree == 0 || degree == 180) {
                    if (showRatioGuide) {
                        marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator_ratio_guide.marginEnd);
                        marginTop = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator_ratio_guide.marginTop);
                    } else {
                        marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator.marginEnd);
                        marginTop = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator.marginTop) + getRotateLayoutMargin(Setting.KEY_MANUAL_VIDEO_SIZE);
                    }
                } else if (showRatioGuide) {
                    marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator_ratio_guide_portrait.marginEnd);
                    marginTop = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator_ratio_guide_portrait.marginTop);
                } else {
                    marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator_portrait.marginEnd);
                    marginTop = Utils.getPx(getAppContext(), C0088R.dimen.manual_recording_indicator_portrait.marginTop) + getRotateLayoutMargin(Setting.KEY_MANUAL_VIDEO_SIZE);
                }
                LayoutParams timeIndicatorParams = (LayoutParams) this.mRecView.findViewById(C0088R.id.rec_time_indicator).getLayoutParams();
                timeIndicatorParams.setMarginEnd(marginEnd);
                timeIndicatorParams.topMargin = marginTop;
                View timeIndicator = this.mRecView.findViewById(C0088R.id.rec_time_indicator);
                if (timeIndicator != null) {
                    timeIndicator.setLayoutParams(timeIndicatorParams);
                }
            }
        }
    }

    public void onDestroy() {
        this.mRecView = null;
        this.mRecProgressBar = null;
        this.mCurRecTimeString = null;
        this.mInterface = null;
        if (this.mArcProgress != null) {
            MemoryUtils.releaseViews(this.mArcProgress);
            this.mArcProgress = null;
        }
        super.onDestroy();
    }

    public void updateRecTimeLayout() {
        if (this.mRecView != null) {
            ArcProgress arcIcon = (ArcProgress) this.mRecView.findViewById(C0088R.id.arc_process);
            if (arcIcon != null) {
                ViewGroup.LayoutParams iconArcParm = arcIcon.getLayoutParams();
                if (iconArcParm != null) {
                    arcIcon.setLayoutParams(iconArcParm);
                    setRotateDegree(getOrientationDegree(), false);
                    if (this.mGet.isManualMode() && VideoRecorder.getLoopState() != 1) {
                        RotateTextView recTimeText = (RotateTextView) this.mRecView.findViewById(C0088R.id.text_rec_time);
                        this.mCurRecTimeString = String.format("%02d:%02d:%02d:%03d", new Object[]{Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), Long.valueOf(0)});
                        recTimeText.setText(this.mCurRecTimeString);
                    }
                }
            }
        }
    }

    public ArcProgress getArcProgress() {
        return this.mArcProgress;
    }

    public void setRec3sec(boolean set) {
        this.mIsRec3sec = set;
    }

    public void setProgresbarView(boolean isVisible) {
        if (this.mRecProgressBar != null) {
            if (isVisible) {
                this.mRecProgressBar.setVisibility(0);
            } else {
                this.mRecProgressBar.setVisibility(8);
            }
        }
    }
}
