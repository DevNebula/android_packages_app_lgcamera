package com.lge.camera.managers.ext;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.support.p000v4.view.InputDeviceCompat;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.ArcEffectView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.SmartCamBar;
import com.lge.camera.components.SmartCamBarInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SmartcamUtil;
import com.lge.camera.util.Utils;
import com.lge.ellievision.IEllieVision;
import com.lge.ellievision.IRemoteServiceCallback;
import com.lge.ellievision.IRemoteServiceCallback.Stub;
import com.lge.ellievision.MotionHandShakeReset;
import com.lge.ellievision.MotionHandShakeReset.MotionHandShakeResetCallback;
import com.lge.ellievision.parceldata.IBitmap;
import com.lge.ellievision.parceldata.IRecognition;
import com.lge.ellievision.parceldata.IRecognitionResults;
import com.lge.ellievision.parceldata.ISceneCategory;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SmartCamManagerBase extends ManagerInterfaceImpl {
    protected static final long AUTO_SCENE_DETECT_HOLDINGTIME = 3000;
    protected static final int BAR_AUTOCONTRAST_MAX = 400;
    protected static final int BAR_AUTOCONTRAST_MIN = 0;
    protected static final int BAR_CONTRAST_DEFAULT = 10;
    protected static final int BAR_CONTRAST_MAX = 20;
    protected static final int BAR_CONTRAST_MIN = 0;
    protected static final int BAR_EV_COMPENSATION_VALUE = 3;
    protected static final int BAR_EV_MAX = 12;
    protected static final int BINNING_STATE_OFF = 0;
    protected static final int BINNING_STATE_ON = 2;
    protected static final int BINNING_STATE_RESTORE_SETTING = 1;
    protected static final String ELLIE_SERVICE_USAGE = "aicam";
    protected static final int EYEEM_IMAGE_SIZE = 224;
    protected static final int PARCELABLE_LIMIT_SIZE = 1048576;
    protected static final String RECOGNITION_THREAD_NAME = "smart_cam_recognition";
    protected static final long RECOGNITION_THREAD_SLEEPTIME = 1000;
    protected static final int SMARTCAM_FRAME = 2;
    protected static final int TAGCLOUD_MAX_RECOG_SIZE = 12;
    protected static final float TAGCLOUD_TEXT_CONFIDENCE_VALUE = 0.9f;
    protected static final String TAGCLOUD_THREAD_NAME = "smart_cam_tag_cloud";
    protected static final long TAGCLOUD_THREAD_SLEEPTIME = 500;
    protected static final int TAG_NUM_MAX_ARRAY_SIZE = 20;
    protected static final int TAG_NUM_SCENE_CHANGED = 3;
    private final float BAR_HEIGHT = 0.0556f;
    protected ViewGroup mBaseView = null;
    protected int mBeforeDegree = -1;
    protected SmartCamBar mContrastBar = null;
    protected View mContrastBarLayout = null;
    private SmartCamBarInterface mContrastBarListener = new C126813();
    private AnimationListener mCoverAnimListener = new C12733();
    protected String mCurrentCategory = ISceneCategory.CATEGORY_NOT_DEFIND;
    protected String mCurrentDisplayName = ISceneCategory.CATEGORY_NOT_DEFIND;
    protected Rect mDualBtnRect = new Rect();
    protected SmartCamBar mEVBar = null;
    protected View mEVBarLayout = null;
    private SmartCamBarInterface mEVBarListener = new C126914();
    protected IRemoteServiceCallback mEllieServiceCallback = new C126410();
    protected View mFilterEffectCover = null;
    protected HandlerRunnable mHandShakeResetRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (SmartCamManagerBase.this.mIEllieVision != null) {
                try {
                    CamLog.m11w(CameraConstants.TAG, "AI-resetSceneFromMotionHandShake-true");
                    SmartCamManagerBase.this.mIEllieVision.resetSceneHandShake();
                    SmartCamManagerBase.this.mIsHandShakingResetSending = true;
                    SmartCamManagerBase.this.stopTimerForSmartCamScene();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    protected IEllieVision mIEllieVision = null;
    protected IRecognitionResults mIRecognitionResults = null;
    protected int mIsBinningState = 0;
    protected boolean mIsHandShakingResetSending = false;
    protected boolean mIsRegisterCallback = false;
    protected boolean mIsSameScene = false;
    protected String mLatestDisplayName = ISceneCategory.CATEGORY_NOT_DEFIND;
    protected int mLatestEVBarParamValue = 0;
    protected int mLatestFilterIndex = -1;
    protected String mLatestFilterName = CameraConstants.FILM_SMARTCAM_NONE;
    protected int mLatestSilContrastBarParamValue = 0;
    protected int mLatestTextBarParamValue = 0;
    protected int mMaxEvParamValue = 9;
    protected MotionHandShakeReset mMotionHandShakeReset = null;
    protected byte[] mOutRGBArray = null;
    protected View mPeopleEffectLayout = null;
    protected float mPeopleEffectRadius = 200.0f;
    protected RotateLayout mPeopleEffectText = null;
    protected ArcEffectView mPeopleEffectView1st = null;
    protected ArcEffectView mPeopleEffectView2nd = null;
    protected List<PointF> mPoint = null;
    protected int mPreviewCallbackTime = 0;
    protected int mPreviewH = 0;
    protected int mPreviewW = 0;
    public Runnable mRecognitionRunnable = new C12722();
    protected SmartCamThread mRecognitionThread;
    public HandlerRunnable mResetUnknownCount = new HandlerRunnable(this) {
        public void handleRun() {
            if (SmartCamManagerBase.this.mIEllieVision != null) {
                try {
                    CamLog.m11w(CameraConstants.TAG, "AI-mResetUnknownCount");
                    SmartCamManagerBase.this.mIEllieVision.resetUnknownCount();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    protected int mResizedPreviewHeight = 0;
    protected int mResizedPreviewWidth = 0;
    protected View mSceneBtn = null;
    protected ImageView mSceneBtnImage = null;
    protected Rect mSceneBtnRect = new Rect();
    protected RotateLayout mSceneBtnRotate = null;
    protected TextView mSceneBtnText = null;
    protected String mSetFilterName = null;
    protected SmartCamThread mShowTextViewThread;
    protected SmartCamInterface mSmartCamInterface = null;
    protected View mSmartCamLayout = null;
    protected ArrayList<SmartCamTagCloudList> mSmartCamTagCloudList = null;
    protected View mSmartCamView = null;
    public HandlerRunnable mStopPeopleEffectRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            SmartCamManagerBase.this.stopPeopleEffect(false);
        }
    };
    protected Object mSync = new Object();
    protected int mTagCloudHeight = 0;
    protected ViewGroup mTagCloudLayout = null;
    public Runnable mTagCloudRunnable = new C12711();
    protected int mTagCloudStartMargin = 0;
    protected int mTagCloudStartMarginForOnTilePreview = 0;
    protected int mTagCloudWidth = 0;
    protected int mTargetHeight = 0;
    protected int mTargetWidth = 0;
    protected Rect mTextRect = new Rect();
    protected int mTimerCount = 0;

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$10 */
    class C126410 extends Stub {
        C126410() {
        }

        public void onRecognitionComplete(IRecognitionResults iRecognitionResults) {
            CamLog.m5e(CameraConstants.TAG, "AI-onRecognitionComplete. mCurrentCategory : " + SmartCamManagerBase.this.mCurrentCategory);
            if (!SmartCamManagerBase.this.mGet.isTimerShotCountdown()) {
                SmartCamManagerBase.this.mIRecognitionResults = iRecognitionResults;
                if (ISceneCategory.CATEGORY_UNKNOWN.equals(SmartCamManagerBase.this.mCurrentCategory) || ISceneCategory.CATEGORY_NOT_DEFIND.equals(SmartCamManagerBase.this.mCurrentCategory)) {
                    SmartCamManagerBase.this.insertRecognitionText(true);
                }
            }
        }

        public void onSceneChanged(final ISceneCategory iSceneCategory) {
            if (iSceneCategory == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-onSceneChanged, iSceneCategory is null, return.");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-onSceneChanged : " + iSceneCategory.category + ", display name : " + iSceneCategory.displayName + ", mIsBinningState : " + SmartCamManagerBase.this.mIsBinningState);
            if (SmartCamManagerBase.this.mIsBinningState <= 0) {
                SmartCamManagerBase.this.mGet.runOnUiThread(new HandlerRunnable(SmartCamManagerBase.this) {
                    public void handleRun() {
                        if (SmartCamManagerBase.this.mCurrentCategory == null || SmartCamManagerBase.this.mCurrentDisplayName == null || iSceneCategory == null) {
                            CamLog.m5e(CameraConstants.TAG, "AI-current category is null. return.");
                            return;
                        }
                        if (SmartCamManagerBase.this.mSmartCamInterface != null) {
                            SmartCamManagerBase.this.mSmartCamInterface.notifySceneChanged(iSceneCategory.category, iSceneCategory.displayName);
                        }
                        if (ISceneCategory.CATEGORY_UNKNOWN.equals(iSceneCategory.category) && SmartCamManagerBase.this.mIsHandShakingResetSending) {
                            SmartCamManagerBase.this.startTimerForSmartCamScene(1);
                            SmartCamManagerBase.this.mIsHandShakingResetSending = false;
                        }
                        SmartCamManagerBase smartCamManagerBase = SmartCamManagerBase.this;
                        boolean z = SmartCamManagerBase.this.mCurrentCategory.equals(iSceneCategory.category) && SmartCamManagerBase.this.mCurrentDisplayName.equals(iSceneCategory.displayName);
                        smartCamManagerBase.mIsSameScene = z;
                        if (SmartCamManagerBase.this.mIsSameScene) {
                            if (!(ISceneCategory.CATEGORY_UNKNOWN.equals(SmartCamManagerBase.this.mCurrentCategory) || ISceneCategory.CATEGORY_NOT_DEFIND.equals(SmartCamManagerBase.this.mCurrentCategory))) {
                                SmartCamManagerBase.this.startTimerForSmartCamScene(0);
                            }
                            CamLog.m7i(CameraConstants.TAG, "AI-mIsSameScene, return");
                        } else if (!SmartCamManagerBase.this.mGet.isTimerShotCountdown() && SmartCamManagerBase.this.mIsBinningState <= 0 && !SmartCamManagerBase.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && SmartCamManagerBase.this.checkDeviceAction()) {
                            if ((SmartCamManagerBase.this.mEVBarLayout != null && SmartCamManagerBase.this.mEVBarLayout.getVisibility() == 0) || (SmartCamManagerBase.this.mContrastBarLayout != null && SmartCamManagerBase.this.mContrastBarLayout.getVisibility() == 0)) {
                                SmartCamManagerBase.this.stopTimerForSmartCamScene();
                            }
                            SmartCamManagerBase.this.mCurrentCategory = iSceneCategory.category;
                            SmartCamManagerBase.this.mCurrentDisplayName = iSceneCategory.displayName;
                            SmartCamManagerBase.this.applyScene(iSceneCategory, true);
                            if (SmartCamManagerBase.this.mSmartCamTagCloudList != null) {
                                SmartCamManagerBase.this.mSmartCamTagCloudList.clear();
                                SmartCamManagerBase.this.insertRecognitionText(ISceneCategory.CATEGORY_UNKNOWN.equals(iSceneCategory.category));
                            }
                            SmartCamManagerBase.this.restoreLatestScene();
                        }
                    }
                });
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$13 */
    class C126813 implements SmartCamBarInterface {
        C126813() {
        }

        public void onBarValueChanged(int value) {
            CamLog.m3d(CameraConstants.TAG, "AI-onBarValueChanged-bartype : " + SmartCamManagerBase.this.mCurrentCategory + ", contrastBar value : " + value);
            if (SmartCamManagerBase.this.mSmartCamInterface == null) {
                return;
            }
            if (ISceneCategory.CATEGORY_ID_TEXT.equals(SmartCamManagerBase.this.mCurrentCategory)) {
                SmartCamManagerBase.this.mSmartCamInterface.updateAutoContrastSolution(value);
            } else if (ISceneCategory.CATEGORY_ID_SILHOUETTE.equals(SmartCamManagerBase.this.mCurrentCategory)) {
                SmartCamManagerBase.this.mSmartCamInterface.updateContrastParam(value);
            }
        }

        public void onBarUp() {
            SmartCamManagerBase.this.stopTimerForSmartCamScene();
        }

        public boolean isTouchAvailable() {
            return SmartCamManagerBase.this.mGet.checkModuleValidate(16) || SmartCamManagerBase.this.mGet.isMultishotState(7);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$14 */
    class C126914 implements SmartCamBarInterface {
        C126914() {
        }

        public void onBarValueChanged(int value) {
            int aeParamValue = value - SmartCamManagerBase.this.mMaxEvParamValue;
            CamLog.m7i(CameraConstants.TAG, "AI-EV BAR, value : " + value + ", " + aeParamValue);
            if (SmartCamManagerBase.this.mSmartCamInterface != null) {
                SmartCamManagerBase.this.mSmartCamInterface.setEVParam(aeParamValue);
            }
        }

        public void onBarUp() {
            SmartCamManagerBase.this.stopTimerForSmartCamScene();
        }

        public boolean isTouchAvailable() {
            return SmartCamManagerBase.this.mGet.checkModuleValidate(16) || SmartCamManagerBase.this.mGet.isMultishotState(7);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$1 */
    class C12711 implements Runnable {
        C12711() {
        }

        public void run() {
            if (SmartCamManagerBase.this.mShowTextViewThread != null && !SmartCamManagerBase.this.mShowTextViewThread.isInterrupted()) {
                SmartCamManagerBase.this.setTagCloudText();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$2 */
    class C12722 implements Runnable {
        C12722() {
        }

        public void run() {
            synchronized (SmartCamManagerBase.this.mSync) {
                if (!(SmartCamManagerBase.this.mRecognitionThread == null || SmartCamManagerBase.this.mRecognitionThread.isInterrupted())) {
                    SmartCamManagerBase.this.sendEyeemServiceSmartCam();
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$3 */
    class C12733 implements AnimationListener {
        C12733() {
        }

        public void onAnimationStart(Animation animation) {
            SmartCamManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(SmartCamManagerBase.this) {
                public void handleRun() {
                    if (!SmartCamManagerBase.this.mGet.isTimerShotCountdown() && SmartcamUtil.isSmartcamBindService() && SmartCamManagerBase.this.mIsRegisterCallback && SmartCamManagerBase.this.mSmartCamInterface != null && SmartCamManagerBase.this.mSetFilterName != null) {
                        SmartCamManagerBase.this.mSmartCamInterface.setFilterListForSmartCam(SmartCamManagerBase.this.mSetFilterName, SmartCamManagerBase.this.mLatestFilterIndex);
                        if (SmartCamManagerBase.this.mLatestFilterIndex != -1) {
                            SmartCamManagerBase.this.mLatestFilterIndex = -1;
                        }
                    }
                }
            }, 225);
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SmartCamManagerBase$5 */
    class C12765 implements OnClickListener {
        C12765() {
        }

        public void onClick(View v) {
            if (SmartCamManagerBase.this.mEVBar == null || SmartCamManagerBase.this.mEVBarLayout == null || SmartCamManagerBase.this.mContrastBar == null || SmartCamManagerBase.this.mContrastBarLayout == null || SmartCamManagerBase.this.mSceneBtnText == null || SmartCamManagerBase.this.mSceneBtnImage == null || !SmartCamManagerBase.this.mGet.checkModuleValidate(240) || SmartCamManagerBase.this.mGet.isTimerShotCountdown() || !SmartCamManagerBase.this.mGet.checkQuickButtonAvailable() || !SmartCamManagerBase.this.mGet.checkFocusStateForChangingSetting()) {
                CamLog.m7i(CameraConstants.TAG, "AI-scene button return.");
                return;
            }
            boolean z;
            String sceneBtnName = (String) SmartCamManagerBase.this.mSceneBtnText.getText();
            CamLog.m3d(CameraConstants.TAG, "AI-on Scene Btn click, sceneBtnName is : " + sceneBtnName);
            SmartCamManagerBase.this.mSceneBtnImage.setSelected(!SmartCamManagerBase.this.mSceneBtn.isSelected());
            TextView textView = SmartCamManagerBase.this.mSceneBtnText;
            if (SmartCamManagerBase.this.mSceneBtn.isSelected()) {
                z = false;
            } else {
                z = true;
            }
            textView.setSelected(z);
            SmartCamManagerBase.this.mSceneBtnText.setEllipsize(SmartCamManagerBase.this.mSceneBtn.isSelected() ? TruncateAt.END : TruncateAt.MARQUEE);
            View view = SmartCamManagerBase.this.mSceneBtn;
            if (SmartCamManagerBase.this.mSceneBtn.isSelected()) {
                z = false;
            } else {
                z = true;
            }
            view.setSelected(z);
            if (SmartCamManagerBase.this.mSmartCamInterface != null) {
                SmartCamManagerBase.this.mSmartCamInterface.hideZoomBar();
                SmartCamManagerBase.this.mSmartCamInterface.hideFocus();
            }
            if (sceneBtnName.equals(SmartCamManagerBase.this.mGet.getAppContext().getString(C0088R.string.sp_lglens_tag_night_sky_NORMAL))) {
                if (SmartCamManagerBase.this.mEVBarLayout.getVisibility() == 0) {
                    SmartCamManagerBase.this.showSmartCamBar(SmartCamManagerBase.this.mEVBarLayout, SmartCamManagerBase.this.mEVBar, false);
                    SmartCamManagerBase.this.mEVBar.setEnabled(false);
                    return;
                }
                SmartCamManagerBase.this.showSmartCamBar(SmartCamManagerBase.this.mEVBarLayout, SmartCamManagerBase.this.mEVBar, true);
                SmartCamManagerBase.this.stopTimerForSmartCamScene();
                SmartCamManagerBase.this.mEVBar.setEnabled(true);
            } else if (sceneBtnName.equals(SmartCamManagerBase.this.mGet.getAppContext().getString(C0088R.string.sp_lglens_tag_silhouette_NORMAL)) || sceneBtnName.equals(SmartCamManagerBase.this.mGet.getAppContext().getString(C0088R.string.sp_lglens_tag_text_NORMAL))) {
                if (SmartCamManagerBase.this.mContrastBarLayout.getVisibility() == 0) {
                    SmartCamManagerBase.this.showSmartCamBar(SmartCamManagerBase.this.mContrastBarLayout, SmartCamManagerBase.this.mContrastBar, false);
                    SmartCamManagerBase.this.mContrastBar.setEnabled(false);
                    return;
                }
                SmartCamManagerBase.this.showSmartCamBar(SmartCamManagerBase.this.mContrastBarLayout, SmartCamManagerBase.this.mContrastBar, true);
                SmartCamManagerBase.this.stopTimerForSmartCamScene();
                SmartCamManagerBase.this.mContrastBar.setEnabled(true);
            } else if (SmartCamManagerBase.this.mSetFilterName != null && SmartCamManagerBase.this.mSmartCamInterface != null) {
                if (SmartCamManagerBase.this.mSceneBtn.isSelected()) {
                    SmartCamManagerBase.this.mSmartCamInterface.showFilmMenu(true);
                    if (!SmartCamManagerBase.this.mSmartCamInterface.isShowingFilmMenu()) {
                        SmartCamManagerBase.this.stopTimerForSmartCamScene();
                        return;
                    }
                    return;
                }
                SmartCamManagerBase.this.mSmartCamInterface.showFilmMenu(false);
            }
        }
    }

    protected class MotionRegHandShakeResetCallback implements MotionHandShakeResetCallback {
        protected MotionRegHandShakeResetCallback() {
        }

        public void onMotionHandShakeResetCallback() {
            if (SmartCamManagerBase.this.checkDeviceAction()) {
                CamLog.m3d(CameraConstants.TAG, "AI-onMotionHandShakeResetCallback");
                SmartCamManagerBase.this.resetSceneFromMotionHandShake();
                if (SmartCamManagerBase.this.mSmartCamInterface != null) {
                    SmartCamManagerBase.this.mSmartCamInterface.showFilmMenu(false);
                    if (ISceneCategory.CATEGORY_ID_LOW_LIGHT.equals(SmartCamManagerBase.this.mCurrentCategory)) {
                        CamLog.m7i(CameraConstants.TAG, "AI-MotionHandShaking, mCurrentCategory is " + SmartCamManagerBase.this.mCurrentCategory);
                        SmartCamManagerBase.this.mSmartCamInterface.onMotionHandShakingBinningReset();
                    }
                }
                SmartCamManagerBase.this.mIsSameScene = false;
                SmartCamManagerBase.this.resetSmartCam(true, false);
                SmartCamManagerBase.this.startTimerForSmartCamScene(1);
            }
        }
    }

    protected class SmartCamTagCloudList {
        public float mConfidence;
        public String mId;
        public String mTitle;

        public SmartCamTagCloudList(String id, String title, float confidence) {
            this.mId = id;
            this.mTitle = title;
            this.mConfidence = confidence;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof SmartCamTagCloudList)) {
                return false;
            }
            SmartCamTagCloudList thing = (SmartCamTagCloudList) obj;
            if (this.mTitle != null) {
                return this.mTitle.equals(thing.mTitle);
            }
            if (thing.mTitle == null) {
                return true;
            }
            return false;
        }
    }

    public class SmartCamThread extends Thread {
        private static final long DEF_SLEEP_TIME = 500;
        private int mCnt = 0;
        private boolean mIsSkip = false;
        private Runnable mRunnable = null;
        private boolean mRunning = false;
        private long mSleepTime = 0;
        private String mThreadName = null;
        private Object mThreadSyncObj = new Object();

        public SmartCamThread(String name, Runnable r, long sleepTime) {
            setName(name);
            this.mThreadName = name;
            this.mRunnable = r;
            if (sleepTime <= 0) {
                sleepTime = 500;
            }
            this.mSleepTime = sleepTime;
        }

        public synchronized void start() {
            this.mRunning = true;
            super.start();
        }

        public void run() {
            if (this.mRunnable == null) {
                CamLog.m5e(CameraConstants.TAG, "Runnable is empty. This thread should be stop.");
                return;
            }
            while (this.mRunning && !isInterrupted()) {
                if (SmartCamManagerBase.TAGCLOUD_THREAD_NAME.equals(this.mThreadName)) {
                    runTagCloud();
                } else if (SmartCamManagerBase.RECOGNITION_THREAD_NAME.equals(this.mThreadName)) {
                    runRecognition();
                } else {
                    CamLog.m5e(CameraConstants.TAG, "There is no thread name to run.");
                    return;
                }
            }
        }

        public void runTagCloud() {
            if (this.mRunnable != null) {
                this.mRunnable.run();
                try {
                    sleep(this.mSleepTime);
                } catch (InterruptedException e) {
                    CamLog.m11w(CameraConstants.TAG, "AI-TAGCLOUD_THREAD_NAME is interrupted.");
                }
            }
        }

        public void runRecognition() {
            if (this.mRunnable != null) {
                if (!this.mIsSkip) {
                    synchronized (this.mThreadSyncObj) {
                        if (isInterrupted()) {
                            return;
                        }
                        if (this.mCnt == 0) {
                            this.mRunnable.run();
                            this.mCnt = Math.round(((float) this.mSleepTime) / 500.0f);
                        } else {
                            this.mCnt--;
                        }
                        this.mCnt = this.mCnt < 0 ? 0 : this.mCnt;
                    }
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    CamLog.m11w(CameraConstants.TAG, "AI-RECOGNITION_THREAD_NAME is interrupted.");
                }
            }
        }

        public void stopThread() {
            this.mRunning = false;
            this.mCnt = 0;
            interrupt();
        }

        public void setTimer(long time) {
            synchronized (this.mThreadSyncObj) {
                this.mSleepTime = time;
                this.mCnt = Math.round(((float) time) / 500.0f);
                CamLog.m3d(CameraConstants.TAG, "AI-setTimer, mSleepTime : " + this.mSleepTime + ", mCnt : " + this.mCnt);
            }
        }

        public void setTimerSkip(boolean isSkip) {
            this.mIsSkip = isSkip;
            CamLog.m3d(CameraConstants.TAG, "AI-setTimerSkip, mIsSkip " + this.mIsSkip);
        }
    }

    public void setSmartCamInterface(SmartCamInterface interfaces) {
        this.mSmartCamInterface = interfaces;
    }

    public SmartCamManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        this.mBaseView = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mSmartCamView = this.mGet.inflateView(C0088R.layout.smart_cam_view);
        View quickButtonView = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
        int index = 0;
        if (quickButtonView != null) {
            index = this.mBaseView.indexOfChild(quickButtonView) + 1;
        }
        this.mBaseView.addView(this.mSmartCamView, index, new LayoutParams(-1, -1));
        this.mSmartCamLayout = this.mSmartCamView.findViewById(C0088R.id.smart_cam_layout);
        this.mSmartCamTagCloudList = new ArrayList();
        this.mTagCloudLayout = (ViewGroup) this.mSmartCamView.findViewById(C0088R.id.smart_cam_tag_cloud_layout);
        this.mFilterEffectCover = this.mSmartCamView.findViewById(C0088R.id.filter_effect_cover);
        this.mContrastBarLayout = this.mSmartCamView.findViewById(C0088R.id.smart_cam_contrast_bar_layout);
        this.mContrastBar = (SmartCamBar) this.mSmartCamView.findViewById(C0088R.id.smart_cam_contrast_bar);
        this.mContrastBar.init(0, 20, 10);
        this.mContrastBar.initValue();
        this.mContrastBar.setOnSmartCamBarListener(this.mContrastBarListener);
        this.mEVBarLayout = this.mSmartCamView.findViewById(C0088R.id.smart_cam_ev_bar_layout);
        this.mEVBar = (SmartCamBar) this.mSmartCamView.findViewById(C0088R.id.smart_cam_ev_bar);
        initEVparamValue(this.mEVBar);
        this.mEVBar.initValue();
        this.mEVBar.setOnSmartCamBarListener(this.mEVBarListener);
        this.mSceneBtn = this.mSmartCamView.findViewById(C0088R.id.smart_cam_scene_btn);
        this.mSceneBtnRotate = (RotateLayout) this.mSmartCamView.findViewById(C0088R.id.smart_cam_scene_btn_rotate);
        this.mSceneBtnImage = (ImageView) this.mSmartCamView.findViewById(C0088R.id.smart_cam_scene_btn_image);
        this.mSceneBtnText = (TextView) this.mSmartCamView.findViewById(C0088R.id.smart_cam_scene_btn_text);
        this.mSceneBtnText.setText("");
        initSmartCamSceneBtnLayout();
        initSmartCamEVBarLayout();
        initSmartCamContrastBarLayout();
        setRotateDegree(this.mGet.getOrientationDegree(), false);
        setBtnClickListener();
        initPeopleEffectView();
    }

    protected void initPeopleEffectView() {
        if (this.mSmartCamView != null || !SharedPreferenceUtil.getAICamPeopleEffectShown(getAppContext())) {
            this.mPeopleEffectLayout = this.mSmartCamView.findViewById(C0088R.id.smart_cam_people_arceffect_layout);
            if (this.mPeopleEffectLayout != null) {
                this.mPeopleEffectRadius = (float) RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.075f);
                this.mPeopleEffectView1st = new ArcEffectView(this.mGet.getAppContext());
                this.mPeopleEffectView1st.initArcEffect(InputDeviceCompat.SOURCE_ANY, this.mPeopleEffectRadius, 1, 0, 360);
                this.mPeopleEffectView2nd = new ArcEffectView(this.mGet.getAppContext());
                this.mPeopleEffectView2nd.initArcEffect(InputDeviceCompat.SOURCE_ANY, this.mPeopleEffectRadius, 1, 0, 360);
                ((ViewGroup) this.mPeopleEffectLayout).addView(this.mPeopleEffectView1st);
                ((ViewGroup) this.mPeopleEffectLayout).addView(this.mPeopleEffectView2nd);
                this.mPeopleEffectText = (RotateLayout) this.mSmartCamView.findViewById(C0088R.id.smart_cam_people_text);
            }
        }
    }

    protected void startPeopleEffect() {
        if (this.mGet.getCameraId() == 2 || (!this.mGet.isRearCamera() && ((FunctionProperties.getCameraTypeFront() == 2 && SharedPreferenceUtil.getCropAngleButtonId(getAppContext()) == 1) || FunctionProperties.getCameraTypeFront() == 0))) {
            CamLog.m7i(CameraConstants.TAG, "No need to show AI-People effect, it is already wide angle camera.");
            return;
        }
        if (this.mSmartCamInterface != null) {
            this.mSmartCamInterface.isStartWideAngleAnimation(true);
        }
        if (SharedPreferenceUtil.getAICamPeopleEffectShown(this.mGet.getAppContext())) {
            CamLog.m7i(CameraConstants.TAG, "No need to show AI-People effect");
            this.mGet.postOnUiThread(this.mStopPeopleEffectRunnable, 3000);
        } else if (this.mPeopleEffectLayout == null || this.mPeopleEffectView1st == null || this.mPeopleEffectView2nd == null || this.mPeopleEffectText == null) {
            CamLog.m7i(CameraConstants.TAG, "AI-People effect view is null. return.");
        } else {
            this.mPeopleEffectText.setVisibility(0);
            this.mPeopleEffectLayout.setVisibility(0);
            Animation animationSet = new AnimationSet(true);
            animationSet.setInterpolator(new AccelerateInterpolator());
            Animation alphaAni1 = new AlphaAnimation(0.5f, 0.0f);
            alphaAni1.setDuration(600);
            alphaAni1.setRepeatCount(-1);
            alphaAni1.setRepeatMode(1);
            float pivot = this.mPeopleEffectRadius / 2.0f;
            Animation scaleAni1 = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f, pivot, pivot);
            scaleAni1.setDuration(600);
            scaleAni1.setRepeatCount(-1);
            scaleAni1.setRepeatMode(1);
            animationSet.addAnimation(alphaAni1);
            animationSet.addAnimation(scaleAni1);
            this.mPeopleEffectView1st.startAnimation(animationSet);
            animationSet = new AnimationSet(true);
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet = new AlphaAnimation(0.0f, 0.5f);
            animationSet.setDuration(300);
            animationSet.setStartOffset(300);
            animationSet.setRepeatCount(-1);
            animationSet.setRepeatMode(1);
            Animation scaleAni2 = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, pivot, pivot);
            scaleAni2.setDuration(300);
            scaleAni2.setStartOffset(300);
            scaleAni2.setRepeatCount(-1);
            scaleAni2.setRepeatMode(1);
            animationSet.addAnimation(animationSet);
            animationSet.addAnimation(scaleAni2);
            this.mPeopleEffectView2nd.startAnimation(animationSet);
            SharedPreferenceUtil.setAICamPeopleEffectShown(this.mGet.getAppContext(), true);
            this.mGet.postOnUiThread(this.mStopPeopleEffectRunnable, 3000);
        }
    }

    public void stopPeopleEffect(boolean removeText) {
        if (this.mPeopleEffectLayout == null || this.mPeopleEffectView1st == null || this.mPeopleEffectView2nd == null || this.mPeopleEffectText == null) {
            CamLog.m11w(CameraConstants.TAG, "AI-People effect view is null. return.");
            return;
        }
        if (this.mSmartCamInterface != null) {
            this.mSmartCamInterface.isStartWideAngleAnimation(false);
        }
        if (removeText) {
            this.mPeopleEffectText.setVisibility(8);
        }
        this.mPeopleEffectView1st.clearAnimation();
        this.mPeopleEffectView2nd.clearAnimation();
        this.mPeopleEffectLayout.setVisibility(8);
    }

    protected void initEVparamValue(SmartCamBar evBar) {
        if (evBar != null) {
            this.mMaxEvParamValue = this.mGet.getMaxEVStep();
            if (this.mMaxEvParamValue < 0) {
                this.mMaxEvParamValue = 12;
            }
            this.mMaxEvParamValue -= 3;
            evBar.init(0, this.mMaxEvParamValue * 2, this.mMaxEvParamValue);
        }
    }

    private void initSmartCamSceneBtnLayout() {
        if (this.mSceneBtn != null) {
            LayoutParams lParam = (LayoutParams) this.mSceneBtn.getLayoutParams();
            if (ModelProperties.getLCDType() == 2) {
                lParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.321f);
            } else {
                lParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.288f);
            }
            Drawable icon = getAppContext().getDrawable(C0088R.drawable.btn_smartcam_person_normal);
            if (icon != null) {
                lParam.bottomMargin -= (Utils.getPx(getAppContext(), C0088R.dimen.smart_cam_scenebtn_width_height) - icon.getIntrinsicHeight()) / 2;
            }
            this.mSceneBtn.setLayoutParams(lParam);
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            if (lcdSize != null) {
                int lcdHeight = Math.max(lcdSize[0], lcdSize[1]);
                int left = lParam.leftMargin;
                int top = (lcdHeight - lParam.bottomMargin) - lParam.height;
                this.mSceneBtnRect.set(left, top, left + lParam.width, top + lParam.height);
            }
        }
    }

    private void initSmartCamEVBarLayout() {
        if (this.mEVBar != null && this.mEVBarLayout != null) {
            LayoutParams layoutParam = (LayoutParams) this.mEVBarLayout.getLayoutParams();
            if (ModelProperties.getLCDType() == 2) {
                layoutParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.304f);
            } else {
                layoutParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.284f);
            }
            this.mEVBarLayout.setLayoutParams(layoutParam);
            LinearLayout.LayoutParams barParam = (LinearLayout.LayoutParams) this.mEVBar.getLayoutParams();
            barParam.width = Utils.getPx(getAppContext(), C0088R.dimen.smart_cam_filter_slider_bar_width) + this.mEVBar.getCursorSize().getWidth();
            barParam.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0556f);
            int marginStartEnd = Utils.getPx(getAppContext(), C0088R.dimen.smart_cam_filter_slider_bar_start_end_margin) - (this.mEVBar.getCursorSize().getWidth() / 2);
            barParam.setMarginStart(marginStartEnd);
            barParam.setMarginEnd(marginStartEnd);
            this.mEVBar.setLayoutParams(barParam);
        }
    }

    private void initSmartCamContrastBarLayout() {
        if (this.mContrastBar != null && this.mContrastBarLayout != null) {
            LayoutParams layoutParam = (LayoutParams) this.mContrastBarLayout.getLayoutParams();
            if (ModelProperties.getLCDType() == 2) {
                layoutParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.304f);
            } else {
                layoutParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.284f);
            }
            this.mContrastBarLayout.setLayoutParams(layoutParam);
            LinearLayout.LayoutParams barParam = (LinearLayout.LayoutParams) this.mContrastBar.getLayoutParams();
            barParam.width = Utils.getPx(getAppContext(), C0088R.dimen.smart_cam_filter_slider_bar_width) + this.mContrastBar.getCursorSize().getWidth();
            barParam.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0556f);
            int marginStartEnd = Utils.getPx(getAppContext(), C0088R.dimen.smart_cam_filter_slider_bar_start_end_margin) - (this.mContrastBar.getCursorSize().getWidth() / 2);
            barParam.setMarginStart(marginStartEnd);
            barParam.setMarginEnd(marginStartEnd);
            this.mContrastBar.setLayoutParams(barParam);
        }
    }

    private void setBtnClickListener() {
        if (this.mSceneBtn != null) {
            this.mSceneBtn.setOnClickListener(new C12765());
        }
    }

    protected void resetSmartCam(final boolean isStartSmartcam, final boolean startRecording) {
        CamLog.m3d(CameraConstants.TAG, "AI-resetSmartCam-isStartSmartcam : " + isStartSmartcam + ", startRecording : " + startRecording);
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (SmartCamManagerBase.this.mTagCloudLayout != null) {
                    SmartCamManagerBase.this.mTagCloudLayout.removeAllViews();
                }
                if (SmartCamManagerBase.this.mSmartCamTagCloudList != null) {
                    SmartCamManagerBase.this.mSmartCamTagCloudList.clear();
                }
                if (SmartCamManagerBase.this.mFilterEffectCover != null) {
                    SmartCamManagerBase.this.mFilterEffectCover.clearAnimation();
                    SmartCamManagerBase.this.mFilterEffectCover.setVisibility(8);
                }
                SmartCamManagerBase.this.stopPeopleEffect(true);
                if (!(SmartCamManagerBase.this.mSceneBtn == null || SmartCamManagerBase.this.mSceneBtnText == null || SmartCamManagerBase.this.mSceneBtnImage == null)) {
                    SmartCamManagerBase.this.mSceneBtnImage.setSelected(false);
                    SmartCamManagerBase.this.mSceneBtnText.setSelected(false);
                    SmartCamManagerBase.this.mSceneBtnText.setText("");
                    SmartCamManagerBase.this.mSceneBtn.setVisibility(8);
                    SmartCamManagerBase.this.mSceneBtn.setSelected(false);
                }
                if (!(SmartCamManagerBase.this.mContrastBarLayout == null || SmartCamManagerBase.this.mContrastBar == null)) {
                    SmartCamManagerBase.this.showSmartCamBar(SmartCamManagerBase.this.mContrastBarLayout, SmartCamManagerBase.this.mContrastBar, false);
                    SmartCamManagerBase.this.mContrastBar.initValue();
                }
                if (!(SmartCamManagerBase.this.mEVBarLayout == null || SmartCamManagerBase.this.mEVBar == null)) {
                    SmartCamManagerBase.this.showSmartCamBar(SmartCamManagerBase.this.mEVBarLayout, SmartCamManagerBase.this.mEVBar, false);
                    SmartCamManagerBase.this.mEVBar.initValue();
                }
                if (SmartCamManagerBase.this.mSmartCamInterface != null) {
                    if (isStartSmartcam) {
                        SmartCamManagerBase.this.mSmartCamInterface.applyFilterToSceneTextSelected(CameraConstants.FILM_SMARTCAM_NONE);
                    }
                    if (startRecording) {
                        SmartCamManagerBase.this.mSmartCamInterface.resetAutoContrastSolution(false, true);
                    } else {
                        SmartCamManagerBase.this.mSmartCamInterface.resetAllparamFunction(isStartSmartcam ? 0 : -1, 0, false);
                    }
                    SmartCamManagerBase.this.stopPeopleEffect(true);
                }
                SmartCamManagerBase.this.mCurrentCategory = ISceneCategory.CATEGORY_NOT_DEFIND;
                SmartCamManagerBase.this.mCurrentDisplayName = ISceneCategory.CATEGORY_NOT_DEFIND;
                SmartCamManagerBase.this.mIsSameScene = false;
            }
        });
    }

    public void showSmartCamBar(View barView, SmartCamBar bar, boolean show) {
        if (bar != null && barView != null && barView.isShown() != show && this.mSmartCamInterface != null) {
            barView.setVisibility(show ? 0 : 8);
            bar.setEnabled(show);
            this.mSmartCamInterface.onSmartCamBarShow(show);
        }
    }

    private void setTagCloudText() {
        if (this.mTagCloudLayout != null && this.mSmartCamTagCloudList != null && this.mSmartCamTagCloudList.size() != 0 && !this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !this.mGet.isActivatedQuickview() && !this.mGet.isActivatedQuickdetailView() && !this.mGet.isAnimationShowing() && !this.mGet.isTimerShotCountdown() && this.mGet.checkModuleValidate(192) && this.mIsBinningState == 0) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SmartCamManagerBase.this.mTagCloudLayout != null && SmartCamManagerBase.this.mSmartCamTagCloudList != null && SmartCamManagerBase.this.mSmartCamTagCloudList.size() != 0) {
                        RotateLayout rotateView = (RotateLayout) SmartCamManagerBase.this.mGet.getActivity().getLayoutInflater().inflate(C0088R.layout.smart_cam_tag_cloud, null);
                        RotateTextView tv = (RotateTextView) rotateView.findViewById(C0088R.id.smart_cam_tag_cloud_textview);
                        tv.setTypeface(Typeface.DEFAULT);
                        String headShot = "헤드 샷";
                        String choSangHwa = "초상화";
                        String title = ((SmartCamTagCloudList) SmartCamManagerBase.this.mSmartCamTagCloudList.get(SmartCamManagerBase.this.mSmartCamTagCloudList.size() - 1)).mTitle;
                        if ("헤드 샷".equals(title)) {
                            title = "초상화";
                        }
                        tv.setText(title);
                        tv.setTextSize(Utils.getPx(SmartCamManagerBase.this.getAppContext(), C0088R.dimen.smart_cam_tag_cloud_text_size));
                        SmartCamManagerBase.this.mTagCloudLayout.addView(rotateView);
                        SmartCamManagerBase.this.setTagCloudLocation(rotateView, tv);
                        SmartCamManagerBase.this.mTagCloudLayout.setVisibility(0);
                        SmartCamManagerBase.this.mTagCloudLayout.requestLayout();
                        SmartCamManagerBase.this.startSmartCamTagCloudAnimation(rotateView);
                        if (SmartCamManagerBase.this.mSmartCamTagCloudList != null && SmartCamManagerBase.this.mSmartCamTagCloudList.size() != 0) {
                            SmartCamManagerBase.this.mSmartCamTagCloudList.remove(SmartCamManagerBase.this.mSmartCamTagCloudList.size() - 1);
                        }
                    }
                }
            });
        }
    }

    private void setTagCloudLocation(RotateLayout rotateView, RotateTextView tv) {
        if (this.mTagCloudHeight != 0 && this.mTagCloudWidth != 0) {
            int degree = this.mGet.getOrientationDegree();
            setRotateDegree(degree, false);
            int tvWidth = (int) tv.getTextPaint().measureText(tv.getText());
            int tvHeight = (int) tv.getTextPaint().getFontSpacing();
            if (degree == 90 || degree == 270) {
                int temp = tvWidth;
                tvWidth = tvHeight;
                tvHeight = temp;
            }
            rotateView.setLayoutParams(new LayoutParams(tvWidth, tvHeight));
            float ratio = ((float) (this.mTagCloudHeight + this.mTagCloudStartMargin)) / ((float) this.mTagCloudWidth);
            int x = 0;
            int y = 0;
            SecureRandom ranMove = new SecureRandom();
            LayoutParams param = (LayoutParams) rotateView.getLayoutParams();
            int width;
            int height;
            int startMargin;
            int margin;
            if (degree == 0) {
                width = this.mTagCloudWidth;
                height = this.mTagCloudHeight / 3;
                if (((double) ratio) < 2.0d && ((double) ratio) > 1.7d) {
                    height = this.mTagCloudHeight / 4;
                    startMargin = this.mTagCloudStartMargin + (height * 2);
                } else if (((double) ratio) >= 2.3d || ((double) ratio) < 2.0d) {
                    startMargin = this.mTagCloudStartMargin + (height * 2);
                } else {
                    height = this.mTagCloudHeight / 4;
                    startMargin = height * 2;
                }
                x = ranMove.nextInt(width + 1);
                y = ranMove.nextInt(height + 1) + startMargin;
                this.mTextRect.set(x, y, x + tvWidth, y + tvHeight);
                if (isOverlapRect(this.mTextRect, this.mDualBtnRect)) {
                    y += this.mDualBtnRect.height() + tvHeight;
                }
                if (width < x + tvWidth) {
                    x -= tvWidth;
                }
                margin = tvHeight;
                if (height - margin < y + tvHeight) {
                    y -= tvHeight + margin;
                }
                this.mTextRect.set(x, y, x + tvWidth, y + tvHeight);
                if (isOverlapRect(this.mTextRect, this.mSceneBtnRect)) {
                    y -= this.mSceneBtnRect.height() + tvHeight;
                }
            } else if (degree == 90) {
                width = this.mTagCloudWidth / 3;
                height = this.mTagCloudHeight;
                startMargin = this.mTagCloudStartMargin + this.mTagCloudStartMarginForOnTilePreview;
                if (((double) ratio) < 2.3d && ((double) ratio) >= 2.0d) {
                    height = ((this.mTagCloudHeight / 4) * 3) - startMargin;
                } else if (((double) ratio) >= 2.0d || ((double) ratio) <= 1.7d) {
                    height -= this.mTagCloudStartMarginForOnTilePreview;
                } else {
                    height = ((this.mTagCloudHeight / 4) * 3) - this.mTagCloudStartMarginForOnTilePreview;
                }
                x = ranMove.nextInt(width + 1) + (width * 2);
                y = ranMove.nextInt(height + 1) + startMargin;
                this.mTextRect.set(x, y, x + tvWidth, y + tvHeight);
                if (isOverlapRect(this.mTextRect, this.mDualBtnRect)) {
                    x -= this.mDualBtnRect.width() + tvWidth;
                }
                margin = tvWidth;
                if (this.mTagCloudWidth - margin < x + tvWidth) {
                    x -= tvWidth + margin;
                }
                if (height + startMargin < y + tvHeight) {
                    y -= tvHeight;
                }
            } else if (degree == 180) {
                width = this.mTagCloudWidth;
                height = this.mTagCloudHeight / 3;
                startMargin = this.mTagCloudStartMargin + this.mTagCloudStartMarginForOnTilePreview;
                if (((double) ratio) <= 2.3d && ((double) ratio) > 1.7d) {
                    height = this.mTagCloudHeight / 4;
                }
                x = ranMove.nextInt(width + 1);
                y = ranMove.nextInt(height + 1) + startMargin;
                if (width < x + tvWidth) {
                    x -= tvWidth;
                }
                if (height + startMargin < y + tvHeight) {
                    y -= tvHeight;
                }
            } else if (degree == 270) {
                width = this.mTagCloudWidth / 3;
                height = this.mTagCloudHeight;
                startMargin = this.mTagCloudStartMargin + this.mTagCloudStartMarginForOnTilePreview;
                if (((double) ratio) < 2.3d && ((double) ratio) >= 2.0d) {
                    height = ((this.mTagCloudHeight / 4) * 3) - startMargin;
                } else if (((double) ratio) >= 2.0d || ((double) ratio) <= 1.7d) {
                    height -= this.mTagCloudStartMarginForOnTilePreview;
                } else {
                    height = ((this.mTagCloudHeight / 4) * 3) - this.mTagCloudStartMarginForOnTilePreview;
                }
                x = ranMove.nextInt(width + 1);
                y = ranMove.nextInt(height + 1) + startMargin;
                if (height + startMargin < y + tvHeight) {
                    y -= tvHeight;
                }
                this.mTextRect.set(x, y, x + tvWidth, y + tvHeight);
                if (isOverlapRect(this.mTextRect, this.mSceneBtnRect)) {
                    x += this.mSceneBtnRect.width() + tvWidth;
                }
            }
            param.setMargins(x, y, 0, 0);
            rotateView.setLayoutParams(param);
        }
    }

    public void setTagCloudLayout(int width, int height, int startMargin, int topMargin) {
        this.mTagCloudHeight = height;
        this.mTagCloudWidth = width;
        this.mTagCloudStartMargin = startMargin;
        this.mTagCloudStartMarginForOnTilePreview = 0;
        float ratio = ((float) this.mTagCloudHeight) / ((float) this.mTagCloudWidth);
        if (((double) ratio) > 1.0d && ((double) ratio) < 2.0d && "on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
            this.mTagCloudStartMarginForOnTilePreview = RatioCalcUtil.getLongLCDModelTopMargin(this.mGet.getAppContext(), 4, 3, 0);
        }
        if (((double) ratio) >= 2.0d) {
            this.mTagCloudStartMargin = RatioCalcUtil.getLongLCDModelTopMargin(this.mGet.getAppContext(), 4, 3, 0);
            this.mTagCloudHeight = ((height + startMargin) - this.mTagCloudStartMargin) + 2;
            this.mTagCloudWidth = width;
        }
        View dualBtnView = this.mGet.findViewById(C0088R.id.dual_button_parent_layout);
        if (dualBtnView != null) {
            int dualBtnWidth = 0;
            int dualBtnHeight = 0;
            ImageView normalBtn = (ImageView) dualBtnView.findViewById(C0088R.id.btn_dualview_normal_range);
            if (normalBtn != null) {
                Drawable normalBtnDrawable = normalBtn.getDrawable();
                if (normalBtnDrawable != null) {
                    dualBtnWidth = normalBtnDrawable.getIntrinsicWidth();
                    dualBtnHeight = normalBtnDrawable.getIntrinsicHeight();
                }
            }
            LayoutParams dualBtnParams = (LayoutParams) dualBtnView.getLayoutParams();
            int betweenMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0167f);
            int dualBtnMarginTop = dualBtnParams.topMargin;
            int dualBtnMarginEnd = dualBtnParams.getMarginEnd();
            this.mDualBtnRect.set((width - dualBtnHeight) - dualBtnMarginEnd, dualBtnMarginTop, width, ((dualBtnWidth * 2) + dualBtnMarginTop) + betweenMargin);
            if (this.mPeopleEffectLayout != null) {
                ViewGroup.LayoutParams pParams = (LayoutParams) this.mPeopleEffectLayout.getLayoutParams();
                pParams.width = (int) (this.mPeopleEffectRadius * 4.0f);
                pParams.height = (int) (this.mPeopleEffectRadius * 4.0f);
                int pTop = ((this.mDualBtnRect.top + (this.mDualBtnRect.height() / 2)) - (pParams.height / 2)) + ((dualBtnWidth + betweenMargin) / 2);
                pParams.setMarginStart(((this.mTagCloudWidth - (pParams.width / 2)) - (dualBtnHeight / 2)) - dualBtnMarginEnd);
                pParams.topMargin = pTop;
                this.mPeopleEffectLayout.setLayoutParams(pParams);
                ViewGroup.LayoutParams pParams1 = (FrameLayout.LayoutParams) this.mPeopleEffectView1st.getLayoutParams();
                pParams1.setMarginStart((int) (this.mPeopleEffectRadius * 1.5f));
                pParams1.topMargin = (int) (this.mPeopleEffectRadius * 1.5f);
                this.mPeopleEffectView1st.setLayoutParams(pParams1);
                ViewGroup.LayoutParams pParams2 = (FrameLayout.LayoutParams) this.mPeopleEffectView2nd.getLayoutParams();
                pParams2.setMarginStart((int) (this.mPeopleEffectRadius * 1.5f));
                pParams2.topMargin = (int) (this.mPeopleEffectRadius * 1.5f);
                this.mPeopleEffectView2nd.setLayoutParams(pParams2);
            }
        }
    }

    protected void applyScene(ISceneCategory sceneCategory, boolean useEffect) {
        int i = 1;
        if (resetScene()) {
            String category = sceneCategory.category;
            String displayName = sceneCategory.displayName;
            if ("portrait".equals(category)) {
                if (this.mGet.isRearCamera()) {
                    this.mSetFilterName = "person_rear";
                } else {
                    this.mSetFilterName = "person_front";
                }
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_PET.equals(category)) {
                this.mSetFilterName = ISceneCategory.CATEGORY_ID_PET;
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_FOOD.equals(category)) {
                this.mSetFilterName = ISceneCategory.CATEGORY_ID_FOOD;
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_SUNSET_SUNRISE.equals(category)) {
                this.mSetFilterName = "sunset_sunrise";
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_SEA.equals(category)) {
                this.mSetFilterName = "landscape";
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_LAKE.equals(category)) {
                this.mSetFilterName = "landscape";
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_CITY.equals(category)) {
                this.mSetFilterName = ISceneCategory.CATEGORY_ID_CITY;
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_FLOWER.equals(category)) {
                this.mSetFilterName = ISceneCategory.CATEGORY_ID_FLOWER;
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_STAR.equals(category)) {
                setBarVisibility(true, false, false);
            } else if (ISceneCategory.CATEGORY_ID_SILHOUETTE.equals(category)) {
                setBarVisibility(false, true, false);
            } else if (ISceneCategory.CATEGORY_ID_TEXT.equals(category)) {
                setBarVisibility(false, false, true);
            } else if (ISceneCategory.CATEGORY_ID_GOP.equals(category)) {
                if (this.mGet.isRearCamera()) {
                    this.mSetFilterName = "people_rear";
                } else {
                    this.mSetFilterName = "people_front";
                }
                setBarVisibility(false, false, false);
                startPeopleEffect();
            } else if (ISceneCategory.CATEGORY_ID_BABY.equals(category)) {
                if (this.mGet.isRearCamera()) {
                    this.mSetFilterName = "baby_rear";
                } else {
                    this.mSetFilterName = "baby_front";
                }
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_ANIMAL.equals(category)) {
                this.mSetFilterName = ISceneCategory.CATEGORY_ID_ANIMAL;
                setBarVisibility(false, false, false);
            } else if ("beverage".equals(category)) {
                this.mSetFilterName = "beverage";
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_FRUIT.equals(category)) {
                this.mSetFilterName = ISceneCategory.CATEGORY_ID_FRUIT;
                setBarVisibility(false, false, false);
            } else if ("snow".equals(category)) {
                this.mSetFilterName = "snow";
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_SKY.equals(category)) {
                if (ISceneCategory.CATEGORY_DISPLAY_LANDSCAPE.equals(displayName)) {
                    this.mSetFilterName = "landscape";
                } else {
                    this.mSetFilterName = ISceneCategory.CATEGORY_ID_SKY;
                }
                setBarVisibility(false, false, false);
            } else if ("beach".equals(category)) {
                this.mSetFilterName = "beach";
                setBarVisibility(false, false, false);
            } else if (ISceneCategory.CATEGORY_ID_LOW_LIGHT.equals(category)) {
                this.mSetFilterName = "lowlight";
                setBarVisibility(false, false, false);
            } else if ("auto".equals(category)) {
                this.mSetFilterName = "basic";
                setBarVisibility(false, false, false);
            } else {
                setBarVisibility(false, false, false);
            }
            CamLog.m7i(CameraConstants.TAG, "AI-current category : " + category + ", -FilterName : " + this.mSetFilterName);
            if (!ISceneCategory.CATEGORY_UNKNOWN.equals(category)) {
                setSceneBtnResource(displayName);
                if (useEffect) {
                    AudioUtil.performHapticFeedback(this.mSceneBtn, 65594);
                    this.mFilterEffectCover.setVisibility(0);
                    AnimationUtil.startSmartCamAnimation(this.mFilterEffectCover, this.mCoverAnimListener);
                }
            }
            if (!ISceneCategory.CATEGORY_UNKNOWN.equals(category)) {
                i = 0;
            }
            startTimerForSmartCamScene(i);
            return;
        }
        CamLog.m7i(CameraConstants.TAG, "AI-applyScene - resetScene was return false. return.");
    }

    public boolean resetScene() {
        if (this.mSceneBtn == null || this.mSceneBtnImage == null || this.mSceneBtnText == null || this.mFilterEffectCover == null || this.mEVBar == null || this.mEVBarLayout == null || this.mContrastBar == null || this.mContrastBarLayout == null || this.mSmartCamInterface == null) {
            return false;
        }
        if (this.mGet.isTimerShotCountdown() || !this.mGet.checkModuleValidate(192) || !SmartcamUtil.isSmartcamBindService() || !this.mIsRegisterCallback) {
            return false;
        }
        this.mSmartCamInterface.applyFilterToSceneTextSelected(CameraConstants.FILM_SMARTCAM_NONE);
        this.mSetFilterName = null;
        showSmartCamBar(this.mContrastBarLayout, this.mContrastBar, false);
        this.mContrastBar.initValue();
        this.mSmartCamInterface.updateContrastParam(0);
        showSmartCamBar(this.mEVBarLayout, this.mEVBar, false);
        this.mEVBar.initValue();
        this.mSmartCamInterface.setEVParam(0);
        this.mSceneBtn.setVisibility(8);
        this.mSceneBtn.setSelected(false);
        this.mSceneBtnImage.setSelected(false);
        this.mSceneBtnText.setText("");
        this.mSceneBtnText.setSelected(false);
        this.mSmartCamInterface.isStartWideAngleAnimation(false);
        stopPeopleEffect(true);
        return true;
    }

    protected void setBarVisibility(boolean nightSky, boolean silhouette, boolean text) {
        if (this.mEVBar != null && this.mEVBarLayout != null && this.mContrastBar != null && this.mContrastBarLayout != null && this.mSmartCamInterface != null) {
            this.mEVBar.setEnabled(false);
            this.mContrastBar.setEnabled(false);
            showSmartCamBar(this.mEVBarLayout, this.mEVBar, false);
            showSmartCamBar(this.mContrastBarLayout, this.mContrastBar, false);
            if (nightSky) {
                this.mEVBar.setEnabled(true);
            } else if (silhouette) {
                this.mContrastBar.configVar(0, 20, 10);
                this.mContrastBar.setEnabled(true);
            } else if (text) {
                this.mContrastBar.configVar(0, 400, 200);
                this.mSmartCamInterface.setAutoContrastSolution(true);
                this.mSceneBtn.setSelected(false);
                this.mContrastBar.setEnabled(true);
                return;
            }
            this.mSmartCamInterface.setAutoContrastSolution(false);
            this.mSceneBtn.setSelected(false);
        }
    }

    private void setSceneBtnResource(String displayName) {
        if (this.mSceneBtn != null && this.mSceneBtnText != null && this.mSceneBtnImage != null) {
            int resourceId = 0;
            int textId = 0;
            if (ISceneCategory.CATEGORY_DISPLAY_PORTRAIT.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_person_button;
                textId = C0088R.string.sp_lglens_tag_person_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_PET.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_pet_button;
                textId = C0088R.string.sp_lglens_tag_pet_NORMAL2;
            } else if (ISceneCategory.CATEGORY_DISPLAY_CLOSEUP_FLOWER.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_flower_button;
                textId = C0088R.string.sp_lglens_tag_flower_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_CITY.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_city_button;
                textId = C0088R.string.sp_lglens_tag_city_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_FOOD.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_food_button;
                textId = C0088R.string.sp_lglens_tag_food_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_text_button;
                textId = C0088R.string.sp_lglens_tag_text_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_LANDSCAPE.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_landscape_button;
                textId = C0088R.string.sp_lglens_tag_landscape_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_GOP.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_person_button;
                textId = C0088R.string.sp_lglens_tag_person_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_STAR.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_nightsky_button;
                textId = C0088R.string.sp_lglens_tag_night_sky_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_SUNSET_SUNRISE.equals(displayName)) {
                resourceId = isAMTime() ? C0088R.drawable.btn_smartcam_sunrise_button : C0088R.drawable.btn_smartcam_sunset_button;
                textId = isAMTime() ? C0088R.string.sp_lglens_tag_sunrise_NORMAL : C0088R.string.sp_lglens_tag_sunset_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_SILHOUETTE.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_silhoutte_button;
                textId = C0088R.string.sp_lglens_tag_silhouette_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_BABY.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_baby_button;
                textId = C0088R.string.cine_filter_tag_baby;
            } else if (ISceneCategory.CATEGORY_DISPLAY_ANIMAL.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_animal_button;
                textId = C0088R.string.sp_lglens_tag_animal_NORMAL;
            } else if ("beverage".equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_beverage_button;
                textId = C0088R.string.sp_lglens_tag_beverage_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_FRUIT.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_fruit_button;
                textId = C0088R.string.sp_lglens_tag_fruit_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_SNOW.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_snow_button;
                textId = C0088R.string.sp_lglens_tag_snow_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_SKY.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_sky_button;
                textId = C0088R.string.sp_lglens_tag_sky_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_BEACH.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_beach_button;
                textId = C0088R.string.sp_lglens_tag_beach_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_LOW_LIGHT.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_nightlight_button;
                textId = C0088R.string.sp_lglens_tag_low_light_NORMAL;
            } else if (ISceneCategory.CATEGORY_DISPLAY_AUTO.equals(displayName)) {
                resourceId = C0088R.drawable.btn_smartcam_auto_button;
                textId = C0088R.string.aicam_auto_scene;
            }
            if (resourceId == 0 || textId == 0) {
                CamLog.m3d(CameraConstants.TAG, "AI-resource null. return.");
                return;
            }
            this.mSceneBtn.setContentDescription(this.mGet.getAppContext().getString(textId));
            this.mSceneBtn.setVisibility(0);
            this.mSceneBtnImage.setImageResource(resourceId);
            this.mSceneBtnText.setTypeface(Typeface.DEFAULT);
            this.mSceneBtnText.setText(this.mGet.getAppContext().getString(textId));
            this.mSceneBtnText.setEllipsize(TruncateAt.END);
        }
    }

    public String getCurrentSceneDisplayName() {
        return this.mCurrentDisplayName;
    }

    private void startSmartCamTagCloudAnimation(final RotateLayout view) {
        AnimationUtil.startSmartCamTagCloudAnimation(view, 3000, new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (view != null) {
                    view.setVisibility(0);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (view != null) {
                    view.setVisibility(8);
                }
                SmartCamManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(SmartCamManagerBase.this) {
                    public void handleRun() {
                        if (view != null && SmartCamManagerBase.this.mTagCloudLayout != null) {
                            SmartCamManagerBase.this.mTagCloudLayout.removeView(view);
                        }
                    }
                }, 500);
            }
        });
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mTagCloudLayout != null && this.mSceneBtnRotate != null) {
            int i;
            RotateLayout rotate;
            if (degree != this.mBeforeDegree) {
                for (i = 0; i < this.mTagCloudLayout.getChildCount(); i++) {
                    rotate = (RotateLayout) this.mTagCloudLayout.getChildAt(i);
                    if (rotate != null) {
                        rotate.clearAnimation();
                    }
                }
                this.mTagCloudLayout.removeAllViews();
            }
            int beforeDegree = this.mSceneBtnRotate.getAngle();
            this.mSceneBtnRotate.clearAnimation();
            AnimationUtil.startRotateAnimationForRotateLayout(this.mSceneBtnRotate, beforeDegree, degree, false, 300, null);
            this.mSceneBtnRotate.rotateLayout(degree);
            for (i = 0; i < this.mTagCloudLayout.getChildCount(); i++) {
                rotate = (RotateLayout) this.mTagCloudLayout.getChildAt(i);
                if (rotate != null) {
                    rotate.rotateLayout(degree);
                }
            }
            if (!(this.mPeopleEffectText == null || this.mDualBtnRect == null)) {
                this.mPeopleEffectText.rotateLayout(degree);
                LayoutParams lp = (LayoutParams) this.mPeopleEffectText.getLayoutParams();
                if (degree == 0 || degree == 180) {
                    lp.setMargins(0, RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.117f), 0, 0);
                    lp.addRule(14, -1);
                } else {
                    lp.removeRule(14);
                    int leftMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.snap_movie_guide_text_marginBottom_land);
                    this.mPeopleEffectText.measure(0, 0);
                    lp.setMargins(leftMargin, this.mDualBtnRect.top + (((this.mDualBtnRect.bottom - this.mDualBtnRect.top) - this.mPeopleEffectText.getMeasuredHeight()) / 2), 0, 0);
                }
                this.mPeopleEffectText.setLayoutParams(lp);
            }
            this.mBeforeDegree = degree;
        }
    }

    private void sendEyeemServiceSmartCam() {
        if (this.mIEllieVision != null && SmartcamUtil.isSmartcamBindService() && this.mIsRegisterCallback && this.mOutRGBArray != null && !this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !this.mGet.isActivatedQuickview() && !this.mGet.isActivatedQuickdetailView() && !this.mGet.isTimerShotCountdown() && this.mGet.getIntervalshotVisibiity() != 0 && checkParcelableDataSize(this.mOutRGBArray.length) && this.mGet.checkModuleValidate(240) && this.mIsBinningState <= 0) {
            IBitmap iPcData = new IBitmap();
            iPcData.setImage(this.mOutRGBArray);
            if (iPcData != null) {
                CamLog.m3d(CameraConstants.TAG, "AI-sendEyeemServiceSmartCam feed frame, size : " + this.mOutRGBArray.length);
                try {
                    this.mIEllieVision.requestRecognizeByte(iPcData, this.mTargetWidth, this.mTargetHeight, this.mGet.isRearCamera() ? this.mGet.getOrientationDegree() + 90 : 360 - (this.mGet.getOrientationDegree() + 90));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void insertRecognitionText(final boolean fullConst) {
        if (this.mIRecognitionResults != null && this.mIRecognitionResults.getIRecognitions() != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SmartCamManagerBase.this.mIRecognitionResults == null || SmartCamManagerBase.this.mSmartCamTagCloudList == null || SmartCamManagerBase.this.mShowTextViewThread == null || SmartCamManagerBase.this.mShowTextViewThread.isInterrupted() || SmartCamManagerBase.this.mGet.isTimerShotCountdown()) {
                        CamLog.m11w(CameraConstants.TAG, "AI-insertRecognitionText return.");
                        return;
                    }
                    List<IRecognition> resultList = SmartCamManagerBase.this.mIRecognitionResults.getIRecognitions();
                    if (!(resultList == null || SmartCamManagerBase.this.mSmartCamTagCloudList == null)) {
                        SmartCamManagerBase.this.mSmartCamTagCloudList.clear();
                    }
                    if (!SmartCamManagerBase.this.makeTagCloudArray(resultList, fullConst ? 20 : 3) || !fullConst) {
                        CamLog.m11w(CameraConstants.TAG, "AI-makeTagCloudArray is empty.");
                    } else if (SmartCamManagerBase.this.mSmartCamTagCloudList.size() < 12) {
                        int remainSize = 12 - SmartCamManagerBase.this.mSmartCamTagCloudList.size();
                        int checkCnt = 0;
                        while (SmartCamManagerBase.this.mSmartCamTagCloudList.size() < 12 && checkCnt < 12) {
                            if (SmartCamManagerBase.this.makeTagCloudArray(resultList, remainSize)) {
                                checkCnt++;
                            } else {
                                return;
                            }
                        }
                    }
                    CamLog.m7i(CameraConstants.TAG, "AI-addCloude size count : " + SmartCamManagerBase.this.mSmartCamTagCloudList.size() + ", fullConstruction : " + fullConst);
                }
            });
        }
    }

    protected boolean makeTagCloudArray(List<IRecognition> resultList, int tagListSize) {
        if (resultList == null) {
            CamLog.m11w(CameraConstants.TAG, "AI-makeTagCloudArray resultList is null, return.");
            return false;
        }
        boolean ret_val = false;
        int i = 0;
        while (i < tagListSize && i < resultList.size()) {
            IRecognition recog = (IRecognition) resultList.get(i);
            if (recog == null) {
                return false;
            }
            if (recog.getConfidence() >= TAGCLOUD_TEXT_CONFIDENCE_VALUE) {
                this.mSmartCamTagCloudList.add(new SmartCamTagCloudList(recog.getId(), recog.getTitle(), recog.getConfidence()));
                ret_val = true;
            }
            i++;
        }
        return ret_val;
    }

    protected void restoreLatestScene() {
        if (this.mLatestDisplayName == null) {
            this.mLatestFilterName = null;
            this.mLatestFilterIndex = -1;
            return;
        }
        if (!(this.mEVBar == null || this.mEVBarLayout == null || this.mContrastBar == null || this.mContrastBarLayout == null || this.mSmartCamInterface == null)) {
            CamLog.m7i(CameraConstants.TAG, "AI-restoreLatestScene, mLatestDisplayName : " + this.mLatestDisplayName);
            if (ISceneCategory.CATEGORY_DISPLAY_STAR.equals(this.mLatestDisplayName) || ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(this.mLatestDisplayName) || ISceneCategory.CATEGORY_DISPLAY_SILHOUETTE.equals(this.mLatestDisplayName)) {
                if (this.mLatestDisplayName.equals(this.mCurrentDisplayName)) {
                    if (ISceneCategory.CATEGORY_DISPLAY_STAR.equals(this.mLatestDisplayName)) {
                        this.mEVBar.setCurVarValue(this.mLatestEVBarParamValue + this.mMaxEvParamValue);
                        this.mSmartCamInterface.setEVParam(this.mLatestEVBarParamValue);
                    } else if (ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(this.mLatestDisplayName)) {
                        this.mContrastBar.setCurVarValue(this.mLatestTextBarParamValue);
                        this.mSmartCamInterface.updateAutoContrastSolution(this.mLatestTextBarParamValue);
                    } else if (ISceneCategory.CATEGORY_DISPLAY_SILHOUETTE.equals(this.mLatestDisplayName)) {
                        this.mContrastBar.setCurVarValue(this.mLatestSilContrastBarParamValue);
                        this.mSmartCamInterface.updateContrastParam(this.mLatestSilContrastBarParamValue);
                    }
                }
                this.mLatestDisplayName = null;
                this.mLatestEVBarParamValue = 0;
                this.mLatestTextBarParamValue = 0;
                this.mLatestSilContrastBarParamValue = 0;
                return;
            }
            this.mLatestEVBarParamValue = 0;
            this.mLatestTextBarParamValue = 0;
            this.mLatestSilContrastBarParamValue = 0;
        }
        if (this.mLatestFilterName == null || ISceneCategory.CATEGORY_UNKNOWN.equals(this.mLatestDisplayName) || !this.mLatestDisplayName.equals(this.mCurrentDisplayName)) {
            CamLog.m7i(CameraConstants.TAG, "AI-you don't remember last filter.");
            this.mLatestFilterIndex = -1;
        }
        this.mLatestFilterName = null;
        this.mLatestDisplayName = null;
    }

    private boolean checkParcelableDataSize(int size) {
        return size < 1048576;
    }

    public void resetSceneFromMotionHandShake() {
        CamLog.m3d(CameraConstants.TAG, "AI-resetSceneFromMotionHandShake");
        this.mGet.removePostRunnable(this.mHandShakeResetRunnable);
        this.mGet.runOnUiThread(this.mHandShakeResetRunnable);
    }

    public void stopTimerForSmartCamScene() {
        if (this.mRecognitionThread != null) {
            this.mRecognitionThread.setTimerSkip(true);
            CamLog.m3d(CameraConstants.TAG, "AI-stopTimerForSmartCamScene setTimerSkip : true");
        }
    }

    public void startTimerForSmartCamScene(int useLocalTime) {
        if (this.mRecognitionThread != null) {
            CamLog.m3d(CameraConstants.TAG, "AI-startTimerForSmartCamScene setTimer : 3000 , setTimerSkip : false, useLocalTime : " + useLocalTime);
            long time = useLocalTime == -1 ? 0 : useLocalTime == 0 ? 3000 : 1000;
            this.mRecognitionThread.setTimerSkip(false);
            this.mRecognitionThread.setTimer(time);
        }
    }

    public boolean checkDeviceAction() {
        if (!this.mGet.isModuleChanging() && !this.mGet.isCameraChanging() && !this.mGet.isAnimationShowing() && !this.mGet.isTimerShotCountdown() && this.mGet.checkModuleValidate(31)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "AI-checkDeviceAction, false., mGet.isModuleChanging() : " + this.mGet.isModuleChanging() + ", mGet.isCameraChanging() : " + this.mGet.isCameraChanging() + ", mGet.isAnimationShowing() : " + this.mGet.isAnimationShowing() + ", mGet.isTimerShotCountdown() : " + this.mGet.isTimerShotCountdown() + ", val-all, capture : " + this.mGet.checkModuleValidate(31));
        return false;
    }

    private boolean isAMTime() {
        boolean z;
        int amPm = Calendar.getInstance().get(9);
        String str = CameraConstants.TAG;
        StringBuilder append = new StringBuilder().append("AI-isAMTime : ");
        if (amPm == 0) {
            z = true;
        } else {
            z = false;
        }
        CamLog.m3d(str, append.append(z).toString());
        if (amPm == 0) {
            return true;
        }
        return false;
    }

    public void turnOnBinning(boolean lowLightOn, int whereFrom) {
        int curBinningState = whereFrom == 1 ? lowLightOn ? 1 : 0 : lowLightOn ? 2 : 0;
        if (this.mIsBinningState != curBinningState) {
            CamLog.m11w(CameraConstants.TAG, "AI-turnOnBinning on : " + this.mIsBinningState + ", current on : " + lowLightOn + ", stateCondition : " + whereFrom + ", mCurrentCategory : " + this.mCurrentCategory);
            this.mIsBinningState = curBinningState;
            if (this.mIsRegisterCallback) {
                boolean useEffect;
                if (whereFrom != 0 || ISceneCategory.CATEGORY_ID_LOW_LIGHT.equals(this.mCurrentCategory)) {
                    useEffect = false;
                } else {
                    useEffect = true;
                }
                if (lowLightOn) {
                    applyScene(new ISceneCategory(ISceneCategory.CATEGORY_ID_LOW_LIGHT, ISceneCategory.CATEGORY_DISPLAY_LOW_LIGHT), useEffect);
                    this.mCurrentCategory = ISceneCategory.CATEGORY_ID_LOW_LIGHT;
                    this.mCurrentDisplayName = ISceneCategory.CATEGORY_DISPLAY_LOW_LIGHT;
                    if (this.mSmartCamTagCloudList != null) {
                        this.mSmartCamTagCloudList.clear();
                        insertRecognitionText(false);
                    }
                    if (this.mLatestDisplayName == null || this.mLatestFilterName == null || ISceneCategory.CATEGORY_UNKNOWN.equals(this.mLatestDisplayName) || !ISceneCategory.CATEGORY_DISPLAY_LOW_LIGHT.equals(this.mLatestDisplayName)) {
                        CamLog.m7i(CameraConstants.TAG, "AI-turnOnBinning, you don't remember last filter.");
                        this.mLatestFilterIndex = -1;
                    }
                    this.mLatestFilterName = null;
                    this.mLatestDisplayName = null;
                    return;
                }
                if (this.mSmartCamInterface != null && this.mSmartCamInterface.isShowingFilmMenu()) {
                    this.mSmartCamInterface.showFilmMenu(false);
                }
                applyScene(new ISceneCategory(ISceneCategory.CATEGORY_UNKNOWN, ISceneCategory.CATEGORY_UNKNOWN), useEffect);
                this.mCurrentCategory = ISceneCategory.CATEGORY_UNKNOWN;
                this.mCurrentDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
                insertRecognitionText(true);
            }
        }
    }

    protected void registerSmartCamCallback() {
        if (this.mIEllieVision != null && !this.mIsRegisterCallback && this.mIsBinningState != 1 && SmartcamUtil.isSmartcamBindService() && this.mOutRGBArray != null) {
            CamLog.m3d(CameraConstants.TAG, "AI-registerCallback, mIsBinningState : " + this.mIsBinningState + ", mCurrentCategory :" + this.mCurrentCategory);
            try {
                this.mIEllieVision.registerCallback(this.mEllieServiceCallback, ELLIE_SERVICE_USAGE);
                this.mIsRegisterCallback = true;
                if (this.mIsBinningState == 2) {
                    this.mGet.runOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            if (SmartCamManagerBase.this.mIsBinningState == 2) {
                                SmartCamManagerBase.this.applyScene(new ISceneCategory(ISceneCategory.CATEGORY_ID_LOW_LIGHT, ISceneCategory.CATEGORY_DISPLAY_LOW_LIGHT), true);
                                SmartCamManagerBase.this.mCurrentCategory = ISceneCategory.CATEGORY_ID_LOW_LIGHT;
                                SmartCamManagerBase.this.mCurrentDisplayName = ISceneCategory.CATEGORY_DISPLAY_LOW_LIGHT;
                                if (SmartCamManagerBase.this.mSmartCamTagCloudList != null) {
                                    SmartCamManagerBase.this.mSmartCamTagCloudList.clear();
                                    SmartCamManagerBase.this.insertRecognitionText(false);
                                }
                            }
                        }
                    });
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            }
        }
    }

    protected boolean isOverlapRect(Rect rect1, Rect rect2) {
        if (rect1 == null || rect2 == null || rect1.left >= rect2.left + rect2.width() || rect1.left + rect1.width() <= rect2.left || rect1.top >= rect2.top + rect2.height() || rect1.top + rect1.height() <= rect2.top) {
            return false;
        }
        return true;
    }
}
