package com.lge.camera.settings;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import com.google.lens.sdk.LensApi;
import com.google.lens.sdk.LensApi.LensAvailabilityCallback;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import java.io.File;
import java.util.List;

public class ModeMenuManager extends ModeMenuManagerBase {

    /* renamed from: com.lge.camera.settings.ModeMenuManager$1 */
    class C13851 implements LensAvailabilityCallback {
        C13851() {
        }

        public void onAvailabilityStatusFetched(int status) {
            CamLog.m3d(CameraConstants.TAG, "Google AR Stickers availability has changed to status " + status);
            ModeMenuManager.this.mGoogleArStickersStatus = status;
            if (status == 0) {
                CamLog.m3d(CameraConstants.TAG, "Google AR Stickers LENS_READY");
                ModeMenuManager.this.addARStickersMode();
            }
        }
    }

    /* renamed from: com.lge.camera.settings.ModeMenuManager$2 */
    class C13862 implements OnClickListener {
        C13862() {
        }

        public void onClick(View arg0) {
            boolean isSeledted = false;
            if (!(ModeMenuManager.this.mEditButton == null || ModeMenuManager.this.mEditButton.isSelected())) {
                isSeledted = true;
            }
            ModeMenuManager.this.onEditButtonClicked(isSeledted);
        }
    }

    /* renamed from: com.lge.camera.settings.ModeMenuManager$3 */
    class C13873 implements OnClickListener {
        C13873() {
        }

        public void onClick(View view) {
            if (!PackageUtil.isLGSmartWorldInstalled(ModeMenuManager.this.mGet.getAppContext())) {
                new SmartWorldCheckTask(ModeMenuManager.this, null).execute(new Void[0]);
            } else if (!PackageUtil.isLGSmartWorldEnabled(ModeMenuManager.this.mGet.getAppContext())) {
                ModeMenuManager.this.mGet.showToast(ModeMenuManager.this.mGet.getAppContext().getString(C0088R.string.error_not_exist_app), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } else if (!ModeMenuManager.this.mIsDownloadBtnClicked) {
                ModeMenuManager.this.mIsDownloadBtnClicked = true;
                Intent intent = new Intent(CameraConstantsEx.INTENT_SMART_WORLD);
                intent.putExtra(CameraConstantsEx.EXTRA_TYPE_SMART_WORLD, CameraConstantsEx.EXTRA_VALUE_SMART_WORLD_CATEGORY);
                intent.putExtra(CameraConstantsEx.EXTRA_TYPE_CATEGORY_ID, CameraConstantsEx.EXTRA_VALUE_CATEGORY_MODE);
                ModeMenuManager.this.mGet.getActivity().startActivity(intent);
            }
        }
    }

    /* renamed from: com.lge.camera.settings.ModeMenuManager$4 */
    class C13884 implements AnimationListener {
        C13884() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            ModeMenuManager.this.setGridView();
            AnimationUtil.startAlphaAnimation(ModeMenuManager.this.mGridLayout, 0.0f, 1.0f, 200, null);
        }
    }

    private class SmartWorldCheckTask extends AsyncTask<Void, Void, Void> {
        private SmartWorldCheckTask() {
        }

        /* synthetic */ SmartWorldCheckTask(ModeMenuManager x0, C13851 x1) {
            this();
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        protected Void doInBackground(Void... params) {
            PackageUtil.checkLGSmartWorldUpdated(ModeMenuManager.this.mGet.getAppContext());
            return null;
        }
    }

    public ModeMenuManager(ModeMenuInterface modeMenuInterface) {
        super(modeMenuInterface);
        if (this.mLensApi != null) {
            CamLog.m3d(CameraConstants.TAG, "AR Stickers");
            this.mLensApi = null;
        }
    }

    public void onResume() {
        if (FunctionProperties.isSupportedARStickers() && this.mLensApi == null) {
            this.mLensApi = new LensApi(this.mGet.getAppContext());
            this.mLensApi.checkArStickersAvailability(new C13851());
        }
        if (this.mLensApi != null) {
            CamLog.m3d(CameraConstants.TAG, "AR Stickers");
            this.mLensApi.onResume();
        }
        this.mIsDownloadBtnClicked = false;
        this.mDegree = this.mGet.getOrientationDegree();
        initDownloadableMode();
        makeModeList();
        setBtnListener();
    }

    protected void setBtnListener() {
        if (this.mEditBtnListener == null) {
            this.mEditBtnListener = new C13862();
        }
        if (this.mDownBtnListener == null) {
            this.mDownBtnListener = new C13873();
        }
    }

    public void onPause() {
        if (this.mLensApi != null) {
            CamLog.m3d(CameraConstants.TAG, "AR Stickers");
            this.mLensApi.onPause();
        }
        if (isVisible()) {
            this.mGet.hideModeMenu(true, true);
        }
        onEditButtonClicked(false);
        resetLayout();
        stopDragging();
        this.mEditBtnListener = null;
        this.mDownBtnListener = null;
        this.mListAdapter = null;
        saveModePreference();
        waitImageCacheThread(true);
        releaseAllImageResources();
        System.gc();
        if (this.mRearModeItemList != null) {
            this.mRearModeItemList.clear();
        }
        if (this.mFrontModeItemList != null) {
            this.mFrontModeItemList.clear();
        }
    }

    public void onDestroy() {
        resetLayout();
        this.mListAdapter = null;
        this.mRearModeItemList = null;
        this.mFrontModeItemList = null;
        this.mModeMenuClickListener = null;
        this.mEditBtnListener = null;
        this.mDownBtnListener = null;
        releaseAllImageResources();
        if (sDownloadableMode != null) {
            sDownloadableMode.clear();
            sDownloadableMode = null;
        }
        if (sDownloadableModeFiles != null) {
            sDownloadableModeFiles.clear();
            sDownloadableModeFiles = null;
        }
        this.mLensApi = null;
    }

    public void saveModePreference() {
        if (this.mRearModeItemList.size() > 0) {
            String rear = modeListToString(this.mRearModeItemList);
            if (!"".equals(rear)) {
                SharedPreferenceUtilBase.setModeList(this.mGet.getAppContext(), rear, true);
                CamLog.m3d(CameraConstants.TAG, "[mode] save Mode Preference - rear : " + rear);
            }
        }
        if (this.mFrontModeItemList.size() > 0) {
            String front = modeListToString(this.mFrontModeItemList);
            if (!"".equals(front)) {
                SharedPreferenceUtilBase.setModeList(this.mGet.getAppContext(), front, false);
                CamLog.m3d(CameraConstants.TAG, "[mode] save Mode Preference - front : " + front);
            }
        }
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mModeMenuView != null) {
            this.mModeMenuView.requestLayout();
            setDegree(this.mGet.getOrientationDegree());
        }
    }

    public void setDegree(int degree) {
        if (this.mDegree != degree) {
            CamLog.m3d(CameraConstants.TAG, "onOrientationChanged : mDegree = " + this.mDegree + ", degree = " + degree);
            this.mDegree = degree;
            if (this.mEditButton != null) {
                this.mEditButton.setDegree(this.mDegree, true);
            }
            if (this.mDownloadButton != null) {
                this.mDownloadButton.setDegree(this.mDegree, true);
            }
            if (isVisible() && this.mGridLayout != null) {
                AnimationUtil.startAlphaAnimation(this.mGridLayout, 1.0f, 0.0f, 200, new C13884());
            }
            if (this.mListAdapter != null) {
                this.mListAdapter.setListItemDegree(this.mDegree);
                this.mListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void deleteMode(ModeItem item) {
        if (this.mListAdapter != null && item != null) {
            String deletedMode = item.getValue();
            if (deletedMode != null && sDownloadableModeFiles != null && sDownloadableModeFiles.containsKey(deletedMode)) {
                String originalFileName = getOriginalFileName((String) sDownloadableModeFiles.get(deletedMode));
                if (originalFileName == null) {
                    CamLog.m3d(CameraConstants.TAG, "[mode] original file path is null");
                    return;
                }
                String filePath = ModeMenuManagerBase.MODE_DOWNLOAD_PATH + originalFileName;
                CamLog.m3d(CameraConstants.TAG, "[mode] deleted file path : " + filePath);
                try {
                    File file = new File(filePath);
                    if (file != null && file.exists() && file.delete()) {
                        CamLog.m3d(CameraConstants.TAG, "[mode] File deleted successfully");
                    }
                    if (this.mListAdapter != null) {
                        int deletedIndex = getModeItemList().indexOf(item);
                        getModeItemList().remove(deletedIndex);
                        String shotMode = this.mGet.getShotMode();
                        if (shotMode != null && shotMode.equals(deletedMode)) {
                            CamLog.m3d(CameraConstants.TAG, "[mode] cur mode & deleted mode is equal. change to Normal mode");
                            updateListViewItem("mode_normal");
                            changeMode("mode_normal");
                        } else if (isVisible()) {
                            this.mGridView.deleteAnimation(deletedIndex);
                            setGridView();
                        }
                        this.mListAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    CamLog.m6e(CameraConstants.TAG, "delete file failed : ", e);
                }
            }
        }
    }

    private String getOriginalFileName(String modeName) {
        List<File> fileList = getFileList(ModeMenuManagerBase.MODE_DOWNLOAD_PATH);
        if (fileList == null || modeName == null) {
            CamLog.m3d(CameraConstants.TAG, "[mode] file is null");
            return null;
        }
        for (File f : fileList) {
            String name = f.getName();
            if (name != null && name.contains(modeName)) {
                return name;
            }
        }
        return null;
    }

    public void startDragging(int position) {
        if (this.mLongClickedItem == null && this.mLongClickedIndex == -1 && this.mListAdapter != null && position >= 0 && position < getModeItemList().size()) {
            this.mIsDragging = true;
            this.mLongClickedItem = (ModeItem) getModeItemList().get(position);
            this.mLongClickedIndex = position;
            getModeItemList().set(position, null);
            this.mListAdapter.notifyDataSetChanged();
        }
    }

    public void drag(int position) {
        if (this.mIsDragging && this.mListAdapter != null && getModeItemList().contains(null) && getModeItemList().indexOf(null) != position) {
            if (position >= 0 && position < getModeItemList().size()) {
                getModeItemList().remove(null);
                getModeItemList().add(position, null);
                CamLog.m3d(CameraConstants.TAG, "[mode] add : " + position);
            }
            this.mListAdapter.notifyDataSetChanged();
        }
    }

    public void stopDragging() {
        if (this.mLongClickedItem != null && this.mLongClickedIndex != -1 && this.mIsDragging && this.mListAdapter != null) {
            if (getModeItemList().contains(null)) {
                int oldIndex = getModeItemList().indexOf(null);
                CamLog.m3d(CameraConstants.TAG, "[mode] stop : " + oldIndex);
                getModeItemList().set(oldIndex, this.mLongClickedItem);
            } else {
                getModeItemList().add(this.mLongClickedItem);
            }
            this.mLongClickedItem = null;
            this.mLongClickedIndex = -1;
            this.mIsDragging = false;
            updateListViewItem(this.mGet.getCurSettingValue(Setting.KEY_MODE));
        }
    }

    public void setEditMode(boolean edit) {
        onEditButtonClicked(edit);
    }

    public boolean isEditable() {
        if (this.mEditButton == null) {
            return false;
        }
        return this.mEditButton.isSelected();
    }
}
