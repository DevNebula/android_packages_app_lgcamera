package com.lge.camera.managers;

import android.graphics.SurfaceTexture;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.app.ext.FoodModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.FileManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import com.lge.filmemulator.FilmEmulatorEngine;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FilmEmulatorManagerBase extends ManagerInterfaceImpl {
    protected static final int FILM_ANIMATION_IDLE = 0;
    protected static final int FILM_ANIMATION_READY_TO_HIDE = 2;
    protected static final int FILM_ANIMATION_READY_TO_SHOW = 1;
    protected static final int FILM_ANIMATION_START_HIDE = 8;
    protected static final int FILM_ANIMATION_START_SHOW = 4;
    protected static final int FILM_ANIMATION_TYPE_FADE = 128;
    protected static final int FILM_ANIMATION_TYPE_SLIDING = 256;
    protected static final float FILTER_MENU_BG_ALPHA = 0.7f;
    protected static final String LUT_DOWNLOAD_PATH = "sdcard/LGWorld/Camera/Filter/";
    protected static final int LUT_LEVEL = 36;
    protected static final String LUT_LOCAL_PATH = "sdcard/film/";
    protected static final String LUT_LOCAL_PATH_SMARTCAM = "sdcard/smartcam/";
    protected static final String LUT_PATH_DEFAULT = "etc/camera/film/";
    protected static final String LUT_PATH_DEFAULT_VENDOR = "vendor/etc/camera/film/";
    protected static final String LUT_PATH_FOOD = "etc/camera/food/";
    protected static final String LUT_PATH_FOOD_VENDOR = "vendor/etc/camera/food/";
    protected static final String LUT_PATH_SELFIE = "etc/camera/selfie/";
    protected static final String LUT_PATH_SELFIE_VENDOR = "vendor/etc/camera/selfie/";
    protected static final String LUT_PATH_SMARTCAM = "etc/camera/smartcam/";
    protected static final String LUT_PATH_SMARTCAM_VENDOR = "vendor/etc/camera/smartcam/";
    protected static final int MAX_ADVANCED_SELFIE_COUNT = 9;
    protected static final int MAX_FILM_COUNT = 9;
    protected static final int MAX_SMARTCAM_FILM_COUNT = 81;
    protected static final int SCROLL_STATE_IDLE = 0;
    protected static final int SCROLL_STATE_SCROLLING = 1;
    protected static final int UNKNOWN_COUNT_FOR_DOWNLOADABLE_FILTER = -1;
    protected float mAlphaAnimationValue = 1.0f;
    protected float mAnimationDiff = 0.0f;
    protected final float mAnimationDuration = 150.0f;
    protected long mAnimationStartTime;
    protected float mBackgroundCoverAlpahValue = 0.7f;
    protected String mCurFilmValue = CameraConstants.FILM_NONE;
    protected int mCurLutNumber = 0;
    protected int mCurSceneIndex = 0;
    protected int mDeleteBtnSize = 0;
    private int mDeleteLutNumber = -1;
    protected int mDeletedIndex = -1;
    protected int mDownX = 0;
    protected int mDownY = 0;
    protected boolean mDrawPreviewBackground = false;
    protected int mDx = 0;
    protected int mDy = 0;
    protected int mFilmAnimationState = 0;
    protected FilmEmulatorEngine mFilmEngine;
    protected GestureDetector mFilmGestureDetector = null;
    protected FilmEmulatorHelper mFilmHelper = new FilmEmulatorHelper();
    protected HashMap<String, Integer> mFilmIndexHashMap;
    protected int mFilmMenuBgBottomMargin = 0;
    protected int mFilmMenuBgHeight = 0;
    protected int mFilmMenuBgStartMargin = 0;
    protected int mFilmMenuBottomMargin = 0;
    protected int mFilmMenuEndMargin = 0;
    protected int mFilmMenuHeight = 0;
    protected int mFilmMenuStartMargin = 0;
    protected int mFilmMenuTopMargin = 0;
    protected int mFilmMenuWidth = 0;
    protected ArrayList<String> mFilmNamePathArrayList;
    protected String[] mFilmPathList;
    protected int mFilmState = 0;
    protected float mFilmStrength = 1.0f;
    protected SurfaceTexture mFilmSurfaceTexture = null;
    protected SurfaceTexture mFilmSurfaceTexture2 = null;
    protected SurfaceView mFilmSurfaceView;
    protected int mFilmThumbItemHeight = 0;
    protected int mFilmThumbItemHorizontalGap = 0;
    protected int mFilmThumbItemVerticalGap = 0;
    protected int mFilmThumbItemWidth = 0;
    protected boolean mFilterItemFourWayRotation = false;
    protected ArrayList<FilterMenuItem> mFilterMenuItemList;
    protected SurfaceHolder mHolder = null;
    protected DecelerateInterpolator mInterpolator = new DecelerateInterpolator();
    protected boolean mIsAfterRecording = false;
    protected boolean mIsDeleteButtonTouched = false;
    protected boolean mIsDeleteFilm = false;
    protected boolean mIsEditMode = false;
    protected boolean mIsFilterItemChanging = false;
    protected boolean mIsIgnoreTouchEvent = false;
    protected boolean mIsLongPressed = false;
    protected boolean mIsOtherFilmSelected = false;
    protected boolean mIsPositionChanged = false;
    protected boolean mIsRTL = false;
    protected boolean mIsReadyToOpenFilterMenu = false;
    protected boolean mIsScrolled = false;
    protected boolean mIsTurnOnLightFrame = false;
    protected int mLPDownX = 0;
    protected int mLPDownY = 0;
    protected int mLPDx = 0;
    protected int mLPDy = 0;
    protected int[] mLcdSize;
    protected FilmEmulatorEnginInterface mListener;
    protected int mLongPresseStartX = 0;
    protected int mLongPresseStartY = 0;
    protected int mLongPressedIndex = -1;
    protected int mLpScrollPosition = 0;
    protected int mMovePosition;
    protected int mPressedLutNumber = -1;
    protected float mPreviewAspect = 1.7777778f;
    protected int[] mPreviewScreenSize;
    protected int mPreviewStartMargin = 0;
    protected int mScrollDuration;
    protected long mScrollEndTime;
    protected int mScrollPosition = 0;
    protected int mScrollStartPosition;
    protected long mScrollStartTime;
    protected int mScrollState = 0;
    protected int mSelectedFilmMenuIndex = 0;
    protected boolean mShowFilmMenu = false;
    protected long mTouchDownTime = 0;
    protected long mTransAnimStartTime = 0;
    protected float mTranslateAnimationValue = 0.0f;
    /* renamed from: mX */
    protected int f27mX = 0;
    /* renamed from: mY */
    protected int f28mY = 0;

    protected class FilterMenuGestureDetector extends SimpleOnGestureListener {
        protected FilterMenuGestureDetector() {
        }

        public void onLongPress(MotionEvent e) {
            if (CameraConstants.MODE_SMART_CAM.equals(FilmEmulatorManagerBase.this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(FilmEmulatorManagerBase.this.mGet.getShotMode()) || FilmEmulatorManagerBase.this.mFilmEngine == null || FilmEmulatorManagerBase.this.mFilmEngine.getPreviewRenderer() == null || !FunctionProperties.isSupportedFilterDownload() || FilmEmulatorManagerBase.this.mScrollState != 0) {
                CamLog.m3d(CameraConstants.TAG, "smartcam mode is not long press.");
            } else if (System.currentTimeMillis() - FilmEmulatorManagerBase.this.mScrollStartTime < ((long) (FilmEmulatorManagerBase.this.mScrollDuration + 500))) {
                CamLog.m5e(CameraConstants.TAG, "[Film] not ready to onLongPress");
            } else {
                int x = (int) e.getX();
                int y = (int) e.getY();
                int idx = FilmEmulatorManagerBase.this.mFilmEngine.getPreviewRenderer().getNearestView(x, y, -1);
                if (!FilmEmulatorManagerBase.this.mFilmEngine.getPreviewRenderer().getDeleteView(x, y, idx, FilmEmulatorManagerBase.this.getOrientationDegree())) {
                    CamLog.m3d(CameraConstants.TAG, "[Film] onLongPress - selected LUT idx : " + idx);
                    if (!(idx == -1 || idx == 0)) {
                        FilmEmulatorManagerBase.this.mIsLongPressed = true;
                        FilmEmulatorManagerBase.this.mLongPressedIndex = FilmEmulatorManagerBase.this.mFilmHelper.getIndexByLutNumber(FilmEmulatorManagerBase.this.mFilterMenuItemList, idx);
                        FilterMenuItem item = (FilterMenuItem) FilmEmulatorManagerBase.this.mFilterMenuItemList.get(FilmEmulatorManagerBase.this.mLongPressedIndex);
                        FilmEmulatorManagerBase.this.mLongPresseStartX = item.f29mX;
                        FilmEmulatorManagerBase.this.mLongPresseStartY = item.f30mY;
                        FilmEmulatorManagerBase.this.showEditToastPopup();
                        FilmEmulatorManagerBase.this.setEditMode(true);
                        if (FilmEmulatorManagerBase.this.mListener != null) {
                            FilmEmulatorManagerBase.this.mListener.onFilterItemLongPressed();
                        }
                    }
                    FilmEmulatorManagerBase.this.mIsDeleteButtonTouched = false;
                    FilmEmulatorManagerBase.this.mAnimationStartTime = 0;
                    CamLog.m3d(CameraConstants.TAG, "[Film] onLongPress - selected Index : " + FilmEmulatorManagerBase.this.mLongPressedIndex);
                }
            }
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null || FilmEmulatorManagerBase.this.mFilmAnimationState != 0) {
                return false;
            }
            if (CameraConstants.MODE_SMART_CAM.equals(FilmEmulatorManagerBase.this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(FilmEmulatorManagerBase.this.mGet.getShotMode())) {
                CamLog.m3d(CameraConstants.TAG, "smartcam mode is not long press.");
                return false;
            } else if (e1.getY() < ((float) FilmEmulatorManagerBase.this.mFilmMenuTopMargin) || e1.getY() > ((float) (FilmEmulatorManagerBase.this.mFilmMenuTopMargin + FilmEmulatorManagerBase.this.mFilmMenuHeight))) {
                return false;
            } else {
                FilmEmulatorManagerBase.this.mScrollDuration = 300;
                FilmEmulatorManagerBase.this.mMovePosition = (int) (((-1.0f * velocityX) / 2000.0f) * ((float) FilmEmulatorManagerBase.this.mScrollDuration));
                FilmEmulatorManagerBase.this.mScrollState = 1;
                FilmEmulatorManagerBase.this.mScrollStartTime = System.currentTimeMillis();
                FilmEmulatorManagerBase.this.mScrollEndTime = FilmEmulatorManagerBase.this.mScrollStartTime + ((long) FilmEmulatorManagerBase.this.mScrollDuration);
                FilmEmulatorManagerBase.this.mScrollStartPosition = FilmEmulatorManagerBase.this.mDx;
                FilmEmulatorManagerBase.this.mPressedLutNumber = -1;
                FilmEmulatorManagerBase.this.mFilmEngine.pressFilmMenu(FilmEmulatorManagerBase.this.mPressedLutNumber);
                CamLog.m7i(CameraConstants.TAG, "[Film] onFling - mMovePosition : " + FilmEmulatorManagerBase.this.mMovePosition + ", mScrollStartPosition : " + FilmEmulatorManagerBase.this.mScrollStartPosition);
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        }
    }

    public FilmEmulatorManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setFilmEmulatorEnginInterface(FilmEmulatorEnginInterface listener) {
        this.mListener = listener;
    }

    public void setupView() {
        if (FunctionProperties.isSupportedFilmEmulator() && this.mFilmPathList != null) {
            int i;
            FilterMenuItem item;
            if (this.mLcdSize == null) {
                this.mLcdSize = Utils.getLCDsize(getAppContext(), true);
            }
            boolean isSmartCamMode = CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode());
            this.mIsRTL = Utils.isRTLLanguage();
            this.mFilmMenuWidth = this.mLcdSize[1];
            this.mFilmMenuStartMargin = Utils.getPx(getAppContext(), C0088R.dimen.film_menu_marginStart);
            this.mFilmThumbItemHorizontalGap = Utils.getPx(getAppContext(), C0088R.dimen.film_menu_divider_horizon_gap);
            this.mFilmThumbItemVerticalGap = Utils.getPx(getAppContext(), C0088R.dimen.film_menu_divider_vertical_gap);
            this.mFilmMenuEndMargin = this.mFilmThumbItemHorizontalGap;
            this.mFilmThumbItemWidth = (int) (((float) (((this.mFilmMenuWidth - (this.mFilmThumbItemHorizontalGap * 5)) - this.mFilmMenuStartMargin) - this.mFilmMenuEndMargin)) / 5.5f);
            if (isSmartCamMode) {
                this.mFilmMenuStartMargin = Utils.getPx(getAppContext(), C0088R.dimen.smart_cam_film_menu_marginstart);
            }
            this.mFilmThumbItemHeight = this.mFilmThumbItemWidth;
            this.mFilmMenuHeight = this.mFilmThumbItemHeight + (this.mFilmThumbItemVerticalGap * 2);
            setFilterMenuMargin(0);
            this.mFilmMenuBgHeight = this.mFilmMenuHeight + this.mFilmMenuBottomMargin;
            if (isSmartCamMode) {
                this.mFilmMenuBgHeight = this.mFilmMenuHeight;
                this.mFilmMenuBgBottomMargin = this.mFilmMenuBottomMargin;
                this.mFilmMenuBgStartMargin = this.mFilmMenuStartMargin - this.mFilmThumbItemHorizontalGap;
            }
            this.mDeleteBtnSize = getAppContext().getDrawable(C0088R.drawable.btn_camera_edit_deleted_normal).getIntrinsicWidth();
            CamLog.m3d(CameraConstants.TAG, "[Film] ##### START filter menu item list init and sorting!!!!");
            this.mFilterMenuItemList = new ArrayList();
            HashMap<String, FilterMenuItem> filterMenuMap = new HashMap();
            ArrayList<FilterMenuItem> baseFilterMenuItemList = new ArrayList();
            ListPreference listPref = (ListPreference) this.mGet.getListPreference(isSmartCamMode ? Setting.KEY_SMART_CAM_FILTER : Setting.KEY_FILM_EMULATOR);
            String[] entryValues = (String[]) listPref.getEntryValues();
            String[] entry = (String[]) listPref.getEntries();
            String backupFilterList = SharedPreferenceUtilBase.getFilterMenuListOrder(getAppContext());
            String smartCamFilterList = "";
            for (i = 0; i < this.mFilmPathList.length; i++) {
                item = new FilterMenuItem(this.mLcdSize, this.mFilmMenuTopMargin, this.mFilmMenuStartMargin, this.mFilmMenuEndMargin, this.mFilmThumbItemWidth, this.mFilmThumbItemHeight, this.mFilmThumbItemVerticalGap, this.mFilmThumbItemHorizontalGap, this.mFilmMenuBottomMargin, this.mIsRTL);
                item.setLUT(i);
                item.setPosition(i);
                item.mNextPosition = item.mItemPosition;
                item.setEntryValue(entryValues[i]);
                item.setEntry(entry[i]);
                CamLog.m3d(CameraConstants.TAG, "entry value : " + item.mEntryValue + ", downloded : " + item.mIsDownlodaded);
                if (isSmartCamMode) {
                    smartCamFilterList = smartCamFilterList + item.mEntryValue + ",";
                } else {
                    if (entryValues[i].split("_").length > 2) {
                        item.mIsDownlodaded = true;
                    }
                    baseFilterMenuItemList.add(item);
                    if (!(FoodModule.FILM_FOOD.equals(item.mEntryValue) || backupFilterList.contains(item.mEntryValue))) {
                        backupFilterList = backupFilterList + item.mEntryValue + ",";
                    }
                }
                filterMenuMap.put(item.mEntryValue, item);
            }
            CamLog.m7i(CameraConstants.TAG, "[Film] smartCamFilterList : " + smartCamFilterList);
            CamLog.m7i(CameraConstants.TAG, "[Film] backupFilterList : " + backupFilterList);
            if (backupFilterList == null || !"".equals(backupFilterList)) {
                String[] splitList = null;
                if (isSmartCamMode) {
                    if (smartCamFilterList != null) {
                        splitList = smartCamFilterList.split(",");
                    }
                } else if (backupFilterList != null) {
                    splitList = backupFilterList.split(",");
                }
                int index = 0;
                if (splitList != null) {
                    for (i = 0; i < splitList.length; i++) {
                        CamLog.m3d(CameraConstants.TAG, "[Film] i : " + i + ", value :" + splitList[i]);
                        item = (FilterMenuItem) filterMenuMap.get(splitList[i]);
                        if (item == null) {
                            CamLog.m3d(CameraConstants.TAG, "[Film] " + splitList[i] + " is removed film");
                        } else {
                            CamLog.m3d(CameraConstants.TAG, "[Film] entry value : " + item.mEntryValue + ", lut num : " + item.mLUTNumber);
                            int index2 = index + 1;
                            item.setPosition(index);
                            item.mNextPosition = item.mItemPosition;
                            this.mFilterMenuItemList.add(item);
                            index = index2;
                        }
                    }
                }
            } else {
                this.mFilterMenuItemList.addAll(baseFilterMenuItemList);
            }
            CamLog.m3d(CameraConstants.TAG, "[Film] ##### END filter menu item list init and sorting!!!!");
        }
    }

    public void applySceneTextFilter(String selectedFilter) {
        if (this.mFilmIndexHashMap != null && this.mFilmIndexHashMap.size() != 0 && this.mListener != null) {
            CamLog.m3d(CameraConstants.TAG, "[Film] applySceneTextFilter, selectedFilter : " + selectedFilter);
            this.mListener.onFilmEffectChanged(selectedFilter);
            this.mCurSceneIndex = this.mFilmIndexHashMap.get(selectedFilter) != null ? ((Integer) this.mFilmIndexHashMap.get(selectedFilter)).intValue() : 0;
        }
    }

    public void setFilterListForSmartCam(String filterName, int selectedIndex) {
        if (this.mFilmEngine != null) {
            int i;
            FilterMenuItem item;
            this.mFilmEngine.resetLutView();
            this.mFilterMenuItemList = null;
            this.mFilterMenuItemList = new ArrayList();
            String[] contentDesc = new String[]{this.mGet.getAppContext().getResources().getString(C0088R.string.smartcam_filter_name1), this.mGet.getAppContext().getResources().getString(C0088R.string.smartcam_filter_name2), this.mGet.getAppContext().getResources().getString(C0088R.string.smartcam_filter_name3), this.mGet.getAppContext().getResources().getString(C0088R.string.smartcam_filter_name4)};
            HashMap<String, FilterMenuItem> filterMenuMap = new HashMap();
            String[] entryValues = (String[]) ((ListPreference) this.mGet.getListPreference(Setting.KEY_SMART_CAM_FILTER)).getEntryValues();
            String smartCamFilterList = "";
            int contentDescCount = 0;
            for (i = 0; i < entryValues.length; i++) {
                if (entryValues[i].contains(filterName)) {
                    item = new FilterMenuItem(this.mLcdSize, this.mFilmMenuTopMargin, this.mFilmMenuStartMargin, this.mFilmMenuEndMargin, this.mFilmThumbItemWidth, this.mFilmThumbItemHeight, this.mFilmThumbItemVerticalGap, this.mFilmThumbItemHorizontalGap, this.mFilmMenuBottomMargin, false);
                    item.setLUT(i);
                    item.setPosition(i);
                    item.mNextPosition = item.mItemPosition;
                    item.setEntryValue(entryValues[i]);
                    item.setContentDesc(contentDesc[contentDescCount]);
                    filterMenuMap.put(item.mEntryValue, item);
                    smartCamFilterList = smartCamFilterList + item.mEntryValue + ",";
                    contentDescCount++;
                }
            }
            CamLog.m7i(CameraConstants.TAG, "[Film] smartCamFilterList : " + smartCamFilterList);
            String[] splitList = smartCamFilterList.split(",");
            int index = 0;
            for (i = 0; i < splitList.length; i++) {
                item = (FilterMenuItem) filterMenuMap.get(splitList[i]);
                if (item == null) {
                    CamLog.m3d(CameraConstants.TAG, "[Film] " + splitList[i] + " is removed film");
                } else {
                    CamLog.m3d(CameraConstants.TAG, "[Film] entry value : " + item.mEntryValue + ", lut num : " + item.mLUTNumber);
                    int index2 = index + 1;
                    item.setPosition(index);
                    item.mNextPosition = item.mItemPosition;
                    this.mFilterMenuItemList.add(item);
                    index = index2;
                }
            }
            if (this.mFilmIndexHashMap != null && this.mFilmIndexHashMap.size() != 0 && this.mFilmEngine != null && this.mFilterMenuItemList != null) {
                int selectedFilterNumber;
                if (selectedIndex <= -1) {
                    selectedFilterNumber = 0;
                } else {
                    selectedFilterNumber = selectedIndex;
                }
                String selectedEntryValue = ((FilterMenuItem) this.mFilterMenuItemList.get(selectedFilterNumber)).mEntryValue;
                this.mCurSceneIndex = this.mFilmIndexHashMap.get(selectedEntryValue) != null ? ((Integer) this.mFilmIndexHashMap.get(selectedEntryValue)).intValue() : 0;
                this.mFilmEngine.checkSelectedLut(this.mCurSceneIndex);
                if (selectedEntryValue != null) {
                    applySceneTextFilter(selectedEntryValue);
                }
            }
        }
    }

    public void setCurIndex(String curFilmValue) {
        if (this.mFilmIndexHashMap != null && this.mFilmIndexHashMap.size() != 0 && this.mFilmEngine != null) {
            this.mCurFilmValue = curFilmValue;
            this.mCurLutNumber = ((Integer) this.mFilmIndexHashMap.get(curFilmValue)).intValue();
            this.mFilmEngine.checkSelectedLut(this.mCurLutNumber);
        }
    }

    public void initializeAfterCameraOpen() {
        createFilmList();
    }

    public synchronized void createFilmList() {
        if (FunctionProperties.isSupportedFilmEmulator()) {
            if (this.mFilmNamePathArrayList == null || this.mFilmNamePathArrayList.size() == 0) {
                CamLog.m3d(CameraConstants.TAG, "[Film] get film list - start, shot mode : " + this.mGet.getShotMode());
                this.mFilmNamePathArrayList = new ArrayList();
                this.mFilmIndexHashMap = new HashMap();
                if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                    readLUTFileList(LUT_PATH_SMARTCAM, LUT_PATH_SMARTCAM_VENDOR, 81);
                } else {
                    readLUTFileList(LUT_PATH_DEFAULT, LUT_PATH_DEFAULT_VENDOR, 9);
                    readLUTFileList(LUT_PATH_SELFIE, LUT_PATH_SELFIE_VENDOR, 9);
                    readLUTFileList(LUT_DOWNLOAD_PATH, LUT_DOWNLOAD_PATH, -1);
                }
                if ("mode_food".equals(this.mGet.getShotMode())) {
                    readLUTFileList(LUT_PATH_FOOD, LUT_PATH_FOOD_VENDOR, 1);
                }
                if (this.mFilmIndexHashMap.size() == 0) {
                    CamLog.m3d(CameraConstants.TAG, "mFilmIndexHashMap.size() == 0");
                } else {
                    this.mFilmPathList = (String[]) this.mFilmNamePathArrayList.toArray(new String[this.mFilmNamePathArrayList.size()]);
                    CamLog.m3d(CameraConstants.TAG, "[Film] get film list - end");
                    ModuleInterface moduleInterface = this.mGet;
                    String str = (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) ? Setting.KEY_SMART_CAM_FILTER : Setting.KEY_FILM_EMULATOR;
                    this.mCurFilmValue = moduleInterface.getSettingValue(str);
                    CamLog.m3d(CameraConstants.TAG, "[Film] mCurFilmValue : " + this.mCurFilmValue + ", mFilmPathList length : " + this.mFilmPathList.length);
                    if (!(this.mCurFilmValue == null || "not found".equals(this.mCurFilmValue))) {
                        if (this.mFilmIndexHashMap.get(this.mCurFilmValue) == null || this.mFilmIndexHashMap.get(CameraConstants.FILM_NONE) == null) {
                            this.mCurLutNumber = 0;
                        } else {
                            this.mCurLutNumber = ((Integer) this.mFilmIndexHashMap.get(this.mCurFilmValue)).intValue();
                        }
                    }
                }
            }
        }
    }

    public void readLUTFileList(String lutPath, String secondLutPath, int readCount) {
        File[] files = this.mFilmHelper.searchbyFileFilter(new File(lutPath));
        if (files == null) {
            if (readCount == -1) {
                CamLog.m3d(CameraConstants.TAG, "[Film] downloaded film is not exist");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "[Film] read from vendor folder");
            files = this.mFilmHelper.searchbyFileFilter(new File(secondLutPath));
            lutPath = secondLutPath;
            if (files == null) {
                if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                    files = this.mFilmHelper.searchbyFileFilter(new File(LUT_LOCAL_PATH_SMARTCAM));
                    lutPath = LUT_LOCAL_PATH_SMARTCAM;
                } else {
                    files = this.mFilmHelper.searchbyFileFilter(new File(LUT_LOCAL_PATH));
                    lutPath = LUT_LOCAL_PATH;
                }
            }
        }
        if (files == null) {
            CamLog.m11w(CameraConstants.TAG, "[Film] LUT data read fail...");
            if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                readLUTFileList(LUT_PATH_DEFAULT, LUT_PATH_DEFAULT_VENDOR, 9);
                return;
            }
            return;
        }
        ArrayList<String> fileNameArrayList = new ArrayList();
        for (File name : files) {
            fileNameArrayList.add(name.getName());
        }
        if (fileNameArrayList.size() != 0) {
            if (!(readCount != -1 || CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode()))) {
                ListPreference listPref = (ListPreference) this.mGet.getListPreference(Setting.KEY_FILM_EMULATOR);
                this.mFilmHelper.restoreFilterPreference(getAppContext(), listPref);
                this.mFilmHelper.updateDownloadFilterPreference(getAppContext(), fileNameArrayList, listPref, LUT_DOWNLOAD_PATH);
            }
            if (!LUT_DOWNLOAD_PATH.equals(lutPath)) {
                this.mFilmHelper.collectionSortForLUTFile(fileNameArrayList);
            }
            makeFilmListAndMap(lutPath, readCount, files, fileNameArrayList);
        }
    }

    private void makeFilmListAndMap(String lutPath, int readCount, File[] files, ArrayList<String> fileNameArrayList) {
        int startIndex = 0;
        int hashMapSize = this.mFilmIndexHashMap.size();
        if (!(CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode()))) {
            if (this.mFilmNamePathArrayList.size() == 0) {
                if (!CameraConstants.FILM_NONE.equals(fileNameArrayList.get(0))) {
                    this.mFilmNamePathArrayList.add("");
                    hashMapSize = 1;
                }
            } else if ("".equals(this.mFilmNamePathArrayList.get(0)) && CameraConstants.FILM_NONE.equals(fileNameArrayList.get(0))) {
                this.mFilmNamePathArrayList.set(0, lutPath + ((String) fileNameArrayList.get(0)));
                this.mFilmIndexHashMap.put(fileNameArrayList.get(0), Integer.valueOf(0));
                startIndex = 1;
            }
        }
        if (readCount < 0) {
            readCount = files.length;
        }
        if (fileNameArrayList.size() < readCount) {
            CamLog.m5e(CameraConstants.TAG, "lutPath = " + lutPath + ", fileNameArrayList.size() = " + fileNameArrayList.size() + ", readCount = " + readCount);
            Toast.makeText(getAppContext(), "LUT file is missing!", 1).show();
            readCount = fileNameArrayList.size();
        }
        CamLog.m3d(CameraConstants.TAG, "[Film] path : " + lutPath + ", startIndex : " + startIndex + ", readCount : " + readCount);
        for (int i = startIndex; i < readCount; i++) {
            this.mFilmNamePathArrayList.add(lutPath + ((String) fileNameArrayList.get(i)));
            if (LUT_DOWNLOAD_PATH.equals(lutPath)) {
                this.mFilmIndexHashMap.put(lutPath + ((String) fileNameArrayList.get(i)), Integer.valueOf(hashMapSize + i));
            } else {
                this.mFilmIndexHashMap.put(fileNameArrayList.get(i), Integer.valueOf(hashMapSize + i));
            }
        }
    }

    public boolean isRunningFilmEmulator() {
        if (this.mFilmState >= 3) {
            return true;
        }
        return false;
    }

    public int getFilmState() {
        return this.mFilmState;
    }

    public void setFilmState(int state) {
        CamLog.m3d(CameraConstants.TAG, "[Film] setFilmState : " + state);
        this.mFilmState = state;
    }

    public int getCurrentFilmIndex() {
        if (FunctionProperties.isSupportedFilmEmulator()) {
            return this.mCurLutNumber;
        }
        return 0;
    }

    public void setReadyToOpenFilterMenu(boolean readyToOpenFilterMenu) {
        this.mIsReadyToOpenFilterMenu = readyToOpenFilterMenu;
    }

    public boolean isReadyToOpenFilterMenu() {
        return this.mIsReadyToOpenFilterMenu;
    }

    public void stopFilmEmulator(boolean isRestartPreview, boolean isStopByRecording) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mFilmEngine == null || !isRunningFilmEmulator() || ((!this.mShowFilmMenu && this.mFilmAnimationState == 0) || (this.mFilmAnimationState & 8) != 0)) {
            return false;
        }
        if (this.mFilmGestureDetector != null && this.mFilmGestureDetector.onTouchEvent(event)) {
            return true;
        }
        int degree = getOrientationDegree();
        this.f27mX = (int) event.getX();
        this.f28mY = (int) event.getY();
        switch (event.getAction()) {
            case 0:
                if (this.mScrollState != 0) {
                    this.mScrollEndTime = 0;
                    this.mScrollState = 0;
                    this.mScrollPosition += this.mDx;
                    releaseTouchUp();
                }
                doTouchDownAction();
                return true;
            case 1:
                if (this.mIsIgnoreTouchEvent) {
                    this.mGet.getHandler().sendEmptyMessage(84);
                }
                doTouchUpAction(event, degree);
                this.mPressedLutNumber = -1;
                this.mFilmEngine.pressFilmMenu(this.mPressedLutNumber);
                return true;
            case 2:
                if (this.mIsIgnoreTouchEvent || this.mIsDeleteButtonTouched) {
                    return true;
                }
                doTouchMoveAction(degree);
                return true;
            default:
                return true;
        }
    }

    private void doTouchDownAction() {
        CamLog.m7i(CameraConstants.TAG, "[Film] doTouchDownAction");
        this.mDownX = this.f27mX;
        this.mDownY = this.f28mY;
        this.mIsScrolled = false;
        this.mTouchDownTime = System.currentTimeMillis();
        if (((null == null || 0 == 90) && (this.f28mY < this.mFilmMenuTopMargin || this.f28mY > this.mFilmMenuTopMargin + this.mFilmMenuHeight)) || ((0 == 180 && this.f28mY > this.mFilmMenuHeight) || (0 == 270 && this.f28mY > this.mFilmMenuHeight + RatioCalcUtil.getQuickButtonWidth(getAppContext())))) {
            this.mIsIgnoreTouchEvent = true;
        } else {
            this.mIsIgnoreTouchEvent = false;
        }
        int pressedLutNumber = this.mFilmEngine.getPreviewRenderer().getNearestView(this.f27mX, this.f28mY, -1);
        if (pressedLutNumber != -1) {
            this.mPressedLutNumber = pressedLutNumber;
            this.mIsDeleteButtonTouched = false;
            if (this.mIsEditMode) {
                if (((FilterMenuItem) this.mFilterMenuItemList.get(this.mFilmHelper.getIndexByLutNumber(this.mFilterMenuItemList, this.mPressedLutNumber))).mIsDownlodaded && this.mFilmEngine.getPreviewRenderer().getDeleteView(this.f27mX, this.f28mY, this.mPressedLutNumber, getOrientationDegree())) {
                    this.mIsDeleteButtonTouched = true;
                }
            }
            if (!this.mIsDeleteButtonTouched) {
                this.mFilmEngine.pressFilmMenu(this.mPressedLutNumber);
            }
        }
    }

    public boolean doTouchMoveAction(int degree) {
        CamLog.m7i(CameraConstants.TAG, "[Film] doTouchMoveAction");
        this.mIsDeleteButtonTouched = false;
        int dx = this.mDownX - this.f27mX;
        int dy = this.mDownY - this.f28mY;
        if (this.mIsLongPressed) {
            checkScrollpositionOnLongPressed(dx, dy, degree);
        } else if (!checkFilterMenuBoundary(dx, dy, degree)) {
            this.mDx = 0;
            this.mDy = 0;
            return false;
        }
        this.mDx = dx;
        this.mDy = dy;
        this.mIsScrolled = true;
        if (!this.mIsLongPressed && (Math.abs(dx) > 50 || Math.abs(dy) > 50)) {
            this.mPressedLutNumber = -1;
            this.mFilmEngine.pressFilmMenu(this.mPressedLutNumber);
        }
        if (this.mIsLongPressed && !this.mIsFilterItemChanging) {
            FilterMenuItem longPressedItem = (FilterMenuItem) this.mFilterMenuItemList.get(this.mLongPressedIndex);
            int selectedLutNumber = this.mFilmEngine.getPreviewRenderer().getNearestView(this.f27mX, this.f28mY, longPressedItem.mLUTNumber);
            if (selectedLutNumber == -1 || longPressedItem.mLUTNumber == selectedLutNumber || selectedLutNumber <= 0) {
                return false;
            }
            this.mSelectedFilmMenuIndex = this.mFilmHelper.getIndexByLutNumber(this.mFilterMenuItemList, selectedLutNumber);
            arrangeNextPostion(selectedLutNumber);
        }
        return true;
    }

    private void checkScrollpositionOnLongPressed(int dx, int dy, int degree) {
        if (this.mIsRTL || !(!this.mFilterItemFourWayRotation || degree == 0 || degree == 90)) {
            checkLandscapeScrollBoundaryOnLongPressed((FilterMenuItem) this.mFilterMenuItemList.get(this.mFilterMenuItemList.size() - 1), (FilterMenuItem) this.mFilterMenuItemList.get(0));
        } else {
            checkLandscapeScrollBoundaryOnLongPressed((FilterMenuItem) this.mFilterMenuItemList.get(0), (FilterMenuItem) this.mFilterMenuItemList.get(this.mFilterMenuItemList.size() - 1));
        }
    }

    private void checkPortraitScrollBoundaryOnLongPressed(FilterMenuItem top, FilterMenuItem bottom) {
        if (this.f28mY < this.mFilmMenuTopMargin) {
            if ((((top.f30mY + top.mHeight) + this.mFilmThumbItemVerticalGap) + this.mScrollPosition) + this.mLpScrollPosition > this.mFilmMenuBottomMargin + this.mFilmMenuHeight) {
                this.mLpScrollPosition -= 20;
            }
        } else if (this.f28mY > this.mFilmMenuTopMargin + this.mFilmMenuHeight && ((bottom.f30mY - this.mFilmThumbItemVerticalGap) + this.mScrollPosition) + this.mLpScrollPosition < this.mFilmMenuBottomMargin) {
            this.mLpScrollPosition += 20;
        }
    }

    private void checkLandscapeScrollBoundaryOnLongPressed(FilterMenuItem top, FilterMenuItem bottom) {
        if (((float) this.f27mX) < ((float) this.mFilmThumbItemWidth) / 2.0f) {
            if (((top.f29mX - this.mFilmMenuStartMargin) - this.mScrollPosition) - this.mLpScrollPosition < 0) {
                this.mLpScrollPosition -= 20;
            }
        } else if (((float) this.f27mX) > ((float) this.mLcdSize[1]) - (((float) this.mFilmThumbItemWidth) / 2.0f) && (((bottom.f29mX + bottom.mHeight) + this.mFilmMenuEndMargin) - this.mScrollPosition) - this.mLpScrollPosition > this.mLcdSize[1]) {
            this.mLpScrollPosition += 20;
        }
    }

    private void arrangeNextPostion(int selectedLutNumber) {
        CamLog.m3d(CameraConstants.TAG, "[Film] selected Lut : " + selectedLutNumber + ", selectedFilmMenuIndex : " + this.mSelectedFilmMenuIndex);
        int i;
        FilterMenuItem item;
        if (this.mLongPressedIndex > this.mSelectedFilmMenuIndex) {
            i = 0;
            while (i < this.mFilterMenuItemList.size()) {
                item = (FilterMenuItem) this.mFilterMenuItemList.get(i);
                if (i == this.mLongPressedIndex) {
                    item.mNextPosition = this.mSelectedFilmMenuIndex;
                } else if (i < this.mSelectedFilmMenuIndex || i >= this.mLongPressedIndex) {
                    item.mNextPosition = item.mItemPosition;
                } else {
                    item.mNextPosition = ((FilterMenuItem) this.mFilterMenuItemList.get(i + 1)).mItemPosition;
                }
                i++;
            }
            this.mIsPositionChanged = true;
        } else if (this.mLongPressedIndex < this.mSelectedFilmMenuIndex) {
            i = 0;
            while (i < this.mFilterMenuItemList.size()) {
                item = (FilterMenuItem) this.mFilterMenuItemList.get(i);
                if (i == this.mLongPressedIndex) {
                    item.mNextPosition = this.mSelectedFilmMenuIndex;
                } else if (i <= this.mLongPressedIndex || i > this.mSelectedFilmMenuIndex) {
                    item.mNextPosition = item.mItemPosition;
                } else {
                    item.mNextPosition = ((FilterMenuItem) this.mFilterMenuItemList.get(i - 1)).mItemPosition;
                }
                i++;
            }
            this.mIsPositionChanged = true;
        } else {
            this.mIsPositionChanged = false;
        }
    }

    public boolean doTouchUpAction(MotionEvent event, int degree) {
        CamLog.m7i(CameraConstants.TAG, "[Film] doTouchUpAction");
        if (this.mScrollState == 1) {
            return false;
        }
        if (this.mIsDeleteButtonTouched) {
            this.mDeleteLutNumber = this.mPressedLutNumber;
            playClickSound();
            this.mGet.showDialog(149);
        } else {
            long timeDiff = System.currentTimeMillis() - this.mTouchDownTime;
            int dx = ((int) event.getX()) - this.mDownX;
            int dy = ((int) event.getY()) - this.mDownY;
            if (!this.mIsLongPressed && Math.abs(dx) < 30 && Math.abs(dy) < 30) {
                if (!this.mGet.checkModuleValidate(48)) {
                    return true;
                }
                int selectedIdx = this.mFilmEngine.getPreviewRenderer().getNearestView(this.f27mX, this.f28mY, -1);
                if (selectedIdx != -1 && timeDiff < 300) {
                    FilterMenuItem item = getSelectedFilterItem(selectedIdx);
                    if (item == null) {
                        return false;
                    }
                    String selectedFilmValue = item.mEntryValue;
                    if (this.mCurFilmValue.equals(selectedFilmValue) || CameraConstants.FILM_NONE.equals(selectedFilmValue)) {
                        this.mIsOtherFilmSelected = false;
                    } else {
                        this.mIsOtherFilmSelected = true;
                        resetFilmStrength();
                    }
                    boolean isSmartcamMode = CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode());
                    if (this.mListener != null) {
                        this.mListener.onFilmEffectChanged(selectedFilmValue);
                        playClickSound();
                        TalkBackUtil.sendAccessibilityEvent(getAppContext(), getClass().getName(), isSmartcamMode ? item.mContentDesc : item.mEntry);
                    }
                    if (this.mFilmEngine != null) {
                        this.mFilmEngine.checkSelectedLut(selectedIdx);
                    }
                    this.mCurFilmValue = selectedFilmValue;
                    if (isSmartcamMode) {
                        this.mCurSceneIndex = selectedIdx;
                    } else {
                        this.mCurLutNumber = selectedIdx;
                    }
                }
            }
        }
        if (this.mIsLongPressed) {
            this.mScrollPosition += this.mLpScrollPosition;
            this.mLpScrollPosition = 0;
        } else {
            this.mScrollPosition += this.mDx;
        }
        CamLog.m3d(CameraConstants.TAG, "[Film] mScrollPosition : " + this.mScrollPosition);
        releaseTouchUp();
        return true;
    }

    private void releaseTouchUp() {
        this.mIsScrolled = false;
        this.mDownX = 0;
        this.mDownY = 0;
        this.mDx = 0;
        this.mDy = 0;
        this.mLPDownX = 0;
        this.mLPDownY = 0;
        this.mLPDx = 0;
        this.mLPDy = 0;
        this.mIsLongPressed = false;
        this.mLongPressedIndex = -1;
        this.mIsDeleteButtonTouched = false;
        this.mIsIgnoreTouchEvent = false;
    }

    public void deleteDownloadedFilter() {
        this.mDeletedIndex = this.mFilmHelper.getIndexByLutNumber(this.mFilterMenuItemList, this.mDeleteLutNumber);
        int i;
        FilterMenuItem item;
        if (((FilterMenuItem) this.mFilterMenuItemList.get(this.mFilterMenuItemList.size() - 1)).f29mX - this.mScrollPosition <= this.mLcdSize[1]) {
            for (i = 0; i < this.mFilterMenuItemList.size(); i++) {
                item = (FilterMenuItem) this.mFilterMenuItemList.get(i);
                if (i <= this.mDeletedIndex) {
                    item.mNextPosition = item.mItemPosition + 1;
                } else {
                    item.mNextPosition = item.mItemPosition;
                }
            }
        } else {
            for (i = 0; i < this.mFilterMenuItemList.size(); i++) {
                item = (FilterMenuItem) this.mFilterMenuItemList.get(i);
                if (i <= this.mDeletedIndex) {
                    item.mNextPosition = item.mItemPosition;
                } else {
                    item.mNextPosition = item.mItemPosition - 1;
                }
            }
        }
        String deletePath = ((FilterMenuItem) this.mFilterMenuItemList.get(this.mDeletedIndex)).mEntryValue;
        CamLog.m3d(CameraConstants.TAG, "[Film], deletePath : " + deletePath);
        FileManager.deleteFile(deletePath);
        if (this.mCurLutNumber == this.mDeleteLutNumber) {
            int nextIndex = this.mDeletedIndex - 1;
            String selectedFilmValue = ((FilterMenuItem) this.mFilterMenuItemList.get(nextIndex)).mEntryValue;
            if (this.mListener != null) {
                this.mListener.onFilmEffectChanged(selectedFilmValue);
            }
            if (this.mFilmEngine != null) {
                this.mFilmEngine.checkSelectedLut(((FilterMenuItem) this.mFilterMenuItemList.get(nextIndex)).mLUTNumber);
            }
            this.mCurFilmValue = selectedFilmValue;
            this.mCurLutNumber = ((FilterMenuItem) this.mFilterMenuItemList.get(nextIndex)).mLUTNumber;
        }
        this.mIsPositionChanged = true;
        this.mIsDeleteFilm = true;
    }

    public boolean isFilmMenuTouched(MotionEvent event) {
        if (isRunningFilmEmulator() && this.mShowFilmMenu && ((int) event.getY()) > this.mFilmMenuTopMargin && ((int) event.getY()) < this.mFilmMenuHeight) {
            return true;
        }
        return false;
    }

    protected void calculateScrollPosition() {
        if (this.mScrollState == 1) {
            long curTime = System.currentTimeMillis();
            if (curTime <= this.mScrollEndTime) {
                int dx = this.mScrollStartPosition + ((int) ((double) (((float) this.mMovePosition) * this.mInterpolator.getInterpolation(Math.min(((float) (curTime - this.mScrollStartTime)) / ((float) this.mScrollDuration), 1.0f)))));
                if (checkFilterMenuBoundary(dx, 0, 0)) {
                    this.mDx = dx;
                    return;
                }
                this.mScrollEndTime = 0;
                this.mScrollState = 0;
                releaseTouchUp();
                return;
            }
            this.mScrollEndTime = 0;
            this.mScrollState = 0;
            this.mScrollPosition += this.mDx;
            releaseTouchUp();
            CamLog.m3d(CameraConstants.TAG, "[Film] fling end");
        }
    }

    public void showFilmMenu(boolean show, int animType, boolean useHandler, boolean isRestartPreview, int delay, boolean updateQuickButton) {
        boolean z = false;
        if (!isRunningFilmEmulator()) {
            menuHandleDone();
            if (!show) {
                this.mGet.setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
            }
        } else if (show) {
            boolean aiCam;
            if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                aiCam = true;
            } else {
                aiCam = false;
            }
            int orientationDegree = getOrientationDegree();
            if (!aiCam) {
                z = true;
            }
            setFilterMenuOrientation(orientationDegree, z);
            doShowAndHideFilmMenu(true, animType, isRestartPreview, updateQuickButton);
        } else {
            doShowAndHideFilmMenu(false, animType, isRestartPreview, updateQuickButton);
            this.mScrollPosition = 0;
            this.mLpScrollPosition = 0;
            releaseTouchUp();
            this.mPressedLutNumber = -1;
            if (this.mFilmEngine != null) {
                this.mFilmEngine.pressFilmMenu(this.mPressedLutNumber);
            }
        }
    }

    protected void doShowAndHideFilmMenu(boolean show, int animType, boolean restoreFilm, final boolean updateQuickButton) {
        if (isRunningFilmEmulator()) {
            CamLog.m3d(CameraConstants.TAG, "[Film] do show ? " + show + ", is filter menu showing ? " + this.mShowFilmMenu + ", anim type ? " + animType + ", restore ?" + restoreFilm);
            boolean isSmartCamMode = CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode());
            if (show) {
                if (this.mShowFilmMenu) {
                    CamLog.m3d(CameraConstants.TAG, "[Film] film menu already show");
                    menuHandleDone();
                    return;
                }
                int selectedMenuIndex = this.mFilmHelper.getIndexByLutNumber(this.mFilterMenuItemList, isSmartCamMode ? this.mCurSceneIndex : this.mCurLutNumber);
                int halfSideItemCnt = 2;
                if (isSmartCamMode) {
                    halfSideItemCnt = 4;
                }
                this.mScrollPosition = this.mFilmHelper.getCenterScrollPosition(selectedMenuIndex, halfSideItemCnt, this.mFilterMenuItemList, this.mLcdSize, this.mFilmMenuEndMargin, this.mIsRTL);
                if (this.mFilmEngine != null) {
                    this.mFilmEngine.checkSelectedLut(isSmartCamMode ? this.mCurSceneIndex : this.mCurLutNumber);
                }
                if (animType == 3) {
                    this.mShowFilmMenu = true;
                    this.mFilmAnimationState = 129;
                } else if (animType == 2) {
                    this.mShowFilmMenu = true;
                    this.mFilmAnimationState = 257;
                } else {
                    this.mFilmAnimationState = 0;
                    this.mShowFilmMenu = true;
                }
                this.mIsOtherFilmSelected = false;
                this.mGet.updateIndicatorPosition(1);
                menuHandleDone();
            } else if (this.mShowFilmMenu) {
                this.mIsEditMode = false;
                if (animType == 3) {
                    this.mListener.updateFilterQuickButton(updateQuickButton);
                    this.mFilmAnimationState = 130;
                } else if (animType == 2) {
                    this.mListener.updateFilterQuickButton(updateQuickButton);
                    this.mFilmAnimationState = 258;
                } else {
                    this.mFilmAnimationState = 0;
                    this.mShowFilmMenu = show;
                    CamLog.m3d(CameraConstants.TAG, "[FilmStrength] mCurFilmValue : " + this.mCurFilmValue + ", setting value : " + this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR) + ", mIsOtherFilmSelected : " + this.mIsOtherFilmSelected);
                    if (!restoreFilm || this.mCurLutNumber != 0 || CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                        this.mListener.updateFilterQuickButton(updateQuickButton);
                        setFilmStrengthButtonVisibility(true, this.mIsOtherFilmSelected);
                        menuHandleDone();
                    } else {
                        this.mFilmState = 5;
                        setFilmStrengthButtonVisibility(false, false);
                        if (this.mIsTurnOnLightFrame) {
                            setFilmPreviewAlpha(true);
                        }
                        this.mGet.postOnUiThread(new HandlerRunnable(this) {
                            public void handleRun() {
                                FilmEmulatorManagerBase.this.mListener.updateFilterQuickButton(updateQuickButton);
                                FilmEmulatorManagerBase.this.stopFilmEmulator(true, false);
                            }
                        }, 50);
                    }
                }
                this.mGet.updateIndicatorPosition(2);
            } else {
                CamLog.m3d(CameraConstants.TAG, "[Film] film menu already hide");
                if (!isSmartCamMode) {
                    this.mGet.setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 1);
                    this.mGet.setQuickButtonSelected(C0088R.id.quick_button_film_emulator, true);
                }
                menuHandleDone();
            }
        }
    }

    private void menuHandleDone() {
        if (this.mListener != null) {
            CamLog.m3d(CameraConstants.TAG, "[Film] menuHandleDone");
            this.mListener.onFilmMenuHandleDone();
        }
    }

    public boolean isShowingFilmMenu() {
        if (isRunningFilmEmulator()) {
            return this.mShowFilmMenu;
        }
        return false;
    }

    public void setDegree(int degree, boolean animation) {
        boolean z = false;
        super.setDegree(degree, animation);
        if (isRunningFilmEmulator() && this.mShowFilmMenu) {
            boolean aiCam;
            this.mDx = 0;
            this.mDy = 0;
            this.mShowFilmMenu = false;
            if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                aiCam = true;
            } else {
                aiCam = false;
            }
            if (!aiCam) {
                z = true;
            }
            setFilterMenuOrientation(degree, z);
            this.mShowFilmMenu = true;
        }
    }

    public void changePreviewSize(String previewSize, boolean isRecordingPreview) {
        if (isRunningFilmEmulator()) {
            CamLog.m3d(CameraConstants.TAG, "[Film] previewSize : " + previewSize);
            this.mPreviewScreenSize = Utils.sizeStringToArray(previewSize);
            this.mPreviewAspect = ((float) this.mPreviewScreenSize[0]) / ((float) this.mPreviewScreenSize[1]);
            if (ModelProperties.isLongLCDModel()) {
                this.mPreviewStartMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), this.mPreviewScreenSize[0], this.mPreviewScreenSize[1], 0);
                if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    if (ModelProperties.getLCDType() == 2) {
                        this.mPreviewStartMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
                    } else {
                        this.mPreviewStartMargin = 0;
                    }
                }
            } else if (this.mPreviewAspect > 1.5f) {
                this.mPreviewStartMargin = 0;
            } else if (this.mPreviewAspect > 1.2f) {
                this.mPreviewStartMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
            } else {
                this.mPreviewStartMargin = (this.mLcdSize[0] - this.mLcdSize[1]) / 2;
            }
            if (!isRecordingPreview && "on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                this.mPreviewStartMargin = RatioCalcUtil.getTilePreviewMargin(this.mGet.getAppContext(), this.mGet.isAvailableTilePreview(), this.mPreviewAspect, this.mPreviewStartMargin);
            }
            if (isRecordingPreview) {
                if (this.mPreviewAspect <= 2.3f) {
                    this.mPreviewScreenSize[1] = this.mLcdSize[1];
                    this.mPreviewScreenSize[0] = (int) (((float) this.mLcdSize[1]) * this.mPreviewAspect);
                } else if (ModelProperties.getLCDType() == 2) {
                    this.mPreviewScreenSize[0] = (this.mLcdSize[0] - this.mPreviewStartMargin) - RatioCalcUtil.getNavigationBarHeight(getAppContext());
                    this.mPreviewScreenSize[1] = (int) (((float) this.mPreviewScreenSize[0]) / this.mPreviewAspect);
                } else {
                    this.mPreviewScreenSize[0] = this.mLcdSize[0];
                    this.mPreviewScreenSize[1] = (int) (((float) this.mLcdSize[0]) / this.mPreviewAspect);
                }
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (FilmEmulatorManagerBase.this.mFilmEngine != null) {
                        FilmEmulatorManagerBase.this.mFilmEngine.changePictureSize(FilmEmulatorManagerBase.this.mPreviewAspect);
                    }
                }
            }, 300);
            CamLog.m3d(CameraConstants.TAG, "[Film] Preview aspect ratio is : " + this.mPreviewAspect);
        }
    }

    public void turnOnLightFrame(boolean turnOn) {
        this.mIsTurnOnLightFrame = false;
    }

    public void setFilmPreviewAlpha(boolean isAlpha) {
        if (this.mFilmSurfaceView != null) {
            CamLog.m3d(CameraConstants.TAG, "[Film] hide film surfaceview by alpha : " + isAlpha);
            if (isAlpha) {
                this.mFilmSurfaceView.setAlpha(0.0f);
            } else {
                this.mFilmSurfaceView.setAlpha(1.0f);
            }
        }
    }

    public void setFilmStrengthButtonVisibility(boolean visible, boolean needShowBar) {
    }

    public void resetFilmStrength() {
    }

    protected void onShowFilterMenu() {
        CamLog.m3d(CameraConstants.TAG, "[Film] onShowFilterMenu");
    }

    protected void onHideFilterMenu() {
        boolean isRestoreFilmEngine;
        CamLog.m3d(CameraConstants.TAG, "[Film] onHideFilterMenu, mCurIndex = " + this.mCurLutNumber);
        if (this.mCurLutNumber != 0 || CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
            isRestoreFilmEngine = false;
        } else {
            isRestoreFilmEngine = true;
        }
        if (this.mGet.isMenuShowing(3) || !isRestoreFilmEngine) {
            this.mShowFilmMenu = false;
        } else {
            doShowAndHideFilmMenu(false, 0, isRestoreFilmEngine, true);
        }
        this.mScrollPosition = 0;
        this.f27mX = 0;
        this.f28mY = 0;
        this.mIsEditMode = false;
    }

    protected FilterMenuItem getSelectedFilterItem(int selectedLut) {
        for (int i = 0; i < this.mFilterMenuItemList.size(); i++) {
            if (selectedLut == ((FilterMenuItem) this.mFilterMenuItemList.get(i)).mLUTNumber) {
                return (FilterMenuItem) this.mFilterMenuItemList.get(i);
            }
        }
        return null;
    }

    protected boolean checkFilterMenuBoundary(int dx, int dy, int degree) {
        if (this.mFilterMenuItemList.size() <= 5) {
            this.mDownX = this.f27mX;
            this.mScrollPosition = 0;
            return false;
        } else if (this.mIsRTL || (this.mFilterItemFourWayRotation && degree != 0 && degree != 90)) {
            return checkLandscapeBoundary((FilterMenuItem) this.mFilterMenuItemList.get(this.mFilterMenuItemList.size() - 1), (FilterMenuItem) this.mFilterMenuItemList.get(0), dx, this.mScrollPosition);
        } else {
            return checkLandscapeBoundary((FilterMenuItem) this.mFilterMenuItemList.get(0), (FilterMenuItem) this.mFilterMenuItemList.get(this.mFilterMenuItemList.size() - 1), dx, this.mScrollPosition);
        }
    }

    public boolean checkPortraitBoundary(FilterMenuItem top, FilterMenuItem bottom, int dy, int scrollPosition) {
        if ((((top.f30mY + dy) + scrollPosition) + top.mHeight) + this.mFilmThumbItemVerticalGap <= this.mFilmMenuBottomMargin + this.mFilmMenuHeight) {
            this.mDownY = this.f28mY;
            this.mScrollPosition = (((this.mFilmMenuBottomMargin + this.mFilmMenuHeight) - this.mFilmThumbItemVerticalGap) - top.f30mY) - top.mHeight;
            return false;
        } else if (((bottom.f30mY + dy) + scrollPosition) - this.mFilmThumbItemVerticalGap < this.mFilmMenuBottomMargin) {
            return true;
        } else {
            this.mDownY = this.f28mY;
            this.mScrollPosition = (this.mFilmMenuBottomMargin + this.mFilmThumbItemVerticalGap) - bottom.f30mY;
            return false;
        }
    }

    public boolean checkLandscapeBoundary(FilterMenuItem top, FilterMenuItem bottom, int dx, int scrollPosition) {
        if (((top.f29mX - dx) - scrollPosition) - this.mFilmMenuStartMargin >= 0) {
            this.mDownX = this.f27mX;
            this.mScrollPosition = top.f29mX - this.mFilmMenuStartMargin;
            return false;
        } else if ((((bottom.f29mX + bottom.mWidth) - dx) - scrollPosition) + this.mFilmMenuEndMargin > this.mLcdSize[1]) {
            return true;
        } else {
            this.mDownX = this.f27mX;
            this.mScrollPosition = (((-this.mLcdSize[1]) + bottom.f29mX) + bottom.mWidth) + this.mFilmMenuEndMargin;
            return false;
        }
    }

    public void setFilterMenuOrientation(int degree, boolean updateFilterName) {
        if (this.mFilmEngine != null) {
            this.mFilmEngine.setDegree(degree, updateFilterName);
        }
        int convertDegree = 0;
        setFilterMenuMargin(0);
        if (this.mFilterItemFourWayRotation) {
            convertDegree = degree;
        }
        if (this.mFilterMenuItemList != null) {
            for (int i = 0; i < this.mFilterMenuItemList.size(); i++) {
                ((FilterMenuItem) this.mFilterMenuItemList.get(i)).mTopMargin = this.mFilmMenuTopMargin;
                ((FilterMenuItem) this.mFilterMenuItemList.get(i)).mFilterMenuBottomMargin = this.mFilmMenuBottomMargin;
                ((FilterMenuItem) this.mFilterMenuItemList.get(i)).setDegree(convertDegree);
                ((FilterMenuItem) this.mFilterMenuItemList.get(i)).updatePosition();
            }
        }
    }

    private void setFilterMenuMargin(int fixedDegree) {
        if (fixedDegree == 270 || fixedDegree == 180) {
            this.mFilmMenuTopMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
            this.mFilmMenuBottomMargin = (this.mLcdSize[0] - this.mFilmMenuHeight) - this.mFilmMenuTopMargin;
            return;
        }
        int filterMenuTotalHeight;
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            if (ModelProperties.getLCDType() == 2) {
                filterMenuTotalHeight = this.mLcdSize[1] + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.123f);
            } else {
                filterMenuTotalHeight = this.mLcdSize[1] + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.133f);
            }
        } else if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
            if (ModelProperties.getLCDType() == 2) {
                filterMenuTotalHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.4205f) - RatioCalcUtil.getNotchDisplayHeight(getAppContext());
            } else {
                filterMenuTotalHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.369f);
            }
        } else if (ModelProperties.getLCDType() == 2) {
            filterMenuTotalHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.349f);
        } else {
            filterMenuTotalHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.336f);
        }
        this.mFilmMenuBottomMargin = filterMenuTotalHeight - this.mFilmMenuHeight;
        this.mFilmMenuTopMargin = this.mLcdSize[0] - filterMenuTotalHeight;
    }

    public void setEditMode(boolean setEditMode) {
        this.mIsEditMode = setEditMode;
    }

    public void setFilmStrength(float filmStrength) {
        this.mFilmStrength = filmStrength;
    }

    public boolean isEditMode() {
        return this.mIsEditMode;
    }

    public void playClickSound() {
        if (this.mFilmSurfaceView != null) {
            this.mFilmSurfaceView.playSoundEffect(0);
        }
    }

    public boolean isFilterMenuAnimationWorking() {
        if ((this.mFilmAnimationState & 8) == 0 && (this.mFilmAnimationState & 4) == 0) {
            return false;
        }
        return true;
    }

    public void showEditToastPopup() {
        if (!this.mIsEditMode) {
            this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.filter_edit_guide), CameraConstants.TOAST_LENGTH_SHORT);
        }
    }

    public boolean isSetDownloadFilm(String filmValue) {
        if (!CameraConstants.FILM_NONE.equals(filmValue)) {
            return false;
        }
        String savedValue = SharedPreferenceUtilBase.getLastSelectFilter(getAppContext());
        if (CameraConstants.FILM_NONE.equals(savedValue)) {
            return false;
        }
        if (new File(savedValue).exists()) {
            return true;
        }
        SharedPreferenceUtilBase.saveLastSelectFilter(getAppContext(), CameraConstants.FILM_NONE);
        return false;
    }
}
