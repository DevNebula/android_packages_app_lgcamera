package com.lge.camera.managers.ext.sticker;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.p001v7.widget.LinearLayoutManager;
import android.support.p001v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.app.AttachCameraModule;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraInfomation;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.ext.sticker.StickerGLSurfaceView.CameraReadyListener;
import com.lge.camera.managers.ext.sticker.StickerManagerState.State;
import com.lge.camera.managers.ext.sticker.StickerTabAdapter.TabItemCallback;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.Callback;
import com.lge.camera.managers.ext.sticker.recentdb.RecentDBHelper;
import com.lge.camera.managers.ext.sticker.recentdb.RecentUsedStickerAdapter;
import com.lge.camera.managers.ext.sticker.solutions.ContentsInformation;
import com.lge.camera.managers.ext.sticker.solutions.StickerAdapter;
import com.lge.camera.managers.ext.sticker.solutions.StickerAdapter.OnStickerItemClickListener;
import com.lge.camera.managers.ext.sticker.solutions.StickerAdapter.OnStickerItemLoadCompleteListener;
import com.lge.camera.managers.ext.sticker.utils.StickerUtil;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.SizePrefMaker;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StickerManager extends ManagerInterfaceImpl implements CameraReadyListener, TabItemCallback, OnClickListener, OnStickerItemLoadCompleteListener, OnStickerItemClickListener, Callback {
    private static final int MSG_CLEAR_RENDERING = 6;
    private static final int MSG_REQUEST_HIDE_ACTION_TEXT = 4;
    private static final int MSG_REQUEST_HIDE_ALL_TEXT = 5;
    private static final int MSG_REQUEST_HIDE_GUIDE_TEXT = 2;
    private static final int MSG_REQUEST_SHOW_ACTION_TEXT = 3;
    private static final int MSG_REQUEST_SHOW_GUIDE_TEXT = 1;
    private static final int MSG_SET_STICKER_DELAYED = 7;
    public static final String[] PREVIEW_SIZE_STRINGS_MID = new String[]{"960x720", "1280x720", ParamConstants.VIDEO_1440_BY_720, "720x720"};
    public static final String[] PREVIEW_SIZE_STRINGS_NOTCH = new String[]{"1440x1080", "1920x1080", "2268x1080", "1080x1080"};
    public static final String[] PREVIEW_SIZE_STRINGS_PREMEUM = new String[]{"1440x1080", "1920x1080", "2160x1080", "1080x1080"};
    private static final int SET_STICKER_DELAY = 300;
    static final String TAG = "StickerManager";
    public static final int USE_SOLUTION = 2;
    private static HashMap<Integer, Integer> orientationMap = new HashMap();
    private static HashMap<Integer, int[]> pictureSizeMap = new HashMap();
    private static int sLeftMargin = 0;
    private final int GUIDE_TEXT_DISPLAY_TIME = 2000;
    private final int GUIDE_TEXT_SHOWING_TIME = 3000;
    private boolean isAttachVideo = false;
    private boolean isCameraSwitchingStarted = false;
    private RotateLayout mActionLayout;
    private TextView mActionText;
    private StickerAdapter mAdapter;
    private RotateImageView mClearButton;
    private int mClickedTabIndex;
    private int[] mCurrentPreviewSize = new int[]{0, 0};
    private boolean mDetectedFace = false;
    private View mDummyView;
    private StickerGLSurfaceView mGLSurfaceView;
    private RecyclerView mGridView;
    private int mGridViewHeight = 0;
    private RotateLayout mGuideLayout;
    private Handler mGuideTextHandler = new Handler(new C13461());
    boolean mIsTouchDown = false;
    boolean mIsTouchMoving = false;
    private boolean mLayoutVisibility = false;
    private long mMaxVideoFileSize = 0;
    private HashMap<String, LayoutParams> mParamPreset = new HashMap();
    private int mPreviousFaceCount = -1;
    private ProgressBar mProgress;
    private RecentUsedStickerAdapter mRecentAdapter;
    private StickerContentCallback mStickerContentCallback;
    private boolean mStickerGridVisiblity = false;
    private StickerManagerState mStickerManagerState = new StickerManagerState();
    private RelativeLayout mStickerMenu;
    private RotateLayout mStickerRotateLayout;
    private RelativeLayout mStickerTabLayout;
    private StickerTabAdapter mTabAdapter;
    private RelativeLayout mTabContainer;
    private RecyclerView mTablist;
    private int mTouchSlop = 100;
    /* renamed from: mX */
    private int f37mX;
    /* renamed from: mY */
    private int f38mY;
    private StickerManagerInterface smi;

    public interface StickerManagerInterface {
        ModuleInterface getModuleInterface();

        void setStickerDrawing(boolean z);
    }

    public interface StickerContentCallback {
        void onContentTaken(ContentsInformation contentsInformation);
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.StickerManager$1 */
    class C13461 implements Handler.Callback {

        /* renamed from: com.lge.camera.managers.ext.sticker.StickerManager$1$2 */
        class C13422 implements Runnable {
            C13422() {
            }

            public void run() {
                StickerManager.this.showGuideText();
            }
        }

        /* renamed from: com.lge.camera.managers.ext.sticker.StickerManager$1$3 */
        class C13433 implements Runnable {
            C13433() {
            }

            public void run() {
                StickerManager.this.hideGuideText();
            }
        }

        /* renamed from: com.lge.camera.managers.ext.sticker.StickerManager$1$4 */
        class C13444 implements Runnable {
            C13444() {
            }

            public void run() {
                StickerManager.this.hideActionText();
            }
        }

        /* renamed from: com.lge.camera.managers.ext.sticker.StickerManager$1$5 */
        class C13455 implements Runnable {
            C13455() {
            }

            public void run() {
                StickerManager.this.hideGuideText();
                StickerManager.this.hideActionText();
            }
        }

        C13461() {
        }

        public boolean handleMessage(Message msg) {
            CamLog.m3d(StickerManager.TAG, "what : " + msg.what);
            switch (msg.what) {
                case 1:
                    if (!(StickerManager.this.mGLSurfaceView == null || !StickerManager.this.mGLSurfaceView.isStickerDrawing() || StickerManager.this.mDetectedFace)) {
                        if (!StickerManager.this.mGet.isSettingMenuVisible() && !StickerManager.this.mGet.isModeMenuVisible() && !StickerManager.this.mGet.isHelpListVisible()) {
                            StickerManager.this.getActivity().runOnUiThread(new C13422());
                            break;
                        }
                        StickerManager.this.mGuideTextHandler.removeMessages(1);
                        if (!(StickerManager.this.mGuideTextHandler.hasMessages(2) || StickerManager.this.mGuideTextHandler.hasMessages(5))) {
                            StickerManager.this.mGuideTextHandler.sendEmptyMessageDelayed(1, 500);
                            break;
                        }
                    }
                    break;
                case 2:
                    StickerManager.this.getActivity().runOnUiThread(new C13433());
                    break;
                case 3:
                    if (StickerManager.this.mGLSurfaceView != null && StickerManager.this.mGLSurfaceView.isStickerDrawing()) {
                        final String textMsg = msg.obj;
                        StickerManager.this.getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                StickerManager.this.showActionText(textMsg);
                                StickerManager.this.mGuideTextHandler.sendEmptyMessageDelayed(4, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                            }
                        });
                        break;
                    }
                case 4:
                    StickerManager.this.getActivity().runOnUiThread(new C13444());
                    break;
                case 5:
                    StickerManager.this.getActivity().runOnUiThread(new C13455());
                    break;
                case 6:
                    if (StickerManager.this.mGLSurfaceView != null) {
                        StickerManager.this.mGLSurfaceView.requestRender();
                        break;
                    }
                    break;
                case 7:
                    if (StickerManager.this.mGLSurfaceView != null) {
                        StickerManager.this.mGLSurfaceView.setLastSticker();
                        break;
                    }
                    break;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.StickerManager$2 */
    class C13472 implements Runnable {
        C13472() {
        }

        public void run() {
            StickerManager.this.mProgress.setVisibility(8);
        }
    }

    public String getSupportedPreviewSize() {
        String[] previewSize;
        boolean useVideoSize = false;
        if ((this.mGet instanceof AttachCameraModule) && this.mGet.isVideoCaptureMode()) {
            if (this.mGet.isMMSIntent() || this.mGet.isMMSRecording()) {
                CamLog.m3d(TAG, "getSupportedPreviewSize use mms attach video");
                String preview = CameraConstants.VGA_RESOLUTION;
                try {
                    preview = CameraDeviceUtils.getPreviewSizeforMMSVideo(getAppContext(), ((ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()))).getValue().split("@")[0].split("@")[0]);
                    CamLog.m3d(TAG, "Check support sticker preview size in attach = " + preview);
                } catch (Exception e) {
                    CamLog.m5e(TAG, "Check support sticker preview size in attach exception so use default = " + preview);
                    preview = CameraConstants.VGA_RESOLUTION;
                }
                this.mCurrentPreviewSize = Utils.sizeStringToArray(preview);
                return preview;
            }
            CamLog.m3d(TAG, "getSupportedPreviewSize use not mms attach video");
            useVideoSize = true;
        }
        if (ModelProperties.getLCDType() == 2) {
            previewSize = PREVIEW_SIZE_STRINGS_NOTCH;
        } else if (ModelProperties.getAppTier() >= 5) {
            previewSize = PREVIEW_SIZE_STRINGS_PREMEUM;
        } else {
            previewSize = PREVIEW_SIZE_STRINGS_MID;
        }
        String ratioSize = this.mGet.getCurrentSelectedPictureSize();
        if (useVideoSize) {
            ratioSize = this.mGet.getCurrentSelectedVideoSize();
        }
        switch (SizePrefMaker.calculateRatio(ratioSize)) {
            case 1:
                ratioSize = previewSize[3];
                break;
            case 2:
                ratioSize = previewSize[0];
                break;
            case 5:
                ratioSize = previewSize[1];
                break;
            case 6:
                ratioSize = previewSize[2];
                break;
            default:
                ratioSize = previewSize[2];
                CamLog.m5e(TAG, "Check support sticker preview size");
                break;
        }
        this.mCurrentPreviewSize = Utils.sizeStringToArray(ratioSize);
        CamLog.m3d(TAG, "ratioSize : " + ratioSize);
        return ratioSize;
    }

    public void onItemClicked(StickerInformationDataClass data, int position) {
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.changeSticker(data);
            if (data == null) {
                this.mGuideTextHandler.removeMessages(1);
                this.mGuideTextHandler.removeMessages(3);
                this.mGuideTextHandler.sendEmptyMessage(5);
            }
            RecentDBHelper helper = RecentDBHelper.getInstance(getAppContext());
            if (helper != null) {
                helper.insertOrUpdate(data);
            }
        }
    }

    public void onItemLoadComplete(ArrayList<StickerInformationDataClass> al) {
        CamLog.m3d(TAG, "");
        if (al != null && al.size() > 0) {
            this.mGLSurfaceView.changeSticker((StickerInformationDataClass) al.get(0));
        }
    }

    public void onDecompressComplete(String decompressPath) {
        CamLog.m5e("DecompressScheduler", "onDecompressComplete called : " + decompressPath);
        if (this.mTabAdapter != null) {
            this.mTabAdapter.loadOne(decompressPath);
        }
    }

    public void onDecompressStarted(String decompressPath) {
        CamLog.m5e("DecompressScheduler", "onDecompressStarted called : " + decompressPath);
    }

    public void onPreloadDecompressComplete(boolean success) {
        CamLog.m5e("DecompressScheduler", "onPreloadDecompressComplete + success = " + success);
        if (this.mProgress != null) {
            getActivity().runOnUiThread(new C13472());
        }
        if (this.mTabAdapter != null && success) {
            setDegreeAdpaterJustSet(this.mGet.getOrientationDegree());
            this.mTabAdapter.loadList();
        }
    }

    public void onPreloadDecompressStart() {
        CamLog.m5e("DecompressScheduler", "onPreloadDecompressStart + rollin babe~~");
    }

    public boolean isStickerGridViewShowing() {
        CamLog.m3d(TAG, "");
        return isStickerGridVisible();
    }

    public StickerManager(ModuleInterface stickerManagerInterface) {
        super(stickerManagerInterface);
        this.smi = (StickerManagerInterface) stickerManagerInterface;
        CamLog.m5e(TAG, "manager created stickerManagerInterface = " + stickerManagerInterface);
        if ((stickerManagerInterface instanceof AttachCameraModule) && stickerManagerInterface.isMMSIntent() && stickerManagerInterface.isVideoCaptureMode()) {
            this.isAttachVideo = true;
            this.mMaxVideoFileSize = MmsProperties.getMmsVideoSizeLimit(this.mGet.getAppContext().getContentResolver());
            Bundle getExBundle = getActivity().getIntent().getExtras();
            if (getExBundle == null) {
                CamLog.m3d(TAG, "intent.getExtras() is null. assume no limit.");
                this.mMaxVideoFileSize = 0;
            } else {
                this.mMaxVideoFileSize = getExBundle.getLong("android.intent.extra.sizeLimit", 0);
                if (this.mMaxVideoFileSize == 0) {
                    this.mMaxVideoFileSize = (long) getExBundle.getInt("android.intent.extra.sizeLimit", 0);
                }
                CamLog.m3d(TAG, String.format("requested file size limit: %d", new Object[]{Long.valueOf(this.mMaxVideoFileSize)}));
            }
        }
        this.mStickerManagerState.setState(State.STATE_CREATE);
        DecompressScheduler.getInstance(getAppContext()).setCallback(this);
        this.mTouchSlop = ViewConfiguration.get(getAppContext()).getScaledTouchSlop();
        CamLog.m3d(TAG, "manager created");
    }

    public boolean isRunning() {
        if (this.mStickerManagerState != null) {
            return this.mStickerManagerState.isStarted();
        }
        return false;
    }

    public boolean isRecording() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.isRecording();
        }
        return false;
    }

    public void init() {
        super.init();
        CamLog.m3d(TAG, "init called");
        ListPreference lp = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), 1));
        for (int i = 0; i < lp.getEntryValues().length; i++) {
            String key = SizePrefMaker.PICTURE_RATIO_STRINGS[SizePrefMaker.calculateRatio(lp.getEntryValues()[i].toString())];
            if (!this.mParamPreset.containsKey(key)) {
                int[] view_size = Utils.sizeStringToArray(lp.getExtraInfo(2, i));
                LayoutParams layoutParams = Utils.getRelativeLayoutParams(getAppContext(), view_size[0], view_size[1]);
                if (layoutParams != null) {
                    layoutParams.topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), view_size[0], view_size[1], 0);
                    CamLog.m3d(TAG, "presetted key ratio : " + key + " w : " + layoutParams.width + " h : " + layoutParams.height + " t : " + layoutParams.topMargin);
                    this.mParamPreset.put(key, layoutParams);
                }
            }
        }
        StickerUtil.copyTrackingData(getAppContext());
        this.mGLSurfaceView = (StickerGLSurfaceView) this.mGet.findViewById(C0088R.id.preview_glsurfaceview);
        if (this.mGLSurfaceView == null) {
            CamLog.m5e(TAG, "this init from old instance. so.... just return;");
        } else {
            this.mGLSurfaceView.setCameraReadyListener(this);
            this.mGLSurfaceView.setPhoneOrientationDegree(this.mGet.getOrientationDegree());
            sendAway();
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mDummyView = vg.findViewById(C0088R.id.dummyview);
        this.mProgress = (ProgressBar) vg.findViewById(C0088R.id.sticker_decompressing_progress);
        this.mStickerRotateLayout = (RotateLayout) vg.findViewById(C0088R.id.sticker_rotate_layout);
        this.mTabContainer = (RelativeLayout) this.mStickerRotateLayout.findViewById(C0088R.id.tab_container);
        LinearLayout.LayoutParams tabParam = (LinearLayout.LayoutParams) this.mTabContainer.getLayoutParams();
        tabParam.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.058f);
        this.mTabContainer.setLayoutParams(tabParam);
        this.mTablist = (RecyclerView) this.mStickerRotateLayout.findViewById(C0088R.id.sticker_tab_list);
        this.mTablist.setLayoutManager(new LinearLayoutManager(getAppContext(), 0, false));
        this.mClearButton = (RotateImageView) this.mStickerRotateLayout.findViewById(C0088R.id.sticker_clear_button);
        this.mClearButton.setOnClickListener(this);
        this.mStickerMenu = (RelativeLayout) vg.findViewById(C0088R.id.sticker_menu);
        this.mStickerTabLayout = (RelativeLayout) vg.findViewById(C0088R.id.sticker_tab_layout);
        this.mGridView = (RecyclerView) this.mStickerTabLayout.findViewById(C0088R.id.sticker_gridview);
        this.mGridView.setLayoutManager(new LinearLayoutManager(getAppContext(), 0, false));
        int[] display = Utils.getLCDsize(getAppContext(), true);
        this.mGridViewHeight = (int) (((float) Math.min(display[0], display[1])) / 5.5f);
        ViewGroup.LayoutParams lps = this.mDummyView.getLayoutParams();
        lps.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.278f) - this.mGridViewHeight;
        this.mDummyView.setLayoutParams(lps);
        LinearLayout.LayoutParams glp = (LinearLayout.LayoutParams) this.mGridView.getLayoutParams();
        glp.height = this.mGridViewHeight;
        this.mGridView.setLayoutParams(glp);
        this.mStickerManagerState.setState(State.STATE_LAYOUT_INIT);
        this.mGuideLayout = (RotateLayout) vg.findViewById(C0088R.id.sticker_guide_layout);
        this.mActionLayout = (RotateLayout) vg.findViewById(C0088R.id.sticker_action_layout);
        this.mActionText = (TextView) vg.findViewById(C0088R.id.sticker_action_text);
        this.mAdapter = new StickerAdapter(this.mGet.getAppContext(), this, 2, this.mGridViewHeight);
        this.mRecentAdapter = new RecentUsedStickerAdapter(this.mGet.getAppContext(), this, this.mGridViewHeight);
        this.mTabAdapter = new StickerTabAdapter(this.mGet.getAppContext(), this);
        this.mTablist.setAdapter(this.mTabAdapter);
    }

    public void startStickerApply(int leftMargin) {
        CamLog.m3d(TAG, "");
        sLeftMargin = leftMargin;
        setPresetForGLView();
        RecentDBHelper helper = RecentDBHelper.getInstance(getAppContext());
        if (helper != null) {
            helper.openDB();
        }
        if (helper == null || !helper.hasContents()) {
            this.mTabAdapter.setSelection(1);
            if (this.mAdapter.getItemCount() <= 0) {
                this.mAdapter.setmLoadListener(this);
                String tabPath = this.mTabAdapter.getSelectedStickerPackPath();
                if (tabPath != null) {
                    this.mAdapter.load(tabPath);
                } else {
                    return;
                }
            } else if (this.mGLSurfaceView != null) {
                this.mGLSurfaceView.changeSticker(this.mAdapter.getItem(0));
            }
        } else {
            CamLog.m3d(TAG, "has content!");
            if (this.mGLSurfaceView != null) {
                this.mGLSurfaceView.changeSticker((StickerInformationDataClass) helper.getRecentList().get(0));
            }
        }
        setGridSelection();
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.show();
        }
        this.mStickerManagerState.setState(State.STATE_START);
    }

    public void start(int leftMargin) {
        CamLog.m3d(TAG, "start called");
        this.smi.setStickerDrawing(false);
        sLeftMargin = leftMargin;
        setPresetForGLView();
        DecompressScheduler ds = DecompressScheduler.getInstance(getAppContext());
        if (!(ds == null || ds.getPrealoadPackageDecompressing() || !DecompressScheduler.needDecompressPreloadedContents(getAppContext()))) {
            ds.setCallback(this);
            ds.preloadedExcuteJob(getAppContext().getFilesDir().getAbsolutePath(), C0088R.raw.sticker);
            this.mProgress.setVisibility(0);
        }
        RecentDBHelper helper = RecentDBHelper.getInstance(getAppContext());
        if (helper != null) {
            helper.openDB();
        }
        showStickerLayout();
        showItemStickerLayoutInner();
        bringItAndShow();
        setDegree(this.mGet.getOrientationDegree(), false);
        this.mStickerManagerState.setState(State.STATE_START);
    }

    public void showGuideTextIfNeed() {
        if (this.mGuideLayout != null && this.mPreviousFaceCount < 1) {
            this.mGuideTextHandler.removeMessages(5);
            this.mGuideTextHandler.removeMessages(4);
            this.mGuideTextHandler.removeMessages(2);
            this.mGuideTextHandler.removeMessages(3);
            this.mGuideTextHandler.removeMessages(1);
            this.mGuideTextHandler.sendEmptyMessageDelayed(1, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
        }
    }

    public void showGuideText() {
        CamLog.m3d(TAG, "");
        if (this.mActionLayout != null && this.mActionLayout.getVisibility() == 0) {
            this.mActionLayout.setVisibility(8);
        }
        if (this.mGuideLayout != null) {
            this.mGuideLayout.setAngle(this.mGet.getOrientationDegree());
            this.mGuideLayout.setVisibility(0);
        }
    }

    public void showActionText(String guideString) {
        if (!this.mGet.isSettingMenuVisible() && !this.mGet.isModeMenuVisible()) {
            if (this.mGuideLayout != null && this.mGuideLayout.getVisibility() == 0) {
                this.mGuideLayout.setVisibility(8);
            }
            if (this.mActionLayout != null) {
                this.mActionLayout.setAngle(this.mGet.getOrientationDegree());
                if (guideString != null) {
                    this.mActionText.setVisibility(0);
                    this.mActionText.setText(guideString);
                }
                this.mActionLayout.setVisibility(0);
            }
        }
    }

    public void hideGuideText() {
        CamLog.m3d(TAG, "");
        if (this.mGuideLayout != null) {
            this.mGuideLayout.setVisibility(8);
        }
    }

    public void hideActionText() {
        if (this.mActionLayout != null) {
            this.mActionLayout.setVisibility(8);
        }
    }

    public boolean isStickerMenuVisible() {
        return this.mLayoutVisibility;
    }

    public boolean isStickerGridVisible() {
        return this.mStickerGridVisiblity;
    }

    private void setLayoutDummyViewVisibility(int visible) {
        if (this.mStickerRotateLayout != null) {
            this.mStickerRotateLayout.setVisibility(visible);
        }
        if (this.mDummyView != null) {
            this.mDummyView.setVisibility(visible);
        }
    }

    private void setMenuTabViewVisibility(int visible) {
        if (this.mStickerMenu != null) {
            this.mStickerMenu.setVisibility(visible);
        }
        if (this.mStickerTabLayout != null) {
            this.mStickerTabLayout.setVisibility(visible);
        }
    }

    public void hideItemStickerLayout() {
        CamLog.m3d(TAG, "");
        setDegree(this.mGet.getOrientationDegree(), false);
        if (this.mTabAdapter != null) {
            this.mTabAdapter.deleteOff();
        }
        setLayoutDummyViewVisibility(8);
        this.mStickerGridVisiblity = false;
    }

    private void showItemStickerLayoutInner() {
        CamLog.m3d(TAG, "");
        setLayoutDummyViewVisibility(0);
        this.mStickerGridVisiblity = true;
        DecompressScheduler ds = DecompressScheduler.getInstance(getAppContext());
        if (ds == null || ds.getPrealoadPackageDecompressing()) {
            setDegree(this.mGet.getOrientationDegree(), false);
            return;
        }
        setDegreeAdpaterJustSet(this.mGet.getOrientationDegree());
        this.mTabAdapter.loadList();
    }

    public void showItemStickerLayout() {
        CamLog.m3d(TAG, "");
        showItemStickerLayoutInner();
    }

    private void showStickerLayout() {
        CamLog.m3d(TAG, "");
        setDegree(this.mGet.getOrientationDegree(), false);
        setMenuTabViewVisibility(0);
        this.mLayoutVisibility = true;
    }

    private void hideStickerLayout() {
        CamLog.m3d(TAG, "");
        setMenuTabViewVisibility(4);
        this.mLayoutVisibility = true;
    }

    public void stop() {
        CamLog.m3d(TAG, "stop called");
        stop(false);
    }

    public void doInStartPreview() {
        CamLog.m3d(TAG, "doInStartPreview + ");
        if (this.mGLSurfaceView != null && this.mGLSurfaceView.hasSticker()) {
            bringItAndShow();
            setPresetForGLView();
        }
    }

    public void hideForChange() {
        this.mGuideTextHandler.removeMessages(1);
        this.mGuideTextHandler.removeMessages(3);
        this.mGuideTextHandler.sendEmptyMessage(5);
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.justHide();
            sendAway();
        }
    }

    public void stop(boolean clearSticker) {
        CamLog.m3d(TAG, "stop called clearSticker : " + clearSticker);
        if (clearSticker) {
            this.mDetectedFace = false;
            if (this.mGLSurfaceView != null) {
                this.mGLSurfaceView.clearSticker();
                clearGridSelection();
            }
        }
        if (this.mStickerMenu != null) {
            this.mStickerMenu.setVisibility(8);
        }
        this.mGuideTextHandler.removeMessages(1);
        this.mGuideTextHandler.removeMessages(3);
        this.mGuideTextHandler.sendEmptyMessage(5);
        RecentDBHelper.getInstance(getAppContext()).closeDB();
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.setLayoutAndPosition(0, 0, 0);
            this.mGLSurfaceView.hide();
            sendAway();
        }
        this.mStickerManagerState.setState(State.STATE_STOP);
    }

    private boolean processActionGuide(boolean show) {
        if (this.mGLSurfaceView == null) {
            return false;
        }
        String[] actionList = this.mGLSurfaceView.getActionStringList();
        if (actionList == null) {
            return false;
        }
        String action = "";
        for (String ac : actionList) {
            if (!TextUtils.isEmpty(ac)) {
                action = action + ac + "\n";
            }
        }
        if (TextUtils.isEmpty(action)) {
            return false;
        }
        if (show) {
            Message msg = this.mGuideTextHandler.obtainMessage(3);
            msg.obj = action;
            this.mGuideTextHandler.sendMessage(msg);
        }
        return true;
    }

    public void onPreviewFrame(byte[] data) {
        if (this.mStickerManagerState.isStarted() && this.mGLSurfaceView != null && this.mGLSurfaceView.isShowing()) {
            this.mGLSurfaceView.feedFrame(data);
        }
    }

    public void onPreviewFrame(Image image) {
        if (this.mStickerManagerState.isStarted() && this.mGLSurfaceView != null && this.mGLSurfaceView.isShowing()) {
            this.mGLSurfaceView.feedFrame(image);
        }
    }

    public int currentCameraId() {
        int cameraId = this.mGet.getCameraId();
        CamLog.m3d(TAG, "cameraId : " + cameraId);
        return cameraId;
    }

    public int[] getPreviewSize() {
        getSupportedPreviewSize();
        return this.mCurrentPreviewSize;
    }

    public int[] getVideoSize() {
        CamLog.m3d(TAG, "getVideoSize : " + Arrays.toString(Utils.sizeStringToArray(this.mGet.getCurrentSelectedVideoSize())));
        return Utils.sizeStringToArray(this.mGet.getCurrentSelectedVideoSize());
    }

    public int getOrientation() {
        if (this.mGet.getCameraDevice() != null) {
            CameraInfomation ci = this.mGet.getCameraDevice().getCameraInfo();
            if (ci != null) {
                CamLog.m3d(TAG, "getOrientation : " + ci.getCameraOrientation());
                return ci.getCameraOrientation();
            }
            CamLog.m3d(TAG, "getOrientation : default 90");
            return 90;
        } else if (this.mGet.getCameraId() == 1) {
            CamLog.m3d(TAG, "getCameraDevice is null : 270");
            return 270;
        } else {
            CamLog.m3d(TAG, "getCameraDevice is null : 90");
            return 90;
        }
    }

    public boolean initWithVideoSize() {
        return this.isAttachVideo;
    }

    public void onContentTaken(ContentsInformation ci) {
        if (this.mStickerContentCallback != null) {
            if (ci.isPicture()) {
                if (!pictureSizeMap.containsKey(Integer.valueOf(ci.getPictureData().hashCode()))) {
                    pictureSizeMap.put(Integer.valueOf(ci.getPictureData().hashCode()), ci.getPictureSize());
                }
                if (!orientationMap.containsKey(Integer.valueOf(ci.getPictureData().hashCode()))) {
                    orientationMap.put(Integer.valueOf(ci.getPictureData().hashCode()), Integer.valueOf(ci.getJpepOrientation()));
                }
            }
            this.mStickerContentCallback.onContentTaken(ci);
        }
    }

    public void infoListener(int action, Object value) {
        boolean z = true;
        switch (action) {
            case 100:
                int faceCount = 0;
                if (value != null) {
                    faceCount = ((Integer) value).intValue();
                }
                if (!this.mDetectedFace && this.mPreviousFaceCount < 1 && faceCount > 0) {
                    this.mDetectedFace = true;
                    this.mGuideTextHandler.removeMessages(1);
                    this.mGuideTextHandler.removeMessages(3);
                    boolean checkActionList = processActionGuide(this.mGLSurfaceView.isStickerLoadCompleted());
                    CamLog.m3d(TAG, "Action list : " + checkActionList);
                    if (!checkActionList) {
                        this.mGuideTextHandler.sendEmptyMessage(2);
                    }
                }
                this.mPreviousFaceCount = faceCount;
                CamLog.m3d(TAG, "faceCount : " + faceCount);
                CamLog.m3d(TAG, "INFO_FACE_COUNT_CHANGED ");
                return;
            case 101:
                CamLog.m3d(TAG, "INFO_FACE_ACTION_DONE");
                this.mGuideTextHandler.removeMessages(1);
                this.mGuideTextHandler.removeMessages(3);
                this.mGuideTextHandler.sendEmptyMessage(5);
                return;
            case 102:
                if (value != null) {
                    int percent = ((Integer) value).intValue();
                    if (percent == 0) {
                        this.mDetectedFace = false;
                        this.mGuideTextHandler.removeMessages(1);
                        this.mGuideTextHandler.removeMessages(3);
                        this.mGuideTextHandler.sendEmptyMessage(5);
                        CamLog.m3d(TAG, "INFO_STICKER_CHANGE start");
                        return;
                    } else if (100 == percent) {
                        CamLog.m3d(TAG, "INFO_STICKER_CHANGE done");
                        if (!this.mDetectedFace && this.mPreviousFaceCount < 1) {
                            this.mGuideTextHandler.sendEmptyMessageDelayed(1, CameraConstants.TOAST_LENGTH_SHORT);
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                }
                return;
            case 103:
                if (value != null) {
                    int setFaceCount = ((Integer) value).intValue();
                    this.mPreviousFaceCount = setFaceCount;
                    CamLog.m3d(TAG, "INFO_SET_FACE_COUNT : " + setFaceCount);
                    return;
                }
                return;
            case 104:
                if (this.mPreviousFaceCount <= 0) {
                    z = false;
                }
                processActionGuide(z);
                return;
            default:
                CamLog.m3d(TAG, "action : " + action);
                return;
        }
    }

    public void setStickerDrawing(boolean draw) {
        this.smi.setStickerDrawing(draw);
    }

    public boolean isGLSurfaceViewShowing() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.isShowing();
        }
        return false;
    }

    public boolean isStickerDrawing() {
        if (this.mGLSurfaceView != null && this.mStickerManagerState.isStarted() && this.mGLSurfaceView.isShowing() && this.mGLSurfaceView.isStickerDrawing()) {
            return true;
        }
        return false;
    }

    public void onCameraSwitchingEnd() {
        super.onCameraSwitchingEnd();
        CamLog.m3d(TAG, "");
        this.isCameraSwitchingStarted = false;
        if (this.mGLSurfaceView != null) {
            setPresetForGLView();
            this.mGLSurfaceView.onCameraSwitchingEnd();
        }
    }

    public void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        CamLog.m3d(TAG, "");
        this.isCameraSwitchingStarted = true;
        this.mGuideTextHandler.removeMessages(7);
        this.mGuideTextHandler.removeMessages(1);
        this.mGuideTextHandler.removeMessages(3);
        this.mGuideTextHandler.sendEmptyMessage(5);
        hideItemStickerLayout();
        if (this.mGLSurfaceView != null && this.mStickerManagerState.isStarted()) {
            offEdit();
            this.mGLSurfaceView.onCameraSwitchingStart();
        }
    }

    public void onPauseBefore() {
        if (this.mGuideTextHandler != null) {
            this.mGuideTextHandler.removeMessages(7);
            this.mGuideTextHandler.removeMessages(3);
            this.mGuideTextHandler.removeMessages(1);
            this.mGuideTextHandler.sendEmptyMessage(5);
        }
        super.onPauseBefore();
        hideItemStickerLayout();
        if (isWaitOneShot()) {
            this.mStickerManagerState.setState(State.STATE_STOP);
        }
        CamLog.m5e(TAG, "onPauseBefore");
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        this.mGLSurfaceView.justHide();
        CamLog.m3d(TAG, "onPauseAfter");
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        CamLog.m3d(TAG, "onResumeAfter");
    }

    public void setLayoutParamsForGLView() {
        CamLog.m3d(TAG, "setLayoutParamsForGLView");
        setPresetForGLView();
    }

    public void setMarginLayoutParamsForGLView(int leftMargin) {
        CamLog.m3d(TAG, "setMarginLayoutParamsForGLView leftMargin : " + leftMargin);
        sLeftMargin = leftMargin;
        setPresetForGLView();
    }

    public void onDestroy() {
        CamLog.m3d(TAG, "onDestroy");
        this.mGuideTextHandler.removeMessages(7);
        if (this.mGLSurfaceView != null) {
            sendAway();
            CamLog.m3d(TAG, "mGet.isCameraChanging() = " + this.mGet.isCameraChanging());
            if (!this.isCameraSwitchingStarted) {
                this.mGLSurfaceView.clearLastSticker();
                clearGridSelection();
            }
        }
        if (this.mGet.getActivity().isFinishing()) {
            if (this.mGLSurfaceView != null) {
                this.mGLSurfaceView.clearLastSticker();
                this.mGLSurfaceView.hide();
                this.mGLSurfaceView.destroy();
            }
            this.mGLSurfaceView = null;
            RecentDBHelper.getInstance(getAppContext());
            RecentDBHelper.removeInstance();
            DecompressScheduler.getInstance(getAppContext()).setCallback(null);
            this.mStickerManagerState.setState(State.STATE_DESTROY);
            this.mDetectedFace = false;
            if (this.mTabAdapter != null) {
                this.mTabAdapter.clearAdapter();
            }
            this.mTabAdapter = null;
            if (this.mAdapter != null) {
                this.mAdapter.clearAdapter();
            }
            this.mAdapter = null;
            if (this.mRecentAdapter != null) {
                this.mRecentAdapter.clearAdapter();
            }
            this.mRecentAdapter = null;
        }
        super.onDestroy();
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mGuideLayout != null && this.mGuideLayout.getVisibility() == 0) {
            this.mGuideLayout.rotateLayout(degree);
        }
        if (this.mActionLayout != null && this.mActionLayout.getVisibility() == 0) {
            this.mActionLayout.rotateLayout(degree);
        }
    }

    private void setDegreeAdpaterJustSet(int degree) {
        CamLog.m3d(TAG, "setDegreeAdpaterJustSet degree = " + degree);
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.setPhoneOrientationDegree(degree);
        }
        if (this.mClearButton != null) {
            this.mClearButton.setDegree(degree, false);
        }
        if (this.mTabAdapter != null) {
            this.mTabAdapter.setDegreeJustSet(degree);
        }
        if (this.mAdapter != null) {
            this.mAdapter.setDegreeJustSet(degree);
        }
        if (this.mRecentAdapter != null) {
            this.mRecentAdapter.setDegreeJustSet(degree);
        }
    }

    public void setDegree(int degree, boolean animation) {
        CamLog.m3d(TAG, "setDegree degree = " + degree);
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.setPhoneOrientationDegree(degree);
        }
        if (this.mClearButton != null) {
            this.mClearButton.setDegree(degree, animation);
        }
        if (this.mTabAdapter != null) {
            this.mTabAdapter.setDegree(degree);
        }
        if (this.mAdapter != null) {
            this.mAdapter.setDegree(degree);
        }
        if (this.mRecentAdapter != null) {
            this.mRecentAdapter.setDegree(degree);
        }
        super.setDegree(degree, animation);
    }

    public void onTabItemClicked(String tabName, String tabPath) {
        CamLog.m3d(TAG, "");
        if (tabName.equals("recent")) {
            this.mGridView.setAdapter(this.mRecentAdapter);
            setGridSelection();
            this.mRecentAdapter.load();
            return;
        }
        this.mGridView.setAdapter(this.mAdapter);
        this.mAdapter.load(tabPath);
        setGridSelection();
    }

    public void onTabItemDeleted(int idx) {
        this.mClickedTabIndex = idx;
        this.mGet.showDialog(151);
    }

    public void onTabListLoadCompleted() {
        if (this.mTabAdapter != null && this.mAdapter != null && this.mRecentAdapter != null) {
            RecentDBHelper helper = RecentDBHelper.getInstance(getAppContext());
            if (helper != null) {
                helper.openDB();
            }
            if (helper == null || !helper.hasContents()) {
                CamLog.m3d(TAG, "no recent content!");
                this.mTabAdapter.setSelection(1);
                String tabPath = this.mTabAdapter.getSelectedStickerPackPath();
                if (tabPath != null) {
                    this.mAdapter.load(tabPath);
                    this.mGridView.setAdapter(this.mAdapter);
                    setGridSelection();
                    return;
                }
                return;
            }
            CamLog.m3d(TAG, "has content!");
            this.mTabAdapter.setSelection(0);
            this.mGridView.setAdapter(this.mRecentAdapter);
            setGridSelection();
            this.mRecentAdapter.load();
        }
    }

    public void onCheckEditStatus(boolean dim) {
        if (this.mGet != null) {
            this.mGet.setEditDim(dim);
        }
    }

    public void takePicture(StickerContentCallback callback, boolean needFlip, Bitmap signature) {
        CamLog.m3d(TAG, "TakePicture!!!!!!");
        if (this.mGLSurfaceView != null) {
            this.mStickerContentCallback = callback;
            this.mGLSurfaceView.takePicture(needFlip, signature);
        }
    }

    private void offEdit() {
        CamLog.m3d(TAG, "");
        if (this.mTabAdapter != null) {
            this.mTabAdapter.deleteOff();
        }
    }

    public boolean onEditClickOnOff() {
        CamLog.m3d(TAG, "");
        if (this.mTabAdapter == null) {
            return false;
        }
        this.mTabAdapter.deleteOnOff();
        return this.mTabAdapter.getDeleteOnOff();
    }

    public void onClick(View view) {
        CamLog.m3d(TAG, "");
        if (view.getId() == C0088R.id.sticker_clear_button) {
            this.mGuideTextHandler.removeMessages(1);
            this.mGuideTextHandler.removeMessages(3);
            this.mGuideTextHandler.sendEmptyMessage(5);
            if (this.mGLSurfaceView != null) {
                this.mGLSurfaceView.clearSticker();
                clearGridSelection();
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mStickerGridVisiblity) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                this.mIsTouchDown = true;
                this.mIsTouchMoving = false;
                this.f37mX = (int) event.getX();
                this.f38mY = (int) event.getY();
                return false;
            case 1:
                this.mIsTouchDown = false;
                int cY = (int) event.getY();
                if ((Math.abs(((int) event.getX()) - this.f37mX) >= this.mTouchSlop || Math.abs(cY - this.f38mY) >= this.mTouchSlop) && !this.mIsTouchMoving) {
                    return false;
                }
                this.mIsTouchMoving = false;
                return true;
            case 2:
                if (!this.mIsTouchDown) {
                    return false;
                }
                this.mIsTouchMoving = true;
                return true;
            default:
                return false;
        }
    }

    public boolean deleteOK() {
        boolean z = true;
        if (this.mClickedTabIndex != -1) {
            String path = this.mTabAdapter.deleteTab(this.mClickedTabIndex);
            String currentPath = this.mGLSurfaceView.getCurrentStickerConfigPath();
            if (currentPath != null && currentPath.contains(path)) {
                this.mGLSurfaceView.clearSticker();
                this.mGuideTextHandler.removeMessages(3);
                this.mGuideTextHandler.removeMessages(1);
                this.mGuideTextHandler.sendEmptyMessage(5);
                clearGridSelection();
            }
        }
        this.mClickedTabIndex = -1;
        boolean retval = this.mTabAdapter.isRemainDeletableItem();
        ModuleInterface moduleInterface = this.mGet;
        if (retval) {
            z = false;
        }
        moduleInterface.setEditDim(z);
        return retval;
    }

    public void deleteCancel() {
        this.mClickedTabIndex = -1;
    }

    private void setPresetForGLView() {
        if (this.mGLSurfaceView != null) {
            String key;
            if (!(this.mGet instanceof AttachCameraModule)) {
                key = SizePrefMaker.PICTURE_RATIO_STRINGS[SizePrefMaker.calculateRatio(this.mGet.getCurrentSelectedPictureSize())];
            } else if (this.mGet.isVideoCaptureMode()) {
                key = SizePrefMaker.PICTURE_RATIO_STRINGS[SizePrefMaker.calculateRatio(this.mGet.getCurrentSelectedVideoSize())];
            } else {
                key = SizePrefMaker.PICTURE_RATIO_STRINGS[SizePrefMaker.calculateRatio(getSupportedPreviewSize())];
            }
            if (this.mParamPreset.containsKey(key)) {
                LayoutParams lp = (LayoutParams) this.mParamPreset.get(key);
                int totalMargin = sLeftMargin == 0 ? lp.topMargin : sLeftMargin;
                CamLog.m3d(TAG, "from preset key ratio : " + key + " w : " + lp.width + " h : " + lp.height + " t : " + totalMargin + " return : " + this.mGLSurfaceView.setLayoutAndPosition(lp.width, lp.height, totalMargin));
                return;
            }
            CamLog.m3d(TAG, "key not valid");
            return;
        }
        CamLog.m3d(TAG, "GLSurfaceView is null");
    }

    public void startRecording(String dir, String filename, StickerContentCallback callback) {
        this.mStickerContentCallback = callback;
        if (this.mGLSurfaceView != null) {
            long maxDuration = (this.mGet.isMMSRecording() && ModelProperties.getCarrierCode() == 6) ? 60000000 : -1;
            this.mGLSurfaceView.startRecording(dir, filename, this.mMaxVideoFileSize, maxDuration);
        }
    }

    public ContentsInformation stopRecording() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.stopRecording();
        }
        return null;
    }

    public void pauseRecording() {
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.pauseRecording();
        }
    }

    public void resumeRecording() {
        if (this.mGLSurfaceView != null) {
            this.mGLSurfaceView.resumeRecording();
        }
    }

    public void resumeEngine() {
        CamLog.m3d(TAG, "");
        if (this.mGLSurfaceView == null) {
            return;
        }
        if (hasSticker()) {
            setPresetForGLView();
            bringItAndShow();
            this.mGLSurfaceView.reResumeEngine();
            this.mStickerManagerState.setState(State.STATE_START);
            this.mGuideTextHandler.removeMessages(7);
            this.mGuideTextHandler.sendEmptyMessageDelayed(7, 300);
            return;
        }
        clearGridSelection();
    }

    public int[] getPictureSize(int byteHashCode) {
        if (!pictureSizeMap.containsKey(Integer.valueOf(byteHashCode))) {
            return null;
        }
        int[] ret = (int[]) pictureSizeMap.get(Integer.valueOf(byteHashCode));
        pictureSizeMap.remove(Integer.valueOf(byteHashCode));
        return ret;
    }

    public int getJpegOrientationWithoutRemove(int byteHashCode) {
        if (orientationMap.containsKey(Integer.valueOf(byteHashCode))) {
            return ((Integer) orientationMap.get(Integer.valueOf(byteHashCode))).intValue();
        }
        return -1;
    }

    public int getJpegOrientation(int byteHashCode) {
        if (!orientationMap.containsKey(Integer.valueOf(byteHashCode))) {
            return -1;
        }
        int ret = ((Integer) orientationMap.get(Integer.valueOf(byteHashCode))).intValue();
        orientationMap.remove(Integer.valueOf(byteHashCode));
        return ret;
    }

    public boolean applyStickerPreview() {
        boolean retValue = false;
        if (this.mGLSurfaceView != null && this.mGLSurfaceView.hasSticker() && this.mStickerManagerState.isStarted()) {
            retValue = true;
        }
        CamLog.m3d(TAG, "retValue : " + retValue);
        return retValue;
    }

    private void setGridSelection() {
        if (this.mGridView != null && this.mGLSurfaceView != null) {
            StickerAdapter adapter = (StickerAdapter) this.mGridView.getAdapter();
            if (adapter != null) {
                adapter.setSelection(this.mGLSurfaceView.getCurrentStickerConfigPath());
            }
        }
    }

    private void clearGridSelection() {
        CamLog.m5e(TAG, "clearGridSelection mRecentAdapter = " + this.mRecentAdapter);
        CamLog.m5e(TAG, "clearGridSelection mAdapter = " + this.mAdapter);
        if (this.mRecentAdapter != null && this.mAdapter != null) {
            this.mRecentAdapter.clearSelection();
            this.mAdapter.clearSelection();
        }
    }

    public int[] getViewportSize() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.getViewportSize();
        }
        return null;
    }

    public boolean shoudRestoreByManager() {
        return this.mGet instanceof AttachCameraModule;
    }

    public boolean hasSticker() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.hasSticker();
        }
        return false;
    }

    public String getLDBInfo() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.getLDBInfo();
        }
        return null;
    }

    public void onPostviewDisplayed() {
        CamLog.m3d(TAG, "onPostviewDisplayed = ");
        this.mGuideTextHandler.removeMessages(6);
        if (!isRecording()) {
            this.mGuideTextHandler.sendEmptyMessage(6);
        }
    }

    public void onPostviewReleased() {
        CamLog.m3d(TAG, "onPostviewReleased = ");
        this.mGuideTextHandler.removeMessages(6);
    }

    public boolean isStickerLoadCompleted() {
        if (this.mGLSurfaceView != null) {
            return this.mGLSurfaceView.isStickerLoadCompleted();
        }
        return false;
    }

    public void waitOneShot() {
        this.mStickerManagerState.setState(State.STATE_WAIT_ONE_SHOT);
    }

    public boolean isWaitOneShot() {
        return this.mStickerManagerState.getState() == State.STATE_WAIT_ONE_SHOT;
    }

    public void bringItAndShow() {
        if (this.mGLSurfaceView != null) {
            CamLog.m3d(TAG, "bringItAndShow");
            this.mGLSurfaceView.bringItAndShow();
        }
    }

    public void sendAway() {
        if (this.mGLSurfaceView != null) {
            int distance = Utils.getDefaultDisplayHeight(getActivity()) + 300;
            CamLog.m3d(TAG, "sendAway " + distance);
            this.mGLSurfaceView.sendAway(distance);
        }
    }

    public boolean prepareStickerDrawing() {
        if (isStickerDrawing() && !isStickerLoadCompleted()) {
            CamLog.m7i(TAG, "Sticker drawing but not completely loaded");
            return true;
        } else if (!hasSticker() || isStickerDrawing()) {
            return false;
        } else {
            CamLog.m7i(TAG, "perparing sticker drawing");
            return true;
        }
    }

    public static String CalculatePreviewSize(String pictureSize) {
        String[] previewSize;
        if (ModelProperties.getLCDType() == 2) {
            previewSize = PREVIEW_SIZE_STRINGS_NOTCH;
        } else if (ModelProperties.getAppTier() >= 5) {
            previewSize = PREVIEW_SIZE_STRINGS_PREMEUM;
        } else {
            previewSize = PREVIEW_SIZE_STRINGS_MID;
        }
        String viewSize = "";
        switch (SizePrefMaker.calculateRatio(pictureSize)) {
            case 1:
                viewSize = previewSize[3];
                break;
            case 2:
                viewSize = previewSize[0];
                break;
            case 5:
                viewSize = previewSize[1];
                break;
            case 6:
                viewSize = previewSize[2];
                break;
            default:
                viewSize = previewSize[2];
                CamLog.m5e(TAG, "Check support sticker preview size");
                break;
        }
        CamLog.m3d(TAG, "ratioSize : " + viewSize);
        return viewSize;
    }

    public boolean isEditDimStatus() {
        if (this.mTabAdapter == null) {
            return false;
        }
        CamLog.m3d(TAG, "isEditDimStatus : " + this.mTabAdapter.isRemainDeletableItem());
        if (this.mTabAdapter.isRemainDeletableItem()) {
            return false;
        }
        return true;
    }

    public boolean hideMenuOnTakePictureBefore() {
        return this.mGet != null && (this.mGet instanceof AttachCameraModule) && isStickerGridViewShowing();
    }
}
