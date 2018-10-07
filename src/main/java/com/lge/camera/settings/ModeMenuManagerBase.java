package com.lge.camera.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.google.lens.sdk.LensApi;
import com.lge.camera.C0088R;
import com.lge.camera.components.EditableGridView;
import com.lge.camera.components.EditableGridView.EditableGridViewListener;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class ModeMenuManagerBase implements OnRemoveHandler, EditableGridViewListener {
    public static final String MODE_DOWNLOAD_PANORAMA360 = "shotmode_360panorama";
    public static final String MODE_DOWNLOAD_PATH = "sdcard/LGWorld/Camera/Mode/";
    public static final String MODE_DOWNLOAD_SNAP = "shotmode_snapmovie";
    public static final String MODE_DOWNLOAD_SNAP_FRONT = "shotmode_snapmovie";
    public static final String MODE_DOWNLOAD_SNAP_MULTI = "shotmode_snapmovie";
    public static final String MODE_DOWNLOAD_SNAP_SINGLE = "shotmode_snapmovie";
    public static final String MODE_DOWNLOAD_SQUARE_GRID = "shotmode_gridshot";
    public static final String MODE_DOWNLOAD_SQUARE_GUIDE = "shotmode_guideshot";
    public static final String MODE_DOWNLOAD_SQUARE_MATCH = "shotmode_matchshot";
    public static final String MODE_DOWNLOAD_SQUARE_SNAP = "shotmode_snapshot";
    public static final String MODE_DOWNLOAD_TIMELAPSE = "shotmode_timelapse";
    protected static HashSet<String> sDownloadableMode = null;
    protected static HashMap<String, String> sDownloadableModeFiles = null;
    protected final int GRIDVIEW_NUM_COLUMN = 3;
    private final String MODE_PREFERENCE_REGEX = ",";
    protected int mDegree = -1;
    protected OnClickListener mDownBtnListener = null;
    protected RotateImageButton mDownloadButton = null;
    protected OnClickListener mEditBtnListener = null;
    protected RotateImageButton mEditButton = null;
    protected View mEditButtonLayout = null;
    protected ArrayList<ModeItem> mFrontModeItemList = new ArrayList();
    protected ModeMenuInterface mGet = null;
    public int mGoogleArStickersStatus = 1;
    protected RotateLayout mGridLayout = null;
    protected EditableGridView mGridView = null;
    private Thread mImageCacheThread = null;
    protected boolean mIsDownloadBtnClicked = false;
    protected boolean mIsDragging = false;
    protected LensApi mLensApi;
    protected ModeMenuListAdapter mListAdapter = null;
    protected int mLongClickedIndex = -1;
    protected ModeItem mLongClickedItem = null;
    protected OnItemClickListener mModeMenuClickListener = new C13912();
    protected View mModeMenuView = null;
    protected ArrayList<ModeItem> mRearModeItemList = new ArrayList();

    /* renamed from: com.lge.camera.settings.ModeMenuManagerBase$2 */
    class C13912 implements OnItemClickListener {
        C13912() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            CamLog.m3d(CameraConstants.TAG, "modemenu : onItemClick position=" + position + " id=" + id);
            if (ModeMenuManagerBase.this.mGet != null && !ModeMenuManagerBase.this.mGet.isModuleChanging() && !ModeMenuManagerBase.this.mGet.isPaused() && ModeMenuManagerBase.this.mGet.checkModuleValidate(207)) {
                if (ModeMenuManagerBase.this.getModeItemList() == null) {
                    CamLog.m3d(CameraConstants.TAG, "[color] menu click - return.");
                    return;
                }
                ModeItem item = (ModeItem) ModeMenuManagerBase.this.getModeItemList().get(position);
                if (item == null) {
                    return;
                }
                if (ModeMenuManagerBase.this.mEditButton == null || !ModeMenuManagerBase.this.mEditButton.isSelected()) {
                    ModeMenuManagerBase.this.onModeItemClick(item.getValue());
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "[mode] it's editing mode - return");
                if (item.isDeletable()) {
                    ModeMenuManagerBase.this.mGet.showModeDeleteDialog(item);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.settings.ModeMenuManagerBase$4 */
    class C13944 extends Thread {
        C13944() {
        }

        public void run() {
            Iterator it;
            ModeItem item;
            if (ModeMenuManagerBase.this.mRearModeItemList != null) {
                it = ModeMenuManagerBase.this.mRearModeItemList.iterator();
                while (it.hasNext()) {
                    item = (ModeItem) it.next();
                    if (!ModeMenuManagerBase.this.mImageCacheThread.isInterrupted()) {
                        if (item != null) {
                            item.setImageDrawable((LevelListDrawable) ModeMenuManagerBase.this.mGet.getAppContext().getResources().getDrawable(item.getImageResourceId()));
                        }
                    } else {
                        return;
                    }
                }
            }
            if (ModeMenuManagerBase.this.mFrontModeItemList != null) {
                it = ModeMenuManagerBase.this.mFrontModeItemList.iterator();
                while (it.hasNext()) {
                    item = (ModeItem) it.next();
                    if (!ModeMenuManagerBase.this.mImageCacheThread.isInterrupted()) {
                        if (item != null) {
                            item.setImageDrawable((LevelListDrawable) ModeMenuManagerBase.this.mGet.getAppContext().getResources().getDrawable(item.getImageResourceId()));
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public ModeMenuManagerBase(ModeMenuInterface modeMenuInterface) {
        this.mGet = modeMenuInterface;
    }

    protected ModeMenuListAdapter createListAdapter() {
        return new ModeMenuListAdapter(this.mGet.getAppContext(), getModeItemList());
    }

    protected void show() {
        makeAllImageResources();
        waitImageCacheThread(false);
        this.mListAdapter = createListAdapter();
        this.mListAdapter.setListItemDegree(this.mDegree);
        if (getModeItemList().size() <= 0) {
            CamLog.m3d(CameraConstants.TAG, "[mode] show - item list is empty, return");
            return;
        }
        this.mDegree = this.mGet.getOrientationDegree();
        initLayout();
        setGridView();
        this.mGridView.setVisibility(0);
        if (this.mEditButtonLayout != null) {
            this.mEditButtonLayout.setVisibility(0);
        }
        CamLog.m3d(CameraConstants.TAG, "modemenu : show");
    }

    protected void initLayout() {
        if (this.mModeMenuView == null) {
            CamLog.m3d(CameraConstants.TAG, "mode : initLayout");
            this.mModeMenuView = this.mGet.getActivity().getLayoutInflater().inflate(C0088R.layout.mode_menu, null);
            this.mGet.inflateStub(C0088R.id.stub_mode_view);
            boolean isTileOn = "on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW));
            RelativeLayout layout = (RelativeLayout) this.mModeMenuView.findViewById(C0088R.id.mode_layout);
            LayoutParams params = (LayoutParams) layout.getLayoutParams();
            int height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.89f);
            int topMargin = 0;
            int thumbnailHeight = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
            if (isTileOn) {
                height -= thumbnailHeight;
                topMargin = thumbnailHeight;
            }
            params.height = height;
            params.topMargin = topMargin;
            layout.setLayoutParams(params);
            this.mEditButtonLayout = this.mModeMenuView.findViewById(C0088R.id.mode_button_layout);
            if (this.mEditButtonLayout != null) {
                LayoutParams editParams = (LayoutParams) this.mEditButtonLayout.getLayoutParams();
                editParams.topMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
                this.mEditButtonLayout.setLayoutParams(editParams);
                this.mEditButton = (RotateImageButton) this.mEditButtonLayout.findViewById(C0088R.id.mode_edit_button);
                this.mEditButton.setOnClickListener(this.mEditBtnListener);
                this.mEditButton.setVisibility(0);
                this.mEditButton.setDegree(this.mDegree, false);
                if (FunctionProperties.isSupportedModedownload()) {
                    this.mDownloadButton = (RotateImageButton) this.mEditButtonLayout.findViewById(C0088R.id.mode_download_button);
                    this.mDownloadButton.setOnClickListener(this.mDownBtnListener);
                    this.mDownloadButton.setDegree(this.mDegree, false);
                    this.mDownloadButton.setVisibility(0);
                }
            }
            this.mGridLayout = (RotateLayout) this.mModeMenuView.findViewById(C0088R.id.mode_menu_grid_layout);
            if (this.mGridLayout != null) {
                this.mGridView = (EditableGridView) this.mGridLayout.findViewById(C0088R.id.mode_menu_grid_view);
                this.mGridView.setListener(this);
            }
            ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.mode_view);
            if (vg != null) {
                vg.addView(this.mModeMenuView);
                this.mModeMenuView.setVisibility(0);
            }
        }
    }

    protected void resetLayout() {
        if (this.mModeMenuView != null) {
            CamLog.m3d(CameraConstants.TAG, "mode : resetLayout");
            if (this.mEditButton != null) {
                this.mEditButton.setOnClickListener(null);
                this.mEditButton = null;
            }
            if (this.mDownloadButton != null) {
                this.mDownloadButton.setOnClickListener(null);
                this.mDownloadButton.setVisibility(8);
                this.mDownloadButton = null;
            }
            if (this.mEditButtonLayout != null) {
                this.mEditButtonLayout.setVisibility(8);
                this.mEditButtonLayout = null;
            }
            if (this.mGridView != null) {
                this.mGridView.setListener(null);
                this.mGridView.setVisibility(8);
                this.mGridView.onDestroy();
                this.mGridView = null;
            }
            this.mGridLayout = null;
            this.mModeMenuView.setVisibility(8);
            this.mGet.setQuickButtonSelected(C0088R.id.quick_button_mode, false);
            ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.mode_view);
            if (!(vg == null || this.mModeMenuView == null)) {
                this.mModeMenuView.setVisibility(8);
                vg.removeView(this.mModeMenuView);
            }
            this.mModeMenuView = null;
        }
    }

    protected void setGridView() {
        if (this.mGridLayout != null) {
            int start;
            int top;
            int horizontal;
            int vertical;
            int i;
            if (this.mGridView == null) {
                this.mGridView = (EditableGridView) this.mGridLayout.findViewById(C0088R.id.mode_menu_grid_view);
            }
            int columnWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.1654f);
            int horizontalSpacing = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0872f);
            int verticalSpacing = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.026f);
            int paddingStart = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.023f);
            CamLog.m3d(CameraConstants.TAG, "[mode] w : " + columnWidth);
            boolean isLand = this.mDegree == 90 || this.mDegree == 270;
            int size = getModeItemList().size();
            int row = size / 3;
            if (size % 3 != 0) {
                row++;
            }
            this.mGridView.setDegree(this.mDegree);
            this.mGridLayout.setAngle(this.mDegree);
            int columns = 3;
            if (isLand) {
                if (size < 3) {
                    columns = size;
                    row = 1;
                } else if (row < 3) {
                    columns = 3;
                } else {
                    columns = row;
                    row = 3;
                }
            } else if (size < 3) {
                columns = size;
                row = 1;
            }
            if (isLand) {
                start = verticalSpacing;
            } else {
                start = paddingStart;
            }
            if (isLand) {
                top = paddingStart;
            } else {
                top = verticalSpacing;
            }
            if (isLand) {
                horizontal = verticalSpacing;
            } else {
                horizontal = horizontalSpacing;
            }
            if (isLand) {
                vertical = horizontalSpacing;
            } else {
                vertical = verticalSpacing;
            }
            int width = ((columnWidth * columns) + ((columns - 1) * horizontal)) + (start * 2);
            int height = ((columnWidth * row) + ((row - 1) * vertical)) + (top * 2);
            this.mGridView.setNumColumns(columns);
            this.mGridView.setColumnWidth(columnWidth);
            this.mGridView.setPaddingRelative(start, top, start, top);
            this.mGridView.setHorizontalSpacing(horizontal);
            this.mGridView.setVerticalSpacing(vertical);
            this.mGridView.setAdapter(this.mListAdapter);
            this.mGridView.setFocusable(false);
            this.mGridView.setSelected(false);
            this.mGridView.setOnItemClickListener(this.mModeMenuClickListener);
            LayoutParams gridParams = (LayoutParams) this.mGridLayout.getLayoutParams();
            if (isLand) {
                i = height;
            } else {
                i = width;
            }
            gridParams.width = i;
            if (!isLand) {
                width = height;
            }
            gridParams.height = width;
            this.mGridLayout.setLayoutParams(gridParams);
        }
    }

    protected void hide(boolean notifyModule) {
        if (this.mModeMenuView != null && this.mGridLayout != null) {
            onEditButtonClicked(false);
            resetLayout();
            waitImageCacheThread(true);
            releaseAllImageResources();
            System.gc();
            CamLog.m3d(CameraConstants.TAG, "modemenu : hide - end");
            this.mGet.onHideModeMenuEnd(notifyModule);
        }
    }

    public void onEditButtonClicked(boolean selected) {
        CamLog.m3d(CameraConstants.TAG, "[mode] edit button selected : " + selected);
        if (this.mEditButton != null) {
            this.mEditButton.setSelected(selected);
        }
        if (this.mListAdapter != null) {
            this.mListAdapter.setEditMode(selected);
        }
        if (this.mGridView != null) {
            this.mGridView.setEditMode(selected);
        }
        if (selected) {
            this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.mode_edit_guide), CameraConstants.TOAST_LENGTH_SHORT);
        }
    }

    public void hide(boolean showAnimation, boolean notifyModule) {
        if (this.mModeMenuView != null) {
            View backCover = this.mModeMenuView.findViewById(C0088R.id.backcover);
            if (backCover != null) {
                backCover.setOnTouchListener(null);
            }
        }
        if (!this.mGet.isPaused()) {
            CamLog.m3d(CameraConstants.TAG, "modemenu : hide, animation " + showAnimation);
            if (showAnimation) {
                shotModeMenuAnimation(this.mModeMenuView, false, notifyModule);
            } else {
                hide(notifyModule);
            }
        }
    }

    public void show(boolean useAnim) {
        CamLog.m3d(CameraConstants.TAG, "modemneu : show - start, animation : " + useAnim);
        show();
        if (this.mGet != null && this.mGet.isEnteringDirectFromShortcut()) {
            updateListViewItem(this.mGet.getShotMode());
        }
        if (useAnim) {
            shotModeMenuAnimation(this.mModeMenuView, true, true);
        }
    }

    public boolean isVisible() {
        return this.mModeMenuView == null ? false : this.mModeMenuView.isShown();
    }

    private void shotModeMenuAnimation(final View aniView, final boolean show, final boolean notifyModule) {
        AnimationUtil.startShowingAnimation(aniView, show, show ? 300 : 150, new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                ModeMenuManagerBase.this.mGet.setQuickButtonSelected(C0088R.id.quick_button_mode, show);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (aniView == null) {
                    return;
                }
                if (show) {
                    aniView.setVisibility(0);
                } else {
                    ModeMenuManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(ModeMenuManagerBase.this) {
                        public void handleRun() {
                            ModeMenuManagerBase.this.hide(notifyModule);
                        }
                    }, 0);
                }
            }
        });
    }

    protected void updateListViewItem(int position) {
        if (this.mListAdapter != null && getModeItemList() != null && position >= 0 && position < getModeItemList().size()) {
            ModeItem newItem = (ModeItem) getModeItemList().get(position);
            if (newItem != null) {
                int curIndex = getCurIndex();
                if (curIndex != -1) {
                    ModeItem prevItem = (ModeItem) getModeItemList().get(curIndex);
                    if (prevItem != null) {
                        prevItem.setSelected(false);
                    }
                    newItem.setSelected(true);
                    this.mListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    protected int getCurIndex() {
        if (getModeItemList() == null) {
            return -1;
        }
        for (int i = 0; i < getModeItemList().size(); i++) {
            ModeItem item = (ModeItem) getModeItemList().get(i);
            if (item != null && item.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    protected void updateListViewItem(String mode) {
        if (this.mListAdapter != null) {
            updateListViewItem(this.mListAdapter.getItemIndex(mode));
        }
    }

    private void validateMobileLiveIntent(Context context) {
        Intent intent = new Intent("com.google.android.youtube.intent.action.CREATE_LIVE_STREAM").setPackage(CameraConstants.PACKAGE_YOUTUBE);
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(CameraConstants.PACKAGE_YOUTUBE, 128);
        } catch (NameNotFoundException e) {
            CamLog.m12w(CameraConstants.TAG, "launch YouTube failed, ", e);
        }
        if (info == null || info.enabled) {
            List resolveInfo = pm.queryIntentActivities(intent, 65536);
            if (resolveInfo == null || resolveInfo.isEmpty()) {
                CamLog.m7i(CameraConstants.TAG, "can't resolve mobile live intent");
                startMobileLive(context);
                return;
            }
            startMobileLive(context);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "show enable youtube dialog");
        this.mGet.showCameraDialog(17);
    }

    private Intent createMobileLiveIntent(Context context, String description) {
        Intent intent = new Intent("com.google.android.youtube.intent.action.CREATE_LIVE_STREAM").setPackage(CameraConstants.PACKAGE_YOUTUBE);
        intent.putExtra("android.intent.extra.REFERRER", new Builder().scheme("android-app").appendPath(context.getPackageName()).build());
        if (!TextUtils.isEmpty(description)) {
            intent.putExtra("android.intent.extra.SUBJECT", description);
        }
        return intent;
    }

    private void startMobileLive(Context context) {
        try {
            this.mGet.getActivity().startActivity(createMobileLiveIntent(context, "Streaming via ..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, C0088R.string.youtube_live_streaming_activity_not_found_error, 1).show();
            CamLog.m6e(CameraConstants.TAG, "YouTube Activity Not Found!", e);
        }
    }

    public void onModeItemClick(String newMode) {
        if (newMode != null && this.mGet != null && !this.mGet.isModuleChanging() && !this.mGet.isPaused() && this.mGet.checkModuleValidate(207)) {
            if (this.mEditButton != null && this.mEditButton.isSelected()) {
                CamLog.m3d(CameraConstants.TAG, "[mode] it's editing mode - return");
            } else if (CameraConstants.MODE_YOUTUBE_LIVE.equals(newMode)) {
                validateMobileLiveIntent(this.mGet.getAppContext());
            } else if (CameraConstants.MODE_AR_STICKERS.equals(newMode)) {
                launchARStickers();
            } else if (!CameraConstants.MODE_CINEMA.equals(newMode) || FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
                String curMode = this.mGet.getCurSettingValue(Setting.KEY_MODE);
                if (newMode != null && curMode != null) {
                    if (newMode.equals(curMode)) {
                        this.mGet.hideModeMenu(true, false);
                        return;
                    }
                    updateListViewItem(newMode);
                    changeMode(newMode);
                }
            } else {
                CamLog.m3d(CameraConstants.TAG, "maximum battery saver on! return");
                this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.cannot_use_cine_video_on_max_power_saving_toast), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }
    }

    protected void changeMode(final String shotMode) {
        if (shotMode != null && this.mGet != null) {
            this.mGet.setSwitchingAniViewParam(false);
            changeCameraIdAndViewMode(shotMode);
            this.mGet.setSetting(Setting.KEY_MODE, shotMode, false);
            this.mGet.removeUIBeforeModeChange();
            this.mGet.setPreviewCallbackAll(false);
            hide(true, false);
            this.mGet.setPreviewCoverVisibility(0, true, new AnimationListener() {
                public void onAnimationStart(Animation arg0) {
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationEnd(Animation arg0) {
                    ModeMenuManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(ModeMenuManagerBase.this) {
                        public void handleRun() {
                            ModeMenuManagerBase.this.mGet.modeMenuClicked(shotMode);
                        }
                    }, 0);
                }
            }, true, false);
        }
    }

    public void changeCameraIdAndViewMode(String curValue) {
        boolean isForceChanged = false;
        if (this.mGet.isRearCamera() && this.mGet.isOpticZoomSupported(null) != this.mGet.isOpticZoomSupported(curValue)) {
            isForceChanged = true;
        }
        if (CameraConstants.MODE_SLOW_MOTION.equals(curValue)) {
            this.mGet.setCurrentConeMode(1, true);
            this.mGet.setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext()), isForceChanged);
        } else if (curValue.contains(CameraConstants.MODE_PANORAMA)) {
            this.mGet.setCurrentConeMode(1, true);
            this.mGet.setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext()), isForceChanged);
        } else if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(curValue) || CameraConstants.MODE_CINEMA.equals(curValue)) {
            this.mGet.setCurrentConeMode(1, true);
            this.mGet.setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext()), true);
        } else if (CameraConstants.MODE_MULTIVIEW.equals(curValue)) {
            this.mGet.setCurrentConeMode(1, true);
            setCameraIdForMultiview(isForceChanged);
        } else if (CameraConstants.MODE_SQUARE_SPLICE.equals(curValue)) {
            setCameraIdForMultiview(isForceChanged);
        } else if (CameraConstants.MODE_POPOUT_CAMERA.equals(curValue) || CameraConstants.MODE_DUAL_POP_CAMERA.equals(curValue)) {
            this.mGet.setCurrentConeMode(1, true);
            this.mGet.setCameraIdBeforeChange(false, 0, true);
        } else if ("mode_food".equals(curValue)) {
            this.mGet.setCurrentConeMode(1, true);
            this.mGet.setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext()), true);
        } else if (FunctionProperties.isSupportedLogProfile() && CameraConstants.MODE_MANUAL_VIDEO.equals(curValue) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            this.mGet.setCameraIdBeforeChange(false, 0, true);
        } else if (FunctionProperties.isSupportedHDR10() && CameraConstants.MODE_MANUAL_VIDEO.equals(curValue) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_HDR10))) {
            this.mGet.setCameraIdBeforeChange(false, 0, true);
        } else {
            setRearCameraIdOnOtherModules(curValue, isForceChanged);
        }
    }

    private void setCameraIdForMultiview(boolean forceChange) {
        if (FunctionProperties.useWideRearAsDefault()) {
            this.mGet.setCameraIdBeforeChange(false, 2, forceChange);
        } else {
            this.mGet.setCameraIdBeforeChange(false, 0, forceChange);
        }
    }

    private void setRearCameraIdOnOtherModules(String mode, boolean forceChange) {
        if (forceChange) {
            this.mGet.setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext()), true);
        } else if (FunctionProperties.getCameraTypeRear() != 1) {
        } else {
            if (this.mGet.getCameraId() == 0 || this.mGet.getCameraId() == 2) {
                int rearCameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                if (rearCameraId != 0 || 2 != rearCameraId) {
                    this.mGet.setCameraIdBeforeChange(false, rearCameraId, true);
                }
            }
        }
    }

    protected void waitImageCacheThread(boolean cancel) {
        if (this.mImageCacheThread != null && this.mImageCacheThread.isAlive()) {
            if (cancel) {
                this.mImageCacheThread.interrupt();
            }
            try {
                this.mImageCacheThread.join();
            } catch (InterruptedException e) {
                CamLog.m4d(CameraConstants.TAG, "Image cache thread join. ", e);
            }
            this.mImageCacheThread = null;
        }
    }

    protected void makeAllImageResources() {
        this.mImageCacheThread = new C13944();
        this.mImageCacheThread.start();
    }

    protected void releaseAllImageResources() {
        Iterator it;
        ModeItem item;
        if (this.mRearModeItemList != null) {
            it = this.mRearModeItemList.iterator();
            while (it.hasNext()) {
                item = (ModeItem) it.next();
                if (!(item == null || item.getImageDrawable() == null)) {
                    item.setImageDrawable(null);
                }
            }
        }
        if (this.mFrontModeItemList != null) {
            it = this.mFrontModeItemList.iterator();
            while (it.hasNext()) {
                item = (ModeItem) it.next();
                if (!(item == null || item.getImageDrawable() == null)) {
                    item.setImageDrawable(null);
                }
            }
        }
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    protected ArrayList<ModeItem> getModeItemList() {
        return this.mGet.isRearCamera() ? this.mRearModeItemList : this.mFrontModeItemList;
    }

    protected void initDownloadableMode() {
        int i = 0;
        if (FunctionProperties.isSupportedModedownload()) {
            String mode;
            if (ConfigurationUtil.sMODE_DOWNLOAD_SUPPORTED == null) {
                setModeDownloadData();
            }
            if (sDownloadableMode == null) {
                sDownloadableMode = new HashSet();
                for (String mode2 : ConfigurationUtil.sMODE_DOWNLOAD_SUPPORTED) {
                    sDownloadableMode.add(mode2);
                }
            }
            if (sDownloadableModeFiles == null) {
                sDownloadableModeFiles = new HashMap();
                String[] strArr = ConfigurationUtil.sMODE_DOWNLOAD_SUPPORTED;
                int length = strArr.length;
                while (i < length) {
                    mode2 = strArr[i];
                    sDownloadableModeFiles.put(mode2, getModeDownloadFile(mode2));
                    i++;
                }
            }
        }
    }

    private void setModeDownloadData() {
        ConfigurationUtil.sMODE_DOWNLOAD_SUPPORTED = new String[]{CameraConstants.MODE_TIME_LAPSE_VIDEO, CameraConstants.MODE_SNAP, CameraConstants.MODE_PANORAMA_LG_360_PROJ, CameraConstants.MODE_SQUARE_SNAPSHOT, CameraConstants.MODE_SQUARE_GRID, CameraConstants.MODE_SQUARE_OVERLAP, CameraConstants.MODE_SQUARE_SPLICE};
    }

    private String getModeDownloadFile(String mode) {
        String fileName = "";
        Object obj = -1;
        switch (mode.hashCode()) {
            case -2143839392:
                if (mode.equals(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
                    obj = null;
                    break;
                }
                break;
            case -2021720858:
                if (mode.equals(CameraConstants.MODE_SNAP)) {
                    obj = 1;
                    break;
                }
                break;
            case -330061962:
                if (mode.equals(CameraConstants.MODE_SQUARE_SPLICE)) {
                    obj = 6;
                    break;
                }
                break;
            case 397904073:
                if (mode.equals(CameraConstants.MODE_SQUARE_SNAPSHOT)) {
                    obj = 3;
                    break;
                }
                break;
            case 857791173:
                if (mode.equals(CameraConstants.MODE_PANORAMA_LG_360_PROJ)) {
                    obj = 2;
                    break;
                }
                break;
            case 1192103715:
                if (mode.equals(CameraConstants.MODE_SQUARE_OVERLAP)) {
                    obj = 5;
                    break;
                }
                break;
            case 1455340237:
                if (mode.equals(CameraConstants.MODE_SQUARE_GRID)) {
                    obj = 4;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                return MODE_DOWNLOAD_TIMELAPSE;
            case 1:
                return "shotmode_snapmovie";
            case 2:
                return MODE_DOWNLOAD_PANORAMA360;
            case 3:
                return MODE_DOWNLOAD_SQUARE_SNAP;
            case 4:
                return MODE_DOWNLOAD_SQUARE_GRID;
            case 5:
                return MODE_DOWNLOAD_SQUARE_GUIDE;
            case 6:
                return MODE_DOWNLOAD_SQUARE_MATCH;
            default:
                return fileName;
        }
    }

    public void makeModeList() {
        if (this.mGet != null) {
            releaseAllImageResources();
            this.mRearModeItemList.clear();
            this.mFrontModeItemList.clear();
            ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MODE);
            CharSequence[] entryValues = listPref.getEntryValues();
            String curMode = this.mGet.getCurSettingValue(Setting.KEY_MODE);
            if (listPref != null && entryValues != null && curMode != null) {
                initDownloadableMode();
                String sharedList = SharedPreferenceUtilBase.getModeList(this.mGet.getAppContext(), this.mGet.isRearCamera());
                if (sharedList == null) {
                    initModeList(entryValues);
                } else {
                    sortModeList(sharedList, entryValues);
                }
            }
        }
    }

    private int getIndexOfListPref(String shotMode, CharSequence[] entryValues) {
        for (int i = 0; i < entryValues.length; i++) {
            String mode = entryValues[i];
            if (mode != null) {
                if (shotMode.contains(CameraConstants.MODE_SNAP)) {
                    shotMode = CameraConstants.MODE_SNAP;
                }
                if (mode.equals(shotMode)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private ModeItem getModeItemFromListPref(int index) {
        ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MODE);
        String curMode = this.mGet.getCurSettingValue(Setting.KEY_MODE);
        if (listPref == null || curMode == null) {
            return null;
        }
        CharSequence[] entryValues = listPref.getEntryValues();
        CharSequence[] entry = listPref.getEntries();
        int[] textId = listPref.getIntExtraInfo(4);
        int[] imageId = listPref.getIntExtraInfo(5);
        String shotMode = entryValues[index];
        if (shotMode == null) {
            return null;
        }
        if (textId == null || imageId == null) {
            return null;
        }
        String title = String.valueOf(entry[index]);
        String desc = this.mGet.getAppContext().getString(textId[index]);
        int imgId = imageId[index];
        boolean selected = false;
        boolean deletable = false;
        if (curMode.equals(shotMode)) {
            selected = true;
        }
        if (isDownloadableMode(shotMode)) {
            deletable = true;
        }
        return new ModeItem(Setting.KEY_MODE, shotMode, title, desc, imgId, selected, deletable);
    }

    private void initModeList(CharSequence[] entryValues) {
        ModeItem item;
        ArrayList<ModeItem> downloadList = null;
        if (FunctionProperties.isSupportedModedownload()) {
            downloadList = new ArrayList();
        }
        for (int i = 0; i < entryValues.length; i++) {
            String shotMode = entryValues[i];
            item = getModeItemFromListPref(i);
            if (!(item == null || checkHideModeIcon(shotMode))) {
                if (!isDownloadableMode(shotMode)) {
                    getModeItemList().add(item);
                    CamLog.m3d(CameraConstants.TAG, "[mode] make : " + item.getValue() + ", delete : " + item.isDeletable());
                } else if (!(downloadList == null || checkDownloadedMode(shotMode))) {
                    downloadList.add(item);
                }
            }
        }
        if (downloadList != null && !downloadList.isEmpty()) {
            Iterator it = downloadList.iterator();
            while (it.hasNext()) {
                item = (ModeItem) it.next();
                getModeItemList().add(item);
                CamLog.m3d(CameraConstants.TAG, "[mode] make : " + item.getValue() + ", delete : " + item.isDeletable());
            }
        }
    }

    private void sortModeList(String sharedList, CharSequence[] entryValues) {
        int i;
        String shotMode;
        ModeItem item;
        String[] shared = sharedList.split(",");
        for (String shotMode2 : shared) {
            int index = getIndexOfListPref(shotMode2, entryValues);
            if (!(checkHideModeIcon(shotMode2) || index == -1 || ((isDownloadableMode(shotMode2) && checkDownloadedMode(shotMode2)) || (CameraConstants.MODE_AR_STICKERS.equals(shotMode2) && !isARStickersSupport())))) {
                item = getModeItemFromListPref(index);
                if (item != null) {
                    getModeItemList().add(item);
                    CamLog.m3d(CameraConstants.TAG, "[mode] make : " + item.getValue() + ", delete : " + item.isDeletable());
                }
            }
        }
        if (!this.mGet.isLGUOEMCameraIntent()) {
            for (i = 0; i < entryValues.length; i++) {
                shotMode2 = (String) entryValues[i];
                item = getModeItemFromListPref(i);
                if (!(item == null || !item.isDeletable() || checkDownloadedMode(shotMode2) || sharedList.contains(shotMode2))) {
                    getModeItemList().add(item);
                    CamLog.m3d(CameraConstants.TAG, "[mode] make : " + item.getValue() + ", delete : " + item.isDeletable());
                }
                if (CameraConstants.MODE_AR_STICKERS.equals(shotMode2) && isARStickersSupport() && !sharedList.contains(shotMode2)) {
                    getModeItemList().add(item);
                }
            }
        }
    }

    protected String modeListToString(ArrayList<ModeItem> list) {
        String str = "";
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ModeItem item = (ModeItem) it.next();
            if (item == null) {
                CamLog.m3d(CameraConstants.TAG, "[mode] mode to str - item is null");
            } else {
                String shotmode = item.getValue();
                if (shotmode != null) {
                    str = str + shotmode + ",";
                }
            }
        }
        return str;
    }

    public boolean isDownloadableMode(String shotMode) {
        if (FunctionProperties.isSupportedModedownload() && sDownloadableMode != null && sDownloadableMode.contains(shotMode)) {
            return true;
        }
        return false;
    }

    private boolean checkHideModeIcon(String shotMode) {
        if (this.mGet != null && this.mGet.isLGUOEMCameraIntent() && shotMode != null && !"mode_normal".equals(shotMode) && !shotMode.contains(CameraConstants.MODE_PANORAMA)) {
            return true;
        }
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA) && CameraConstants.MODE_POPOUT_CAMERA.equals(shotMode)) {
            return true;
        }
        if (ModelProperties.isFakeMode() && FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA) && CameraConstants.MODE_DUAL_POP_CAMERA.equals(shotMode)) {
            return true;
        }
        if (!CameraConstants.MODE_AR_STICKERS.equals(shotMode) || isARStickersSupport()) {
            return false;
        }
        return true;
    }

    public boolean checkDownloadedMode(String mode) {
        String modeForCheck = (String) sDownloadableModeFiles.get(mode);
        List<File> fileList = getFileList(MODE_DOWNLOAD_PATH);
        if (fileList == null || modeForCheck == null) {
            CamLog.m3d(CameraConstants.TAG, "[mode] file is null");
            return true;
        }
        for (File f : fileList) {
            String name = f.getName();
            if (name != null && name.contains(modeForCheck)) {
                return false;
            }
        }
        return true;
    }

    protected List<File> getFileList(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            return Arrays.asList(dir.listFiles());
        }
        return null;
    }

    public void addARStickersMode() {
        if (getModeItemList() == null) {
            CamLog.m5e(CameraConstants.TAG, "getModeItemList is null");
            return;
        }
        ModeItem item = getModeItemFromListPref(getIndexOfListPref(CameraConstants.MODE_AR_STICKERS, this.mGet.getListPreference(Setting.KEY_MODE).getEntryValues()));
        boolean needToAdd = true;
        for (int i = 0; i < getModeItemList().size(); i++) {
            if (((ModeItem) getModeItemList().get(i)).getValue().equals(CameraConstants.MODE_AR_STICKERS)) {
                needToAdd = false;
            }
        }
        if (needToAdd) {
            CamLog.m3d(CameraConstants.TAG, "AR STticker");
            getModeItemList().add(item);
            if (this.mListAdapter != null) {
                this.mListAdapter.notifyDataSetChanged();
            }
        }
    }

    private boolean isARStickersSupport() {
        return this.mGoogleArStickersStatus == 0;
    }

    private void launchARStickers() {
        if (this.mLensApi != null) {
            CamLog.m3d(CameraConstants.TAG, "AR Stickers");
            try {
                this.mLensApi.launchLensActivity(this.mGet.getActivity(), 1);
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, e.toString());
            }
        }
    }
}
