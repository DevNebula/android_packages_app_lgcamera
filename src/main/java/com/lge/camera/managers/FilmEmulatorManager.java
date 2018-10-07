package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.app.ext.FoodModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import com.lge.filmemulator.FilmEmulatorEngine;
import com.lge.filmemulatorengine.FilmEmulatorRendererPreview;
import com.lge.filmemulatorengine.OnFilmEmulationViewListener;

public class FilmEmulatorManager extends FilmEmulatorManagerBase {
    public Callback mCallback = new C08081();
    private int mCurrentSurfaceTextureNumber = 1;
    protected OnFilmEmulationViewListener mFilmListener = new C08114();
    protected FilterMenuAnimationInfo mFilterMenuAniInfo = new FilterMenuAnimationInfo();
    protected float mFilterMenuAnimationDuration = 300.0f;
    int mReverseFlag = 0;

    /* renamed from: com.lge.camera.managers.FilmEmulatorManager$1 */
    class C08081 implements Callback {
        C08081() {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            CamLog.m3d(CameraConstants.TAG, "[Film] surfaceCreated");
            if (FilmEmulatorManager.this.mHolder == holder && FilmEmulatorManager.this.mFilmEngine == null) {
                CamLog.m3d(CameraConstants.TAG, "[Film] engine init - start");
                if (FilmEmulatorManager.this.mFilmPathList == null) {
                    FilmEmulatorManager.this.createFilmList();
                }
                FilmEmulatorManager.this.mLcdSize = Utils.getLCDsize(FilmEmulatorManager.this.getAppContext(), true);
                Drawable[] deleteBtnDrawable = new Drawable[]{FilmEmulatorManager.this.getAppContext().getDrawable(C0088R.drawable.btn_camera_edit_deleted_normal), FilmEmulatorManager.this.getAppContext().getDrawable(C0088R.drawable.btn_camera_edit_deleted_pressed)};
                int[] pictureSize = Utils.sizeStringToArray(FilmEmulatorManager.this.mGet.getSettingValue(SettingKeyWrapper.getPictureSizeKey(FilmEmulatorManager.this.mGet.getShotMode(), FilmEmulatorManager.this.mGet.getCameraId())));
                ModuleInterface moduleInterface = FilmEmulatorManager.this.mGet;
                String str = (CameraConstants.MODE_SMART_CAM.equals(FilmEmulatorManager.this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(FilmEmulatorManager.this.mGet.getShotMode())) ? Setting.KEY_SMART_CAM_FILTER : Setting.KEY_FILM_EMULATOR;
                ListPreference listPref = (ListPreference) moduleInterface.getListPreference(str);
                if (listPref != null) {
                    FilmEmulatorManager.this.mFilmEngine = new FilmEmulatorEngine(holder.getSurface(), FilmEmulatorManager.this.mFilmPathList, 36, !FilmEmulatorManager.this.mGet.isRearCamera(), deleteBtnDrawable, null, FilmEmulatorManager.this.mFilmListener, ((float) pictureSize[0]) / ((float) pictureSize[1]), (String[]) listPref.getEntries(), 19);
                    FilmEmulatorManager.this.mFilmEngine.checkSelectedLut(FilmEmulatorManager.this.mCurLutNumber);
                }
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "[Film] surfaceChanged");
            if (FilmEmulatorManager.this.mHolder == holder) {
                FilmEmulatorManager.this.mFilmEngine.surfaceChanged(holder, format, width, height);
            }
            if (FilmEmulatorManager.this.mFilmEngine != null) {
                FilmEmulatorManager.this.mFilmEngine.changePictureSize(FilmEmulatorManager.this.mPreviewAspect);
            }
            CamLog.m3d(CameraConstants.TAG, "[Film] Preview aspect ratio is : " + FilmEmulatorManager.this.mPreviewAspect);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            CamLog.m3d(CameraConstants.TAG, "[Film] surfaceDestroyed");
            if (FilmEmulatorManager.this.mFilmEngine != null) {
                FilmEmulatorManager.this.mFilmEngine.release();
                FilmEmulatorManager.this.mFilmEngine = null;
            }
        }
    }

    /* renamed from: com.lge.camera.managers.FilmEmulatorManager$4 */
    class C08114 implements OnFilmEmulationViewListener {
        C08114() {
        }

        public void onDraw(FilmEmulatorRendererPreview render) {
            int x;
            int surfaceWidth = render.mSurfaceViewWidth;
            int surfaceHeight = render.mSurfaceViewHeight;
            int mainPreviewWidth = FilmEmulatorManager.this.mPreviewScreenSize[1];
            int mainPreviewHeight = FilmEmulatorManager.this.mPreviewScreenSize[0];
            if (FilmEmulatorManager.this.mDrawPreviewBackground) {
                GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
                render.drawPreviewBackground(1.0f);
            }
            if (FilmEmulatorManager.this.mPreviewAspect > 2.3f) {
                x = surfaceWidth - mainPreviewWidth;
            } else {
                x = (int) (((float) (surfaceWidth - mainPreviewWidth)) / 2.0f);
            }
            GLES20.glViewport(x, (surfaceHeight - FilmEmulatorManager.this.mPreviewStartMargin) - mainPreviewHeight, mainPreviewWidth, mainPreviewHeight);
            int curLutNumber = FilmEmulatorManager.this.mCurLutNumber;
            if (CameraConstants.MODE_SMART_CAM.equals(FilmEmulatorManager.this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(FilmEmulatorManager.this.mGet.getShotMode())) {
                curLutNumber = FilmEmulatorManager.this.mCurSceneIndex;
            } else {
                FilmEmulatorManager.this.mCurSceneIndex = 0;
            }
            render.drawFrame(curLutNumber, false, 1.0f, FilmEmulatorManager.this.mFilmStrength, 1.0f, 1.0f, false, 0.0f);
            render.showFilmMenu(FilmEmulatorManager.this.mShowFilmMenu);
            if (FilmEmulatorManager.this.mShowFilmMenu) {
                int menuBackgroundStartX;
                int menuBackgroundStartY;
                int i;
                FilterMenuItem item;
                int xPos;
                int yPos;
                FilmEmulatorManager.this.calculateScrollPosition();
                int degree = 0;
                if ((FilmEmulatorManager.this.mFilmAnimationState & 256) != 0) {
                    FilmEmulatorManager.this.doFilmMenuAnimation();
                    degree = FilmEmulatorManager.this.getOrientationDegree();
                } else if ((FilmEmulatorManager.this.mFilmAnimationState & 128) != 0) {
                    FilmEmulatorManager.this.doFilmMenuAnimationWithAlpha();
                } else {
                    FilmEmulatorManager.this.mTranslateAnimationValue = 0.0f;
                    FilmEmulatorManager.this.mAlphaAnimationValue = 1.0f;
                    FilmEmulatorManager.this.mBackgroundCoverAlpahValue = 0.7f;
                    FilmEmulatorManager.this.mFilmAnimationState = 0;
                }
                boolean isMenu90Degree = false;
                if (degree == 90) {
                    isMenu90Degree = true;
                    GLES20.glViewport(0, 0, surfaceWidth, FilmEmulatorManager.this.mFilmMenuHeight + FilmEmulatorManager.this.mFilmMenuBottomMargin);
                    render.enableStencilBuffer();
                    render.drawPlaneAndDisableStencilBuffer();
                }
                int filmMenuHeight = FilmEmulatorManager.this.mFilmMenuBgHeight;
                int transAnimationValue = (int) FilmEmulatorManager.this.mTranslateAnimationValue;
                if (degree == 0) {
                    menuBackgroundStartX = FilmEmulatorManager.this.mFilmMenuBgStartMargin + transAnimationValue;
                    menuBackgroundStartY = FilmEmulatorManager.this.mFilmMenuBgBottomMargin;
                } else if (degree == 180) {
                    menuBackgroundStartX = transAnimationValue;
                    menuBackgroundStartY = 0;
                } else if (degree == 90) {
                    menuBackgroundStartX = 0;
                    menuBackgroundStartY = transAnimationValue;
                } else {
                    menuBackgroundStartX = 0;
                    menuBackgroundStartY = transAnimationValue;
                }
                int bgWidth = surfaceWidth;
                boolean isSmartCamMode = CameraConstants.MODE_SMART_CAM.equals(FilmEmulatorManager.this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(FilmEmulatorManager.this.mGet.getShotMode());
                if (isSmartCamMode) {
                    bgWidth = (FilmEmulatorManager.this.mFilmThumbItemWidth * 4) + (FilmEmulatorManager.this.mFilmThumbItemVerticalGap * 5);
                }
                GLES20.glViewport(menuBackgroundStartX, menuBackgroundStartY, bgWidth, filmMenuHeight);
                render.drawBackground(FilmEmulatorManager.this.mBackgroundCoverAlpahValue);
                if (!isMenu90Degree) {
                    filmMenuHeight = FilmEmulatorManager.this.mFilmMenuHeight;
                    if (degree == 0) {
                        menuBackgroundStartX = transAnimationValue;
                        menuBackgroundStartY = FilmEmulatorManager.this.mFilmMenuBottomMargin;
                    } else if (degree == 180) {
                        menuBackgroundStartX = transAnimationValue;
                        menuBackgroundStartY = (FilmEmulatorManager.this.mLcdSize[0] - FilmEmulatorManager.this.mFilmMenuHeight) - FilmEmulatorManager.this.mFilmMenuTopMargin;
                    } else if (degree == 90) {
                        menuBackgroundStartX = 0;
                        menuBackgroundStartY = FilmEmulatorManager.this.mFilmMenuBottomMargin + transAnimationValue;
                    } else {
                        menuBackgroundStartX = 0;
                        menuBackgroundStartY = FilmEmulatorManager.this.mFilmMenuBottomMargin + transAnimationValue;
                    }
                    GLES20.glViewport(menuBackgroundStartX, menuBackgroundStartY, surfaceWidth, filmMenuHeight);
                    render.enableStencilBuffer();
                    render.drawPlaneAndDisableStencilBuffer();
                }
                degree = FilmEmulatorManager.this.getOrientationDegree();
                int dX = FilmEmulatorManager.this.mDx;
                int dY = FilmEmulatorManager.this.mDy;
                int scrollDiff = FilmEmulatorManager.this.mDy;
                int scrollPosition = FilmEmulatorManager.this.mScrollPosition;
                if (FilmEmulatorManager.this.mIsLongPressed) {
                    scrollDiff = 0;
                } else {
                    scrollDiff = FilmEmulatorManager.this.mDx;
                }
                if (FilmEmulatorManager.this.mIsDeleteFilm && FilmEmulatorManager.this.mDeletedIndex != -1) {
                    FilmEmulatorManager.this.mFilmEngine.removeDownloadedLut(((FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(FilmEmulatorManager.this.mDeletedIndex)).mLUTNumber);
                    FilmEmulatorManager.this.mFilterMenuItemList.remove(FilmEmulatorManager.this.mDeletedIndex);
                    FilmEmulatorManager.this.mDeletedIndex = -1;
                }
                long time = 0;
                if (FilmEmulatorManager.this.mAnimationStartTime != 0) {
                    time = SystemClock.uptimeMillis() - FilmEmulatorManager.this.mAnimationStartTime;
                }
                boolean readyToDeleteDone = false;
                if (((float) time) >= 150.0f) {
                    FilmEmulatorManager.this.mIsPositionChanged = false;
                    FilmEmulatorManager.this.mAnimationStartTime = 0;
                    if (FilmEmulatorManager.this.mLongPressedIndex != -1) {
                        FilterMenuItem longPressItem = (FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(FilmEmulatorManager.this.mLongPressedIndex);
                        FilmEmulatorManager.this.mFilterMenuItemList.remove(FilmEmulatorManager.this.mLongPressedIndex);
                        FilmEmulatorManager.this.mFilterMenuItemList.add(FilmEmulatorManager.this.mSelectedFilmMenuIndex, longPressItem);
                        for (i = 0; i < FilmEmulatorManager.this.mFilterMenuItemList.size(); i++) {
                            ((FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(i)).setPosition(i);
                        }
                        FilmEmulatorManager.this.mLongPressedIndex = FilmEmulatorManager.this.mSelectedFilmMenuIndex;
                    } else if (FilmEmulatorManager.this.mIsDeleteFilm) {
                        for (i = 0; i < FilmEmulatorManager.this.mFilterMenuItemList.size(); i++) {
                            ((FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(i)).setPosition(((FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(i)).mNextPosition);
                        }
                        FilmEmulatorManager.this.mIsDeleteFilm = false;
                        readyToDeleteDone = true;
                    } else {
                        for (i = 0; i < FilmEmulatorManager.this.mFilterMenuItemList.size(); i++) {
                            ((FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(i)).setPosition(i);
                        }
                    }
                    FilmEmulatorManager.this.mIsFilterItemChanging = false;
                }
                if (FilmEmulatorManager.this.mIsPositionChanged && FilmEmulatorManager.this.mAnimationStartTime == 0) {
                    FilmEmulatorManager.this.mAnimationStartTime = SystemClock.uptimeMillis();
                    FilmEmulatorManager.this.mIsFilterItemChanging = true;
                }
                int[] iArr = new int[2];
                iArr = new int[]{0, 0};
                i = FilmEmulatorManager.this.mFilterMenuItemList.size() - 1;
                while (i >= 0) {
                    item = (FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(i);
                    if ((!FilmEmulatorManager.this.mIsLongPressed && !FilmEmulatorManager.this.mIsPositionChanged) || FilmEmulatorManager.this.mLongPressedIndex != i) {
                        if (FilmEmulatorManager.this.mIsPositionChanged && i < FilmEmulatorManager.this.mFilterMenuItemList.size()) {
                            iArr = item.movePosition(item.mNextPosition, 150.0f, time, degree);
                        }
                        if (degree == 0 || degree == 180) {
                            xPos = ((((item.f29mX - iArr[0]) - scrollDiff) - scrollPosition) - FilmEmulatorManager.this.mLpScrollPosition) + transAnimationValue;
                            yPos = item.f30mY - iArr[1];
                        } else {
                            xPos = (((item.f29mX - iArr[0]) - scrollDiff) - scrollPosition) - FilmEmulatorManager.this.mLpScrollPosition;
                            yPos = (item.f30mY - iArr[1]) + transAnimationValue;
                        }
                        GLES20.glViewport(xPos, yPos, FilmEmulatorManager.this.mFilmThumbItemWidth, FilmEmulatorManager.this.mFilmThumbItemHeight);
                        render.drawFrame(item.mLUTNumber, true, FilmEmulatorManager.this.mAlphaAnimationValue, 1.0f, FilmEmulatorManager.this.mPreviewAspect, 1.0f, false, 1.0f);
                        if (FilmEmulatorManager.this.mIsEditMode && item.mIsDownlodaded) {
                            boolean z;
                            FilmEmulatorManager.this.setDeleteButtonPosition(degree, xPos, yPos, FilmEmulatorManager.this.mFilmThumbItemWidth, FilmEmulatorManager.this.mFilmThumbItemHeight);
                            if (FilmEmulatorManager.this.mPressedLutNumber == item.mLUTNumber && FilmEmulatorManager.this.mIsDeleteButtonTouched) {
                                z = true;
                            } else {
                                z = false;
                            }
                            render.drawDeleteButton(1.0f, z);
                        }
                    }
                    i--;
                }
                if (FilmEmulatorManager.this.mIsLongPressed && FilmEmulatorManager.this.mLongPressedIndex != -1) {
                    item = (FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(FilmEmulatorManager.this.mLongPressedIndex);
                    xPos = (((FilmEmulatorManager.this.mLongPresseStartX - dX) - scrollDiff) - scrollPosition) - 25;
                    yPos = (FilmEmulatorManager.this.mLongPresseStartY + dY) - 25;
                    GLES20.glViewport(xPos, yPos, FilmEmulatorManager.this.mFilmThumbItemWidth + 50, FilmEmulatorManager.this.mFilmThumbItemHeight + 50);
                    render.drawFrame(item.mLUTNumber, true, 1.0f, 1.0f, FilmEmulatorManager.this.mPreviewAspect, 1.0f, false, 1.0f);
                    if (FilmEmulatorManager.this.mIsEditMode && item.mIsDownlodaded) {
                        FilmEmulatorManager.this.setDeleteButtonPosition(degree, xPos, yPos, FilmEmulatorManager.this.mFilmThumbItemWidth + 50, FilmEmulatorManager.this.mFilmThumbItemHeight + 50);
                        render.drawDeleteButton(1.0f, FilmEmulatorManager.this.mIsDeleteButtonTouched);
                    }
                }
                render.disableStencilBuffer();
                if (readyToDeleteDone) {
                    for (i = 0; i < FilmEmulatorManager.this.mFilterMenuItemList.size(); i++) {
                        ((FilterMenuItem) FilmEmulatorManager.this.mFilterMenuItemList.get(i)).setPosition(i);
                    }
                    FilmEmulatorManager.this.checkFilterMenuBoundary(dX, dY, 0);
                }
            }
        }

        public void onEngineInitializeDone(SurfaceTexture texture, SurfaceTexture texture2) {
            CamLog.m3d(CameraConstants.TAG, "[Film] onEngineInitializeDone");
            FilmEmulatorManager.this.mFilmSurfaceTexture = texture;
            FilmEmulatorManager.this.mFilmSurfaceTexture2 = texture2;
            FilmEmulatorManager.this.mCurrentSurfaceTextureNumber = 1;
            if (FilmEmulatorManager.this.mListener != null) {
                FilmEmulatorManager.this.mListener.onEngineInitializeDone(texture);
            }
        }

        public void onSendStillImage(Bitmap output) {
            CamLog.m3d(CameraConstants.TAG, "[Film] onSendStillImage");
        }

        public void onErrorOccured(int errorCode) {
            CamLog.m3d(CameraConstants.TAG, "[Film] onErrorOccured : " + errorCode);
        }
    }

    public class FilterMenuAnimationInfo {
        public float mAnimationEndPos;
        public float mAnimationMoveTotalDistance;
        public float mAnimationStartPos;
        public int mDegree;
    }

    public FilmEmulatorManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        this.mLcdSize = Utils.getLCDsize(getAppContext(), true);
        this.mReverseFlag = 0;
        this.mFilmGestureDetector = new GestureDetector(this.mGet.getAppContext(), new FilterMenuGestureDetector());
        this.mIsAfterRecording = false;
        this.mIsReadyToOpenFilterMenu = false;
    }

    public void onResumeAfter() {
        setupView();
        super.onResumeAfter();
        SharedPreferenceUtilBase.saveLastSelectFilter(getAppContext(), CameraConstants.FILM_NONE);
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mFilterMenuItemList != null) {
            StringBuilder filterMenuList = new StringBuilder();
            for (int i = 0; i < this.mFilterMenuItemList.size(); i++) {
                FilterMenuItem item = (FilterMenuItem) this.mFilterMenuItemList.get(i);
                if (!FoodModule.FILM_FOOD.equals(item.mEntryValue)) {
                    filterMenuList.append(item.mEntryValue + ",");
                }
            }
            if (!(CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode()))) {
                if (this.mGet.isPaused()) {
                    SharedPreferenceUtilBase.saveLastSelectFilter(getAppContext(), this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR));
                }
                SharedPreferenceUtilBase.saveFilterMenuListOrder(getAppContext(), filterMenuList.toString());
            }
            if (FunctionProperties.isSupportedFilmEmulator()) {
                doShowAndHideFilmMenu(false, 0, false, true);
                this.mShowFilmMenu = false;
                this.mIsEditMode = false;
                this.mScrollPosition = 0;
                this.mLpScrollPosition = 0;
            }
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (this.mFilmNamePathArrayList != null) {
            this.mFilmNamePathArrayList.clear();
            this.mFilmNamePathArrayList = null;
        }
        if (this.mFilmIndexHashMap != null) {
            this.mFilmIndexHashMap.clear();
            this.mFilmIndexHashMap = null;
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        CamLog.m3d(CameraConstants.TAG, "[Film] onConfigurationChanged");
        setupView();
    }

    public SurfaceTexture getFilmSurfaceTexture() {
        if (this.mCurrentSurfaceTextureNumber == 1) {
            return this.mFilmSurfaceTexture;
        }
        return this.mFilmSurfaceTexture2;
    }

    public SurfaceTexture getSurfaceTextureForInAndOut() {
        if (this.mCurrentSurfaceTextureNumber == 1) {
            this.mCurrentSurfaceTextureNumber = 2;
            return this.mFilmSurfaceTexture2;
        }
        this.mCurrentSurfaceTextureNumber = 1;
        return this.mFilmSurfaceTexture;
    }

    public void movePreviewOutOfWindow(final boolean moveOut, int delay) {
        if (!ModelProperties.isMTKChipset() || delay == 0) {
            this.mGet.movePreviewOutOfWindow(moveOut);
        } else {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    FilmEmulatorManager.this.mGet.movePreviewOutOfWindow(moveOut);
                }
            }, (long) delay);
        }
    }

    public void runFilmEmulator(String filmName) {
        if (!FunctionProperties.isSupportedFilmEmulator() || this.mFilmIndexHashMap == null || this.mFilmIndexHashMap.size() == 0) {
            CamLog.m3d(CameraConstants.TAG, "[Film] film is not supported or mFilmIndexHashMap is null");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[Film] runFilmEmulator");
        if (this.mFilmSurfaceView == null) {
            this.mFilmSurfaceView = (SurfaceView) this.mGet.findViewById(C0088R.id.preview_surface_popout);
            if (this.mFilmSurfaceView == null) {
                this.mFilmState = 0;
                CamLog.m3d(CameraConstants.TAG, "[Film] film surfaceview is null");
                return;
            }
            moveFilmPreviewOutOfWindow(true);
            boolean lightFrameOn = this.mGet.isLightFrameOn();
            if (lightFrameOn) {
                setFilmPreviewAlpha(true);
            }
            this.mHolder = this.mFilmSurfaceView.getHolder();
            this.mHolder.addCallback(this.mCallback);
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            this.mFilmSurfaceView.setLayoutParams(new LayoutParams(lcdSize[1], lcdSize[0]));
            setFilmState(3);
            this.mFilmSurfaceView.setVisibility(0);
            this.mIsTurnOnLightFrame = lightFrameOn;
            this.mShowFilmMenu = false;
            if (this.mFilmIndexHashMap.get(filmName) == null) {
                filmName = CameraConstants.FILM_NONE;
            }
            this.mCurLutNumber = ((Integer) this.mFilmIndexHashMap.get(filmName)).intValue();
            this.mCurFilmValue = filmName;
        }
    }

    public void release() {
        CamLog.m3d(CameraConstants.TAG, "[Film] Release");
        if (this.mFilmSurfaceView != null) {
            this.mFilmSurfaceView.setVisibility(8);
            this.mFilmSurfaceView = null;
        }
        if (this.mHolder != null) {
            this.mHolder.removeCallback(this.mCallback);
            this.mHolder = null;
        }
        this.mFilmSurfaceTexture = null;
        this.mFilmSurfaceTexture2 = null;
        this.mCurrentSurfaceTextureNumber = 1;
        if (this.mListener != null) {
            this.mListener.onEnginReleased(false, false);
        }
        setFilmState(0);
    }

    public void stopFilmEmulator(boolean isRestartPreview, boolean isStopByRecording) {
        if (isRunningFilmEmulator()) {
            CamLog.m3d(CameraConstants.TAG, "[Film] stopFilmEmulator");
            setFilmState(2);
            if (this.mListener != null) {
                this.mListener.onEnginReleased(isRestartPreview, isStopByRecording);
            }
            setFilmState(1);
        }
    }

    public void filmEmulatorStopDone() {
        int delay = 0;
        if (ModelProperties.isMTKChipset()) {
            delay = 150;
            if (!this.mGet.checkModuleValidate(192)) {
                delay = 250;
            }
        }
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            delay = 150;
        }
        if (delay == 0) {
            doFilmEmulatorStopDone();
        } else {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    FilmEmulatorManager.this.doFilmEmulatorStopDone();
                }
            }, (long) delay);
        }
    }

    private void doFilmEmulatorStopDone() {
        if (this.mFilmSurfaceView != null) {
            this.mFilmSurfaceView.setVisibility(8);
            this.mFilmSurfaceView = null;
        }
        if (this.mHolder != null) {
            this.mHolder.removeCallback(this.mCallback);
            this.mHolder = null;
        }
        this.mFilmSurfaceTexture = null;
        this.mFilmSurfaceTexture2 = null;
        this.mCurrentSurfaceTextureNumber = 1;
        setFilmState(0);
    }

    private void setDeleteButtonPosition(int degree, int xPos, int yPos, int width, int height) {
        int[] deletePos = new int[2];
        if (degree == 0) {
            deletePos[0] = (width - this.mDeleteBtnSize) + xPos;
            deletePos[1] = (width - this.mDeleteBtnSize) + yPos;
        } else if (degree == 90) {
            deletePos[0] = xPos;
            deletePos[1] = (width - this.mDeleteBtnSize) + yPos;
        } else if (degree == 180) {
            deletePos[0] = xPos;
            deletePos[1] = yPos;
        } else {
            deletePos[0] = (width - this.mDeleteBtnSize) + xPos;
            deletePos[1] = yPos;
        }
        GLES20.glViewport(deletePos[0], deletePos[1], this.mDeleteBtnSize, this.mDeleteBtnSize);
    }

    protected void setTranslateAnimationStartValue(boolean show, int degree) {
        this.mFilterMenuAniInfo.mDegree = degree;
        if (show) {
            switch (degree) {
                case 0:
                    this.mFilterMenuAniInfo.mAnimationStartPos = (float) this.mLcdSize[1];
                    this.mFilterMenuAniInfo.mAnimationEndPos = 0.0f;
                    this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) (-this.mLcdSize[1]);
                    return;
                case 90:
                    this.mFilterMenuAniInfo.mAnimationStartPos = (float) ((this.mLcdSize[0] / 2) - this.mFilmMenuHeight);
                    this.mFilterMenuAniInfo.mAnimationEndPos = 0.0f;
                    this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) (-((this.mLcdSize[0] / 2) - this.mFilmMenuHeight));
                    return;
                case 180:
                    this.mFilterMenuAniInfo.mAnimationStartPos = (float) (-this.mLcdSize[1]);
                    this.mFilterMenuAniInfo.mAnimationEndPos = 0.0f;
                    this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) this.mLcdSize[1];
                    return;
                default:
                    this.mFilterMenuAniInfo.mAnimationStartPos = (float) (-(this.mFilmMenuHeight + this.mFilmMenuBottomMargin));
                    this.mFilterMenuAniInfo.mAnimationEndPos = 0.0f;
                    this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) (this.mFilmMenuHeight + this.mFilmMenuBottomMargin);
                    return;
            }
        }
        switch (degree) {
            case 0:
                this.mFilterMenuAniInfo.mAnimationStartPos = 0.0f;
                this.mFilterMenuAniInfo.mAnimationEndPos = (float) this.mLcdSize[1];
                this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) (-this.mLcdSize[1]);
                return;
            case 90:
                this.mFilterMenuAniInfo.mAnimationStartPos = 0.0f;
                this.mFilterMenuAniInfo.mAnimationEndPos = (float) ((this.mLcdSize[0] / 2) - this.mFilmMenuHeight);
                this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) (-((this.mLcdSize[0] / 2) - this.mFilmMenuHeight));
                return;
            case 180:
                this.mFilterMenuAniInfo.mAnimationStartPos = 0.0f;
                this.mFilterMenuAniInfo.mAnimationEndPos = (float) (-this.mLcdSize[1]);
                this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) this.mLcdSize[1];
                return;
            default:
                this.mFilterMenuAniInfo.mAnimationStartPos = 0.0f;
                this.mFilterMenuAniInfo.mAnimationEndPos = (float) (-(this.mFilmMenuHeight + this.mFilmMenuBottomMargin));
                this.mFilterMenuAniInfo.mAnimationMoveTotalDistance = (float) (this.mFilmMenuHeight + this.mFilmMenuBottomMargin);
                return;
        }
    }

    protected void doFilmMenuAnimationWithAlpha() {
        this.mTranslateAnimationValue = 0.0f;
        if (this.mFilmAnimationState == 0) {
            this.mAlphaAnimationValue = 1.0f;
            this.mBackgroundCoverAlpahValue = 0.7f;
        } else if ((this.mFilmAnimationState & 1) != 0) {
            this.mFilmAnimationState &= -2;
            this.mFilmAnimationState |= 4;
            this.mTransAnimStartTime = SystemClock.uptimeMillis();
            this.mAlphaAnimationValue = 0.0f;
            this.mBackgroundCoverAlpahValue = 0.0f;
        } else if ((this.mFilmAnimationState & 2) != 0) {
            this.mFilmAnimationState &= -3;
            this.mFilmAnimationState |= 8;
            this.mTransAnimStartTime = SystemClock.uptimeMillis();
            this.mAlphaAnimationValue = 1.0f;
            this.mBackgroundCoverAlpahValue = 0.7f;
        }
        if (this.mFilmAnimationState == 0) {
            return;
        }
        boolean isAnimationEnd;
        long time;
        float interpolatedPosition;
        if ((this.mFilmAnimationState & 4) != 0) {
            isAnimationEnd = false;
            if (this.mAlphaAnimationValue >= 1.0f) {
                isAnimationEnd = true;
            }
            if (isAnimationEnd) {
                this.mAlphaAnimationValue = 1.0f;
                this.mBackgroundCoverAlpahValue = 0.7f;
                this.mTransAnimStartTime = 0;
                onShowFilterMenu();
                this.mFilmAnimationState = 0;
                return;
            }
            time = SystemClock.uptimeMillis() - this.mTransAnimStartTime;
            if (time != 0) {
                interpolatedPosition = this.mInterpolator.getInterpolation(Math.min(((float) time) / this.mFilterMenuAnimationDuration, 1.0f));
                this.mAnimationDiff = 1.0f * interpolatedPosition;
                this.mAlphaAnimationValue = this.mAnimationDiff;
                this.mBackgroundCoverAlpahValue = 0.7f * interpolatedPosition;
            }
        } else if ((this.mFilmAnimationState & 8) != 0) {
            isAnimationEnd = false;
            if (this.mAlphaAnimationValue <= 0.0f) {
                isAnimationEnd = true;
            }
            if (isAnimationEnd) {
                this.mAlphaAnimationValue = 0.0f;
                this.mBackgroundCoverAlpahValue = 0.0f;
                this.mTransAnimStartTime = 0;
                onHideFilterMenu();
                this.mFilmAnimationState = 0;
                return;
            }
            time = SystemClock.uptimeMillis() - this.mTransAnimStartTime;
            if (time != 0) {
                interpolatedPosition = this.mInterpolator.getInterpolation(Math.min(((float) time) / this.mFilterMenuAnimationDuration, 1.0f));
                this.mAnimationDiff = 1.0f * interpolatedPosition;
                this.mAlphaAnimationValue = 1.0f - this.mAnimationDiff;
                this.mBackgroundCoverAlpahValue = 0.7f - (0.7f * interpolatedPosition);
            }
        }
    }

    protected void doFilmMenuAnimation() {
        this.mAlphaAnimationValue = 1.0f;
        this.mBackgroundCoverAlpahValue = 0.7f;
        int degree = getOrientationDegree();
        if (this.mFilmAnimationState == 0) {
            this.mTranslateAnimationValue = 0.0f;
        } else if ((this.mFilmAnimationState & 1) != 0) {
            setTranslateAnimationStartValue(true, degree);
            this.mFilmAnimationState &= -2;
            this.mFilmAnimationState |= 4;
            this.mTransAnimStartTime = SystemClock.uptimeMillis();
            this.mTranslateAnimationValue = this.mFilterMenuAniInfo.mAnimationStartPos;
        } else if ((this.mFilmAnimationState & 2) != 0) {
            setTranslateAnimationStartValue(false, degree);
            this.mFilmAnimationState &= -3;
            this.mFilmAnimationState |= 8;
            this.mTransAnimStartTime = SystemClock.uptimeMillis();
            this.mTranslateAnimationValue = this.mFilterMenuAniInfo.mAnimationStartPos;
        }
        if (this.mFilmAnimationState == 0) {
            return;
        }
        if ((this.mFilmAnimationState & 4) != 0) {
            filmMenuShowAnimation();
        } else if ((this.mFilmAnimationState & 8) != 0) {
            filmMenuHideAnimation();
        }
    }

    private void filmMenuHideAnimation() {
        boolean isAnimationEnd = false;
        if ((this.mFilterMenuAniInfo.mDegree == 0 || this.mFilterMenuAniInfo.mDegree == 90) && this.mTranslateAnimationValue >= this.mFilterMenuAniInfo.mAnimationEndPos) {
            isAnimationEnd = true;
        } else if ((this.mFilterMenuAniInfo.mDegree == 180 || this.mFilterMenuAniInfo.mDegree == 270) && this.mTranslateAnimationValue <= this.mFilterMenuAniInfo.mAnimationEndPos) {
            isAnimationEnd = true;
        }
        if (isAnimationEnd) {
            this.mTranslateAnimationValue = this.mFilterMenuAniInfo.mAnimationEndPos;
            this.mTransAnimStartTime = 0;
            onHideFilterMenu();
            this.mFilmAnimationState = 0;
            return;
        }
        long time = SystemClock.uptimeMillis() - this.mTransAnimStartTime;
        if (time != 0) {
            this.mAnimationDiff = this.mFilterMenuAniInfo.mAnimationMoveTotalDistance * this.mInterpolator.getInterpolation(Math.min(((float) time) / this.mFilterMenuAnimationDuration, 1.0f));
            this.mTranslateAnimationValue = this.mFilterMenuAniInfo.mAnimationStartPos - this.mAnimationDiff;
        }
    }

    private void filmMenuShowAnimation() {
        boolean isAnimationEnd = false;
        if ((this.mFilterMenuAniInfo.mDegree == 0 || this.mFilterMenuAniInfo.mDegree == 90) && this.mTranslateAnimationValue <= this.mFilterMenuAniInfo.mAnimationEndPos) {
            isAnimationEnd = true;
        } else if ((this.mFilterMenuAniInfo.mDegree == 180 || this.mFilterMenuAniInfo.mDegree == 270) && this.mTranslateAnimationValue >= this.mFilterMenuAniInfo.mAnimationEndPos) {
            isAnimationEnd = true;
        }
        if (isAnimationEnd) {
            this.mTranslateAnimationValue = this.mFilterMenuAniInfo.mAnimationEndPos;
            this.mTransAnimStartTime = 0;
            onShowFilterMenu();
            this.mFilmAnimationState = 0;
            return;
        }
        long time = SystemClock.uptimeMillis() - this.mTransAnimStartTime;
        if (time != 0) {
            this.mAnimationDiff = this.mFilterMenuAniInfo.mAnimationMoveTotalDistance * this.mInterpolator.getInterpolation(Math.min(((float) time) / this.mFilterMenuAnimationDuration, 1.0f));
            this.mTranslateAnimationValue = this.mFilterMenuAniInfo.mAnimationStartPos + this.mAnimationDiff;
        }
    }

    public void prepareRecording(String videoSize, boolean changeReverseFlag) {
        if (isRunningFilmEmulator() && !this.mGet.isPaused()) {
            boolean isSmartcamMode;
            int i;
            int[] size = Utils.sizeStringToArray(videoSize);
            Surface s = VideoRecorder.getSurface(true, 0);
            boolean isCinema = false;
            if (((float) size[0]) / ((float) size[1]) > 2.3f) {
                isCinema = true;
            }
            if (!this.mGet.isRearCamera() && changeReverseFlag) {
                CamLog.m3d(CameraConstants.TAG, "[Film] get orientation : " + this.mGet.getOrientationDegree());
                int degree = this.mGet.getOrientationDegree();
                if (degree == 0 || degree == 180) {
                    this.mReverseFlag = 2;
                } else {
                    this.mReverseFlag = 1;
                }
                if (CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                    this.mReverseFlag = 1;
                }
            }
            CamLog.m3d(CameraConstants.TAG, "[Film] prepareRecording. - " + videoSize + ", isCinema : " + isCinema + ", isReverse : " + this.mReverseFlag);
            if (CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
                isSmartcamMode = true;
            } else {
                isSmartcamMode = false;
            }
            FilmEmulatorEngine filmEmulatorEngine = this.mFilmEngine;
            int i2 = size[0];
            int i3 = size[1];
            if (isSmartcamMode) {
                i = this.mCurSceneIndex;
            } else {
                i = this.mCurLutNumber;
            }
            filmEmulatorEngine.prepareRecording(s, i2, i3, i, isCinema, this.mReverseFlag);
        }
    }

    public void startRecorder() {
        if (isRunningFilmEmulator()) {
            CamLog.m3d(CameraConstants.TAG, "[Film] startRecording.");
            this.mFilmEngine.startRecoding();
        }
    }

    public void stopRecorder(boolean isDirect) {
        if (isRunningFilmEmulator()) {
            CamLog.m3d(CameraConstants.TAG, "[Film] stopRecorder. isDirect : " + isDirect);
            this.mFilmEngine.stopRecoding(isDirect);
        }
    }

    public void showFilmLimitPopupForNormalMode() {
        if (!CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR)) && !this.mGet.isManualMode() && !this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            if (this.mGet.isUHDmode() || this.mGet.isFHD60()) {
                ListPreference listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
                if (listPref != null) {
                    String videoSizeEntry = listPref.getEntry();
                    this.mGet.showToast(String.format(getActivity().getString(C0088R.string.filter_limitation_desc), new Object[]{videoSizeEntry}), CameraConstants.TOAST_LENGTH_SHORT);
                }
            }
        }
    }

    public boolean hasFilmLimitationForManualVideo() {
        if (this.mGet.isUHDmode() || "on".equals(this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) || "on".equals(this.mGet.getSettingValue(Setting.KEY_HDR10))) {
            return true;
        }
        String value = this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        int fpsValue = 30;
        if (!"not found".equals(value)) {
            fpsValue = Integer.parseInt(value);
        }
        if (fpsValue <= 30) {
            return false;
        }
        return true;
    }

    public void checkAndStopFilmEngine() {
        if (isRunningFilmEmulator() && this.mCurLutNumber == 0 && !CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode())) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    FilmEmulatorManager.this.stopFilmEmulator(true, false);
                }
            }, 300);
        }
    }

    public void changeCamera() {
        if (FunctionProperties.isSupportedFilmEmulator() && this.mFilmEngine != null) {
            CamLog.m3d(CameraConstants.TAG, "[Film] change camera");
            this.mFilmEngine.changeCamera();
        }
    }

    public void moveFilmPreviewOutOfWindow(boolean outOfWindow) {
        int distance = 0;
        if (outOfWindow) {
            distance = Utils.getDefaultDisplayHeight(getActivity()) + 300;
        }
        if (this.mFilmSurfaceView != null) {
            CamLog.m3d(CameraConstants.TAG, "[FIlm] moveFilmPreviewOutOfWindow : " + outOfWindow + ", current position : " + this.mFilmSurfaceView.getTranslationY());
            this.mFilmSurfaceView.setTranslationY((float) distance);
        }
    }

    public boolean checkBlockingButtonState() {
        return this.mFilmState > 0 && this.mFilmState < 4;
    }

    public void turnOnOffBackground(boolean turnOn) {
        this.mDrawPreviewBackground = turnOn;
    }

    public void changeBackground(Drawable drawable) {
        if (this.mFilmEngine != null) {
            this.mFilmEngine.changeBackground(drawable);
        }
    }

    public void setAfterRecording(boolean isAfterRecording) {
        this.mIsAfterRecording = isAfterRecording;
    }

    public boolean isAfterRecording() {
        return this.mIsAfterRecording;
    }

    public void removePreviewCoverWithDelay(int delay, final boolean useAnim) {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                boolean isMovePreview = true;
                if (FilmEmulatorManager.this.isRunningFilmEmulator()) {
                    isMovePreview = false;
                }
                FilmEmulatorManager.this.mGet.setPreviewCoverVisibility(8, useAnim, null, isMovePreview, false);
            }
        }, (long) delay);
    }
}
