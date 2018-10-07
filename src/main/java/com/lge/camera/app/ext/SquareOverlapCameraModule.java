package com.lge.camera.app.ext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.support.p000v4.view.PointerIconCompat;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.database.OverlapProjectDb;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.SquareSnapGalleryItem;
import com.lge.camera.managers.ext.OverlapPreviewManagerBase;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SquareUtil;
import com.lge.camera.util.SystemBarUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SquareOverlapCameraModule extends SquareOverlapCameraModuleBase {
    private static final int ALREADY_CNT = 1;
    private static final int GET_FILE_FOR_COPY = 1;
    private static final int GET_FILE_FOR_CROP = 0;
    HashMap<String, Integer> mHashMap = new HashMap();
    private Uri mImportedUri;
    private boolean mIsCreatingSampleFromCamera = false;
    private boolean mIsFirstResumed = false;
    private boolean mIsOverlapNeedToReloadList = false;
    private boolean mIsStartedGallery = false;
    private boolean mIsToSnapAfterAddingSample = false;
    private ArrayList<Uri> mUriList = new ArrayList();

    public SquareOverlapCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        this.mIniGuideManager.setInitGuideListener(this);
        if (!SharedPreferenceUtilBase.getOverlapShotFirstGuide(this.mGet.getAppContext())) {
            this.mIniGuideManager.initLayout();
        }
        this.mCaptureMode = 0;
        this.mIsFirstResumed = false;
        this.mIsCreatingSampleFromCamera = false;
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mOverlapProjectManager);
        this.mManagerList.add(this.mOverlapPreviewManager);
        this.mManagerList.add(this.mIniGuideManager);
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        OverlapProjectDb currProject = this.mOverlapProjectManager.openDatabase();
        this.mOverlapPreviewManager.setProjectView(currProject, this.mOverlapProjectManager.getProjectAdapter());
        this.mGalleryManager.showGalleryView(false);
        this.mOverlapProjectManager.setVisible(true);
        this.mOverlapPreviewManager.setVisible(true);
        this.mIsFirstCaptured = false;
        this.mCurrentState = 0;
        if (currProject == null) {
            changeCaptureMode(1);
        }
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        this.mIsStartedGallery = false;
        if (this.mIsToSnapAfterAddingSample) {
            onProjectSelected(this.mOverlapProjectManager.getProjectCnt() - 1, false, false);
        }
    }

    public void onResumeAfter() {
        this.mIsOverlapNeedToReloadList = this.mIsNeedToReloadList;
        super.onResumeAfter();
        this.mCaptureButtonManager.changeButtonByMode(12);
        if (this.mIsToSnapAfterAddingSample) {
            this.mOverlapProjectManager.setGridViewMoveToPosition(this.mOverlapProjectManager.getProjectCnt() - 1, false);
        }
        if (this.mCurrentState == 0 && !this.mIsToSnapAfterAddingSample && this.mIsFirstResumed) {
            this.mOverlapProjectManager.addSlideShow();
        }
        this.mIsToSnapAfterAddingSample = false;
        this.mIsFirstResumed = true;
    }

    public void onPauseBefore() {
        boolean z = false;
        super.onPauseBefore();
        if (this.mCurrentState == 0) {
            this.mOverlapProjectManager.setVisible(true);
            this.mGalleryManager.showGalleryView(false);
        } else {
            this.mGalleryManager.showGalleryView(true);
        }
        this.mOverlapPreviewManager.setVisible(true);
        this.mOverlapPreviewManager.setGuideSeekbarVisible(true);
        OverlapPreviewManagerBase overlapPreviewManagerBase = this.mOverlapPreviewManager;
        if (!this.mIsFirstCaptured) {
            z = true;
        }
        overlapPreviewManagerBase.setGuideTextVisible(z);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mOverlapProjectManager.closeDatabase();
    }

    public String getShotMode() {
        return CameraConstants.MODE_SQUARE_OVERLAP;
    }

    protected void onCameraSwitchingEnd() {
        super.onCameraSwitchingEnd();
        if (isMenuShowing(4)) {
            CamLog.m3d(CameraConstants.TAG, "film menu is showing. return");
            return;
        }
        if (this.mCurrentState == 0) {
            this.mOverlapProjectManager.setVisible(true);
            this.mGalleryManager.showGalleryView(false);
        } else {
            this.mGalleryManager.showGalleryView(true);
        }
        this.mOverlapPreviewManager.setVisible(true);
        showPreviewGuide();
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        String projectId = null;
        if (this.mCaptureMode == 0) {
            projectId = ((OverlapProjectDb) this.mOverlapProjectManager.onLoadedSamples().get(this.mOverlapProjectManager.getCurrProjectIndex())).getProjectId();
        }
        FileManager.setProjectId(projectId);
        Uri uri = super.doSaveImage(data, extraExif, dir, filename);
        if (this.mCaptureMode == 1) {
            createSampleProjectFromCamera(uri);
        }
        return uri;
    }

    public void onShutterLargeButtonClicked() {
        if (checkModuleValidate(15) && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 12:
                    onCameraShutterButtonClicked();
                    return;
                default:
                    super.onShutterLargeButtonClicked();
                    return;
            }
        }
    }

    public int getShutterButtonType() {
        return 4;
    }

    public boolean doBackKey() {
        boolean result = super.doBackKey();
        if (!result && this.mCaptureMode == 1 && this.mOverlapProjectManager.onLoadedSamples().size() > 0 && this.mGet.checkModuleValidate(48)) {
            changeCaptureMode(0);
            return true;
        } else if (!result && this.mCurrentState == 1) {
            deleteImmediatelyNotUndo();
            this.mGalleryManager.cancleNewItemAdd();
            onProjectMove();
            return true;
        } else if (result || this.mIniGuideManager == null || !this.mIniGuideManager.getGuideLayouVisiblity()) {
            return result;
        } else {
            this.mIniGuideManager.removeViews();
            return true;
        }
    }

    protected void onTakePictureBefore() {
        if (!this.mIsFirstCaptured) {
            this.mIsFirstCaptured = this.mCaptureMode != 1;
            CamLog.m3d(CameraConstants.TAG, "mIsFirstCaptured " + this.mIsFirstCaptured);
        }
        super.onTakePictureBefore();
        if (this.mCaptureButtonManager.getShutterButtonMode(getShutterButtonType()) == 11) {
            setCaptureButtonEnable(true, getShutterButtonType());
        }
    }

    protected boolean takePicture() {
        if (this.mCaptureMode == 1) {
            if (this.mIsCreatingSampleFromCamera) {
                return false;
            }
            this.mIsCreatingSampleFromCamera = true;
        }
        boolean result = super.takePicture();
        if (result && this.mCaptureMode != 0) {
            return result;
        }
        this.mIsCreatingSampleFromCamera = false;
        return result;
    }

    public boolean isSquareGalleryBtn() {
        return this.mCurrentState == 1;
    }

    protected void setManagersListener() {
        super.setManagersListener();
        this.mOverlapProjectManager.setProjectManagerInterface(this);
        this.mOverlapPreviewManager.setPreviewManagerInterface(this);
    }

    protected void setBtimapImageToAnimManager(ExifInterface exif) {
        if (this.mCurrentState == 1) {
            super.setBtimapImageToAnimManager(exif);
        }
    }

    public boolean isProjectSelectable() {
        if (!this.mGet.checkModuleValidate(48)) {
            return false;
        }
        if (this.mTimerManager.isTimerShotCountdown() || isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT)) {
            return false;
        }
        return true;
    }

    public void onProjectSelected(int projectIndex, boolean isForceToSnap, boolean isUserClick) {
        CamLog.m3d(CameraConstants.TAG, "[Cell]onProjectSelected select index : " + projectIndex + " currproject index : " + this.mOverlapProjectManager.getCurrProjectIndex() + " isForceToSnap : " + isForceToSnap + " isUserClick " + isUserClick);
        if (!isUserClick || isProjectSelectable()) {
            ArrayList<OverlapProjectDb> tempProjectArr = this.mOverlapProjectManager.onLoadedSamples();
            if (projectIndex == tempProjectArr.size()) {
                showDialog(146);
            } else if (this.mOverlapProjectManager.getCurrProjectIndex() != projectIndex || isForceToSnap) {
                OverlapProjectDb currProject = (OverlapProjectDb) tempProjectArr.get(projectIndex);
                this.mOverlapProjectManager.setCurrProjectIndex(projectIndex);
                this.mOverlapProjectManager.notifyProjectAdaper();
                this.mOverlapPreviewManager.setProjectView(currProject, this.mOverlapProjectManager.getProjectAdapter());
                if (isForceToSnap) {
                    this.mOverlapProjectManager.removeSlideShow();
                    makeUriList(projectIndex, false);
                } else if (!this.mIsToSnapAfterAddingSample) {
                    this.mOverlapProjectManager.addSlideShow();
                }
                if (getOverlapCaptureMode() == 1) {
                    changeCaptureMode(0);
                }
            } else if (this.mOverlapProjectManager.isSlideShowing()) {
                this.mOverlapProjectManager.removeSlideShow();
            } else {
                this.mOverlapProjectManager.addSlideShow();
            }
        }
    }

    public void makeUriList(int projectIndex, boolean isRunByCaptured) {
        int i;
        CamLog.m3d(CameraConstants.TAG, "[Cell] makeUriList projectIndex " + projectIndex);
        OverlapProjectDb currProject = (OverlapProjectDb) this.mOverlapProjectManager.onLoadedSamples().get(projectIndex);
        int preset = currProject.getPreset();
        String imagePath = currProject.getSamplePath();
        this.mUriList.clear();
        ArrayList<Uri> pictureList = this.mOverlapProjectManager.getMostRecentContent(this.mOverlapProjectManager.getBucketId(getCurDir()), currProject.getProjectId(), "");
        if (pictureList != null) {
            for (i = 0; i < pictureList.size(); i++) {
                this.mUriList.add(pictureList.get(i));
            }
        }
        if (preset == -1) {
        }
        for (i = 0; i < 1; i++) {
            this.mUriList.add(SquareUtil.getOverlapSampleUri(preset, i, imagePath));
        }
        if (this.mGalleryManager != null) {
            this.mGalleryManager.resetBitmapList();
            if (!isRunByCaptured) {
                Uri tempUri = (Uri) this.mUriList.get(0);
                this.mUriList.remove(0);
                this.mGalleryManager.loadTheFirstOfSavedItems(tempUri, null, 0);
            }
        }
    }

    public void onGalleryBitmapLoaded(boolean useAnimation) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] onGalleryBitmapLoaded");
        if (this.mCurrentState == 0) {
            this.mCurrentState = 1;
            this.mOverlapProjectManager.removeSlideShow();
            this.mGalleryManager.loadTheRestOfSavedItems(this.mUriList, null, 1, 0);
            this.mOverlapProjectManager.setVisible(false);
            this.mGalleryManager.showGalleryView(true);
            this.mGalleryManager.showGalleryControlUI(false, 0, false);
            this.mQuickClipManager.setForceStatusForSquareMode(false);
            if (this.mGalleryManager.isOverlapSampleUri()) {
                this.mReviewThumbnailManager.setThumbnailDefault(false, false);
                this.mReviewThumbnailManager.setEnabled(false);
                this.mQuickClipManager.setQuickClipIcon(false);
                return;
            }
            this.mReviewThumbnailManager.setThumbnailDefault(false, true);
            SquareSnapGalleryItem item = getCurSquareSnapItem();
            if (item != null) {
                setQuickClipSharedUri(item.mUri);
                return;
            }
            return;
        }
        super.onGalleryBitmapLoaded(useAnimation);
    }

    public void onProjectMove() {
        CamLog.m3d(CameraConstants.TAG, "[Cell]onProjectMove ");
        this.mGalleryManager.cancelOriginalImageMaking();
        this.mOverlapProjectManager.notifyProjectAdaper();
        this.mOverlapProjectManager.addSlideShow();
        this.mOverlapProjectManager.setVisible(true);
        this.mGalleryManager.showGalleryView(false);
        this.mCurrentState = 0;
        this.mReviewThumbnailManager.updateThumbnail(true);
        setQuickClipSharedUri(this.mReviewThumbnailManager.getUri());
        this.mQuickClipManager.setQuickClipIcon(true);
    }

    public boolean isSelfie() {
        return !isRearCamera();
    }

    public void doOkClickInDeleteConfirmDialog(int type) {
        if (type == 2) {
            super.doOkClickInDeleteConfirmDialog(type);
            return;
        }
        OverlapProjectDb currProject = this.mOverlapProjectManager.deleteProject();
        this.mOverlapPreviewManager.setProjectView(currProject, this.mOverlapProjectManager.getProjectAdapter());
        if (currProject == null) {
            this.mOverlapPreviewManager.setVisible(false);
            changeCaptureMode(1);
        }
    }

    private void startGallery() {
        if (!this.mIsStartedGallery) {
            CamLog.m3d(CameraConstants.TAG, "[Cell]startGallery ");
            Intent intent = new Intent("android.intent.action.PICK");
            intent.setType("vnd.android.cursor.dir/image");
            this.mGet.getActivity().startActivityForResult(intent, 6);
            this.mIsStartedGallery = true;
            AppControlUtil.setLaunchingGallery(true);
        }
    }

    protected void resultFromPickImage(Intent data) {
        this.mImportedUri = data.getData();
        CamLog.m3d(CameraConstants.TAG, "[Cell]resultFromPickImage " + this.mImportedUri.toString());
        startCameraCrop();
    }

    private void startCameraCrop() {
        CamLog.m3d(CameraConstants.TAG, "[Cell]startCameraCrop ");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(1);
        intent.setDataAndType(this.mImportedUri, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("scale", true);
        intent.putExtra("lge-crop-max-cue", true);
        intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
        File tempFile = getTempFile(new File(Environment.getExternalStorageDirectory(), OverlapProjectDbAdapter.SAMPLE_IMAGE), 0);
        if (tempFile == null || !CheckStatusManager.checkDataStorageEnough(0)) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]startCameraCrop can not execute");
            return;
        }
        intent.putExtra("output", Uri.fromFile(tempFile));
        this.mGet.getActivity().startActivityForResult(intent, 7);
    }

    protected void resultFromCropImage(Intent data) {
        createSampleProjectFromGallery(getTempFile(new File(Environment.getExternalStorageDirectory(), OverlapProjectDbAdapter.SAMPLE_IMAGE), 1));
        this.mIsToSnapAfterAddingSample = true;
    }

    private void createSampleProjectFromGallery(File file) {
        if (file == null) {
            CamLog.m5e(CameraConstants.TAG, "[Cell] data storage is not enough");
            this.mOverlapProjectManager.deleteFile(file);
            this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.storage_full_msg_1_internal), CameraConstants.TOAST_LENGTH_LONG);
            return;
        }
        int userSampleCnt = this.mOverlapProjectManager.getUserSampleCnt() + 1;
        String sampleName = "user_sample_" + userSampleCnt + ".jpg";
        CamLog.m3d(CameraConstants.TAG, "[Cell]sampleName " + sampleName);
        boolean copySucceed = false;
        try {
            copySucceed = this.mOverlapProjectManager.copyFile(file, SquareUtil.getSampleFilesDir(getAppContext()) + sampleName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mOverlapProjectManager.deleteFile(file);
        if (copySucceed) {
            this.mOverlapPreviewManager.setProjectView(this.mOverlapProjectManager.insertNewUserProject(sampleName, userSampleCnt), this.mOverlapProjectManager.getProjectAdapter());
            return;
        }
        boolean delete = this.mOverlapProjectManager.deleteFile(new File(SquareUtil.getSampleFilesDir(getAppContext()), sampleName));
        this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.storage_full_msg_1_internal), CameraConstants.TOAST_LENGTH_LONG);
        CamLog.m5e(CameraConstants.TAG, "[Cell]copy fail : IOException " + delete);
    }

    private void createSampleProjectFromCamera(Uri uri) {
        String filePath = FileUtil.getRealPathFromURI(getAppContext(), uri);
        if (filePath == null) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SquareOverlapCameraModule.this.changeCaptureMode(0);
                }
            });
            return;
        }
        int height = SquareUtil.getHeight(getAppContext());
        Bitmap reviewBmp = BitmapManagingUtil.loadScaledandRotatedBitmap(getAppContext().getContentResolver(), uri.toString(), height, height, Exif.getOrientation(Exif.readExif(filePath)));
        if (reviewBmp == null) {
            changeCaptureMode(0);
            return;
        }
        final int userSampleCnt = this.mOverlapProjectManager.getUserSampleCnt() + 1;
        final String sampleName = "user_sample_" + userSampleCnt + ".jpg";
        CamLog.m3d(CameraConstants.TAG, "[Cell]sampleName " + sampleName);
        if (BitmapManagingUtil.saveBitmapToFile(SquareUtil.getSampleFilesDir(getAppContext()) + sampleName, reviewBmp)) {
            final Uri uri2 = uri;
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    String filePath = FileUtil.getRealPathFromURI(SquareOverlapCameraModule.this.mGet.getActivity(), uri2);
                    if (filePath != null) {
                        try {
                            File file = new File(filePath);
                            if (file != null && file.exists()) {
                                OverlapProjectDb currProject = SquareOverlapCameraModule.this.mOverlapProjectManager.insertNewUserProject(sampleName, userSampleCnt);
                                if (currProject != null) {
                                    SquareOverlapCameraModule.this.mOverlapPreviewManager.setProjectView(currProject, SquareOverlapCameraModule.this.mOverlapProjectManager.getProjectAdapter());
                                    SquareOverlapCameraModule.this.onProjectSelected(SquareOverlapCameraModule.this.mOverlapProjectManager.getProjectCnt() - 1, false, false);
                                    SquareOverlapCameraModule.this.mOverlapProjectManager.setGridViewMoveToPosition(SquareOverlapCameraModule.this.mOverlapProjectManager.getProjectCnt() - 1, true);
                                }
                                SquareOverlapCameraModule.this.changeCaptureMode(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            SquareOverlapCameraModule.this.changeCaptureMode(0);
                        }
                    }
                }
            });
            return;
        }
        changeCaptureMode(0);
        this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.storage_full_msg_1_internal), CameraConstants.TOAST_LENGTH_LONG);
    }

    private File getTempFile(File file, int type) {
        CamLog.m3d(CameraConstants.TAG, "[Cell]getTempFile path : " + file.getAbsolutePath() + ", type : " + type);
        if (type == 1) {
            try {
                if (file.exists()) {
                    CamLog.m7i(CameraConstants.TAG, "[Cell]file exists, size : " + file.length());
                    if (file.length() == 0 || !CheckStatusManager.checkDataStorageEnough(file.length())) {
                        return null;
                    }
                    return file;
                }
            } catch (IOException e) {
                CamLog.m5e(CameraConstants.TAG, "[Cell]fail createNewFile");
                return null;
            }
        }
        file.createNewFile();
        return file;
    }

    public boolean reloadGalleryList() {
        if (this.mGalleryManager.getGalleryItemList() == null) {
            return false;
        }
        OverlapProjectDb currProject = (OverlapProjectDb) this.mOverlapProjectManager.onLoadedSamples().get(this.mOverlapProjectManager.getCurrProjectIndex());
        ArrayList<Uri> pictureList = this.mOverlapProjectManager.getMostRecentContent(this.mOverlapProjectManager.getBucketId(getCurDir()), currProject.getProjectId(), "");
        int currProjectRecentUriCnt = getProjectRecentUriCnt(currProject, pictureList);
        CamLog.m7i(CameraConstants.TAG, "[Cell]current gallery item list : " + this.mGalleryManager.getGalleryItemList().size() + "  newly query count : " + currProjectRecentUriCnt + ", forceReload() : " + forceReload());
        if (this.mGalleryManager.getGalleryItemList().size() <= currProjectRecentUriCnt && !forceReload()) {
            return false;
        }
        int i;
        CamLog.m7i(CameraConstants.TAG, "[Cell]compareItemsCnt start");
        ArrayList<Uri> currList = new ArrayList();
        addCurrUriList(currProject, pictureList, currList);
        this.mHashMap.clear();
        String[] oldUriList = new String[this.mGalleryManager.getGalleryItemList().size()];
        for (i = 0; i < this.mGalleryManager.getGalleryItemList().size(); i++) {
            oldUriList[i] = ((SquareSnapGalleryItem) this.mGalleryManager.getGalleryItemList().get(i)).mUri.toString();
        }
        this.mGalleryManager.resetBitmapList();
        for (i = 0; i < currList.size(); i++) {
            this.mHashMap.put(((Uri) currList.get(i)).toString(), Integer.valueOf(i));
            this.mGalleryManager.addUriToGalleryItemList((Uri) currList.get(i), null, 0);
        }
        this.mGalleryManager.changeGalleryItemList(this.mHashMap, oldUriList, forceReload());
        if (this.mGalleryManager.isOverlapSampleUri()) {
            this.mReviewThumbnailManager.setEnabled(false);
            this.mQuickClipManager.setQuickClipIcon(false);
        }
        this.mIsOverlapNeedToReloadList = false;
        CamLog.m7i(CameraConstants.TAG, "[Cell]compareItemsCnt end");
        return true;
    }

    private boolean forceReload() {
        return this.mIsOverlapNeedToReloadList || (this.mGalleryManager != null && this.mGalleryManager.isNewItemAdding());
    }

    protected int getProjectRecentUriCnt(OverlapProjectDb currProject, ArrayList<Uri> pictureList) {
        if (currProject.getPreset() == -1) {
        }
        int totalCnt = 1;
        if (pictureList != null) {
            return pictureList.size() + 1;
        }
        return totalCnt;
    }

    protected void addCurrUriList(OverlapProjectDb currProject, ArrayList<Uri> pictureList, ArrayList<Uri> currList) {
        int i;
        int preset = currProject.getPreset();
        if (preset == -1) {
        }
        String imagePath = currProject.getSamplePath();
        if (pictureList != null) {
            for (i = 0; i < pictureList.size(); i++) {
                currList.add(pictureList.get(i));
            }
        }
        for (i = 0; i < 1; i++) {
            currList.add(SquareUtil.getOverlapSampleUri(preset, i, imagePath));
        }
    }

    public void reloadList() {
        CamLog.m3d(CameraConstants.TAG, "[Cell]reloadList");
        if (this.mOverlapProjectManager.getProjectLayout() != null) {
            if (this.mCurrentState == 0) {
                this.mOverlapProjectManager.notifyProjectAdaper();
                this.mOverlapProjectManager.addSlideShow();
                return;
            }
            reloadGalleryList();
        }
    }

    public void onOkButtonClicked(boolean isChecked) {
        this.mIniGuideManager.removeViews();
        if (isChecked) {
            SharedPreferenceUtilBase.saveOverlapShotFirstGuide(this.mGet.getAppContext(), true);
        }
    }

    public void changeCaptureMode(int value) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] changeCaptureMode " + value);
        this.mCaptureMode = value;
        if (this.mCaptureMode == 1) {
            this.mOverlapProjectManager.removeSlideShow();
            if (this.mOverlapProjectManager.onLoadedSamples().size() > 0) {
                this.mOverlapProjectManager.showTouchBlockCoverView(true);
            }
            this.mOverlapPreviewManager.setVisible(false);
            return;
        }
        this.mOverlapProjectManager.showTouchBlockCoverView(false);
        if (isMenuShowing(7)) {
            this.mOverlapPreviewManager.setVisible(false);
        } else if (this.mOnShowMenuFilm) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (!SquareOverlapCameraModule.this.isMenuShowing(7) && SquareOverlapCameraModule.this.mCaptureMode == 0) {
                        SquareOverlapCameraModule.this.mOverlapPreviewManager.setVisible(true);
                    }
                }
            });
        } else {
            this.mOverlapPreviewManager.setVisible(true);
        }
        this.mIsCreatingSampleFromCamera = false;
    }

    public int getOverlapCaptureMode() {
        return this.mCaptureMode;
    }

    public void doSelectInOverlapSampleDialog(int id) {
        if (id == C0088R.string.option_take_photo) {
            changeCaptureMode(1);
        } else if (id == C0088R.string.select_photo) {
            startGallery();
        }
    }

    public void onSeekBarAnimationEnd() {
        this.mOverlapProjectManager.getProjectAdapter().addBitmapToMemoryCacheInThread();
        this.mOverlapProjectManager.addSlideShow();
    }
}
