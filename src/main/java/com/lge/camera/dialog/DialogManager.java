package com.lge.camera.dialog;

import android.content.res.Configuration;
import android.util.SparseArray;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.ModeItem;
import com.lge.camera.util.CamLog;

public class DialogManager {
    private static String sCtsContent = "content://android.provider.cts.fileprovider/debug/capture.jpg";
    private boolean isConsentOpenedFromSetting = false;
    private int mDegree = 0;
    private int mDialogId = -1;
    private CamDialogInterface mGet = null;
    private SparseArray<RotateDialog> mRotateDialogs = new SparseArray();

    public DialogManager(CamDialogInterface dialogInterface) {
        this.mGet = dialogInterface;
    }

    public void setDialogInterface(CamDialogInterface dialogInterface) {
        this.mGet = dialogInterface;
    }

    public int getDialogID() {
        return this.mDialogId;
    }

    public void showDialogPopup(int id) {
        if (id == this.mDialogId) {
            CamLog.m3d(CameraConstants.TAG, "[dialog] same as previous dialog id");
        } else {
            showDialogPopup(id, null, true);
        }
    }

    public void showDialogPopup(int id, boolean showAnim) {
        showDialogPopup(id, null, true, showAnim);
    }

    public void showDialogPopup(int id, String setting, boolean isCheckBoxNeeded) {
        showDialogPopup(id, setting, isCheckBoxNeeded, true);
    }

    protected void showDialogPopup(int id, String setting, boolean isCheckBoxNeeded, boolean showAnim) {
        CamLog.m3d(CameraConstants.TAG, "[dialog] request dialog id : " + id);
        if (this.mDialogId == 12 && id == 138) {
            this.isConsentOpenedFromSetting = true;
        }
        this.mDialogId = id;
        if (onCreateRotateableDialog(id, showAnim)) {
            this.mDegree = this.mGet.getOrientationDegree();
            setDegree(this.mGet.getOrientationDegree(), showAnim);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "Do not match dialog id.");
    }

    public void setDegree(int degree, boolean showAnim) {
        if (this.mRotateDialogs != null) {
            for (int i = 0; i < this.mRotateDialogs.size(); i++) {
                RotateDialog dialog = (RotateDialog) this.mRotateDialogs.valueAt(i);
                if (dialog != null) {
                    dialog.setDegree(degree, showAnim);
                }
            }
        }
    }

    public boolean onCreateRotateableDialog(int dialogId, boolean showAnim) {
        ProgressCircleRotatableDialog progressCircleRotatableDialog;
        TagLocationRotatableDialog tagLocationRotatableDialog;
        switch (dialogId) {
            case 2:
                DeleteRotatableDialog deleteDialog = new DeleteRotatableDialog(this.mGet);
                deleteDialog.create(C0088R.string.sp_photo_will_be_deleted_NORMAL, 2);
                this.mRotateDialogs.put(dialogId, deleteDialog);
                break;
            case 3:
                progressCircleRotatableDialog = new ProgressCircleRotatableDialog(this.mGet);
                progressCircleRotatableDialog.create(C0088R.string.pd_message_processing);
                this.mRotateDialogs.put(dialogId, progressCircleRotatableDialog);
                break;
            case 4:
                EnableGalleryRotatableDialog disabledDialog = new EnableGalleryRotatableDialog(this.mGet);
                disabledDialog.create();
                this.mRotateDialogs.put(dialogId, disabledDialog);
                break;
            case 5:
                progressCircleRotatableDialog = new ProgressCircleRotatableDialog(this.mGet);
                progressCircleRotatableDialog.create(C0088R.string.msg_save_progress);
                this.mRotateDialogs.put(dialogId, progressCircleRotatableDialog);
                break;
            case 6:
                AuCloudDialog auCloudDialog = new AuCloudDialog(this.mGet);
                auCloudDialog.create();
                this.mRotateDialogs.put(dialogId, auCloudDialog);
                break;
            case 7:
                if (!checkCTSIntent()) {
                    tagLocationRotatableDialog = new TagLocationRotatableDialog(this.mGet);
                    tagLocationRotatableDialog.create(showAnim);
                    this.mRotateDialogs.put(dialogId, tagLocationRotatableDialog);
                    break;
                }
                break;
            case 8:
                StorageInitRotatableDialog storageInitRotatableDialog = new StorageInitRotatableDialog(this.mGet);
                storageInitRotatableDialog.create(showAnim);
                this.mRotateDialogs.put(dialogId, storageInitRotatableDialog);
                break;
            case 9:
                SelectSizeRotatableDialog selectSizeRotatableDialog = new SelectSizeRotatableDialog(this.mGet);
                selectSizeRotatableDialog.create();
                this.mRotateDialogs.put(dialogId, selectSizeRotatableDialog);
                break;
            case 10:
                SelectSaveDirectionRotatableDialog selectSaveDirectionRotatableDialog = new SelectSaveDirectionRotatableDialog(this.mGet);
                selectSaveDirectionRotatableDialog.create();
                this.mRotateDialogs.put(dialogId, selectSaveDirectionRotatableDialog);
                break;
            case 11:
                createEnablePackageDialog(dialogId, CameraConstants.PACKAGE_GOOGLE_PHOTO);
                break;
            case 12:
                tagLocationRotatableDialog = new TagLocationRotatableDialog(this.mGet);
                tagLocationRotatableDialog.create(showAnim);
                this.mRotateDialogs.put(dialogId, tagLocationRotatableDialog);
                break;
            case 13:
                ProgressBarRotatableDialog progressBarRotatableDialog = new ProgressBarRotatableDialog(this.mGet);
                progressBarRotatableDialog.create(C0088R.string.msg_save_progress);
                this.mRotateDialogs.put(dialogId, progressBarRotatableDialog);
                break;
            case 14:
                GraphyInstallDialog graphyInstallDialog = new GraphyInstallDialog(this.mGet);
                graphyInstallDialog.create(showAnim);
                this.mRotateDialogs.put(dialogId, graphyInstallDialog);
                break;
            case 15:
                GraphyOnDialog graphyOnDialog = new GraphyOnDialog(this.mGet);
                graphyOnDialog.create(showAnim);
                this.mRotateDialogs.put(dialogId, graphyOnDialog);
                break;
            case 16:
                GraphyFullOfImageDialog graphyFullOfImageDialog = new GraphyFullOfImageDialog(this.mGet);
                graphyFullOfImageDialog.create(showAnim);
                this.mRotateDialogs.put(dialogId, graphyFullOfImageDialog);
                break;
            case 17:
                createEnablePackageDialog(dialogId, CameraConstants.PACKAGE_YOUTUBE);
                break;
            case 122:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_1_internal);
                break;
            case 123:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_1_external);
                break;
            case 124:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_2_internal);
                break;
            case 125:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_2_external);
                break;
            case 126:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_2_uplus_cloud_to_internal1);
                break;
            case 127:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_2_uplus_cloud_to_external1);
                break;
            case 128:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_3);
                break;
            case 130:
                SnapInitRotatableDialog snapInitRotatableDialog = new SnapInitRotatableDialog(this.mGet);
                snapInitRotatableDialog.create();
                this.mRotateDialogs.put(dialogId, snapInitRotatableDialog);
                break;
            case 131:
                SnapDeleteRotatableDialog snapDeleteRotatableDialog = new SnapDeleteRotatableDialog(this.mGet);
                snapDeleteRotatableDialog.create();
                this.mRotateDialogs.put(dialogId, snapDeleteRotatableDialog);
                break;
            case 132:
                progressCircleRotatableDialog = new ProgressCircleRotatableDialog(this.mGet);
                progressCircleRotatableDialog.create(C0088R.string.msg_multiview_save_progress);
                this.mRotateDialogs.put(dialogId, progressCircleRotatableDialog);
                break;
            case 133:
                SnapSaveNoteRotateDialog snapSaveNoteRotateDialog = new SnapSaveNoteRotateDialog(this.mGet);
                snapSaveNoteRotateDialog.create();
                this.mRotateDialogs.put(dialogId, snapSaveNoteRotateDialog);
                break;
            case 134:
                SnapClipStorageWarningRotateDialog snapClipStorageWarningRotateDialog = new SnapClipStorageWarningRotateDialog(this.mGet);
                snapClipStorageWarningRotateDialog.create();
                this.mRotateDialogs.put(dialogId, snapClipStorageWarningRotateDialog);
                break;
            case 138:
                VZWConsentDialog consentDlg = new VZWConsentDialog(this.mGet);
                consentDlg.create();
                this.mRotateDialogs.put(dialogId, consentDlg);
                break;
            case 139:
                HifiRotatableDialog hifiRotatableDialog = new HifiRotatableDialog(this.mGet);
                hifiRotatableDialog.create();
                this.mRotateDialogs.put(dialogId, hifiRotatableDialog);
                break;
            case 140:
                FingerprintInitialRotatableDialog fpDialog = new FingerprintInitialRotatableDialog(this.mGet);
                fpDialog.create(false);
                this.mRotateDialogs.put(dialogId, fpDialog);
                break;
            case 141:
                ProgressCircleRotatableDialog deletePrDialog = new ProgressCircleRotatableDialog(this.mGet);
                deletePrDialog.create(C0088R.string.sp_deleting_NORMAL);
                this.mRotateDialogs.put(dialogId, deletePrDialog);
                break;
            case 142:
                DeleteProjectRotatableDialog deleteProjectDialog = new DeleteProjectRotatableDialog(this.mGet);
                deleteProjectDialog.create();
                this.mRotateDialogs.put(dialogId, deleteProjectDialog);
                break;
            case 143:
                DeleteSampleRotatableDialog deleteSample = new DeleteSampleRotatableDialog(this.mGet);
                deleteSample.create();
                this.mRotateDialogs.put(dialogId, deleteSample);
                break;
            case 144:
                UndoDeleteRotatableDialog undoDlg = new UndoDeleteRotatableDialog(this.mGet);
                undoDlg.create();
                this.mRotateDialogs.put(dialogId, undoDlg);
                break;
            case 145:
                createStorageFullDialog(dialogId, C0088R.string.storage_full_msg_uplus_cloud_cache1);
                break;
            case 146:
                OverlapSelectSampleDialog overlapSelectSampleDialog = new OverlapSelectSampleDialog(this.mGet);
                overlapSelectSampleDialog.create();
                this.mRotateDialogs.put(dialogId, overlapSelectSampleDialog);
                break;
            case 147:
                CNasRotatableDialog cnasDialog = new CNasRotatableDialog(this.mGet);
                cnasDialog.create();
                this.mRotateDialogs.put(dialogId, cnasDialog);
                break;
            case 148:
                ManualModeInitialRotatableDialog manualModeInitialRotatableDialog = new ManualModeInitialRotatableDialog(this.mGet);
                manualModeInitialRotatableDialog.create(true, null, dialogId);
                this.mRotateDialogs.put(dialogId, manualModeInitialRotatableDialog);
                break;
            case 149:
                DeleteRotatableDialog deleteDialogForFilter = new DeleteRotatableDialog(this.mGet);
                deleteDialogForFilter.create(C0088R.string.delete_filter, 149);
                this.mRotateDialogs.put(dialogId, deleteDialogForFilter);
                break;
            case 151:
                DeleteRotatableDialog deleteDialogForSticker = new DeleteRotatableDialog(this.mGet);
                deleteDialogForSticker.create(C0088R.string.sticker_delete, 151);
                this.mRotateDialogs.put(dialogId, deleteDialogForSticker);
                break;
            default:
                return false;
        }
        return true;
    }

    private void createEnablePackageDialog(int dialogId, String packageName) {
        EnablePackageRotatableDialog enabledDialog = new EnablePackageRotatableDialog(this.mGet);
        enabledDialog.create(packageName);
        this.mRotateDialogs.put(dialogId, enabledDialog);
    }

    private void createStorageFullDialog(int dialogId, int msgId) {
        StorageFullRotatableDialog storageFullDialog = new StorageFullRotatableDialog(this.mGet, msgId);
        storageFullDialog.create();
        this.mRotateDialogs.put(dialogId, storageFullDialog);
    }

    public void onConfigurationChanged(Configuration config) {
        setOrientationDegree(this.mGet.getOrientationDegree());
    }

    public void onPause() {
        onDismissRotateDialog();
    }

    public void setOrientationDegree(int degree) {
        if ((this.mGet.isPostviewShowing() && !this.mGet.isOrientationLocked()) || this.mDegree != degree) {
            this.mDegree = degree;
            setDegree(this.mDegree, true);
        }
    }

    public void onDismissRotateDialog() {
        if (!this.mGet.isCollageProgressing() || getDialogID() != 132) {
            CamLog.m3d(CameraConstants.TAG, "[dialog] dismiss dialog id : " + this.mDialogId);
            RotateDialog dialog = (RotateDialog) this.mRotateDialogs.get(this.mDialogId);
            if (dialog != null && dialog.onDismiss()) {
                this.mRotateDialogs.remove(this.mDialogId);
            }
        }
    }

    public void onDismissRotateDialog(boolean immediately) {
        if (!this.mGet.isCollageProgressing() || getDialogID() != 132) {
            CamLog.m3d(CameraConstants.TAG, "[dialog] dismiss dialog id : " + this.mDialogId);
            RotateDialog dialog = (RotateDialog) this.mRotateDialogs.get(this.mDialogId);
            if (dialog != null && dialog.onDismiss(immediately)) {
                this.mRotateDialogs.remove(this.mDialogId);
            }
        }
    }

    public void onDismiss() {
        if (this.mRotateDialogs != null && this.mGet != null) {
            if (!this.mGet.isCollageProgressing() || getDialogID() != 132) {
                if (isRotateDialogVisible()) {
                    this.mRotateDialogs.remove(this.mDialogId);
                }
                if (this.mDialogId != 138) {
                    this.mDialogId = -1;
                    return;
                }
                this.mDialogId = this.isConsentOpenedFromSetting ? 12 : 7;
                if (this.mGet.isPaused()) {
                    onDismissRotateDialog();
                }
                this.isConsentOpenedFromSetting = false;
            }
        }
    }

    public boolean isRotateDialogVisible() {
        if (this.mRotateDialogs.get(this.mDialogId) == null) {
            return false;
        }
        return true;
    }

    public boolean isRotateDialogVisible(int type) {
        if (this.mDialogId == type && isRotateDialogVisible()) {
            return true;
        }
        return false;
    }

    public boolean isBackKeyAvailable() {
        if (!isRotateDialogVisible() || getDialogID() == 3 || getDialogID() == 5 || getDialogID() == 13 || getDialogID() == 132) {
            return false;
        }
        return true;
    }

    public boolean isProgressDialogVisible() {
        if (isRotateDialogVisible() && (getDialogID() == 3 || getDialogID() == 5 || getDialogID() == 13 || getDialogID() == 132)) {
            return true;
        }
        return false;
    }

    public void setProgress(int progress) {
        if (getDialogID() != 13) {
            CamLog.m3d(CameraConstants.TAG, "[dialog] is not progress_bar_dialog");
        } else {
            ((ProgressBarRotatableDialog) this.mRotateDialogs.get(this.mDialogId)).setProgress(progress);
        }
    }

    public void showModeDeleteDialog(ModeItem item) {
        CamLog.m3d(CameraConstants.TAG, "[mode] show dialog");
        this.mDialogId = 150;
        DeleteModeDialog deleteModeDlg = new DeleteModeDialog(this.mGet);
        deleteModeDlg.create(item);
        this.mRotateDialogs.put(150, deleteModeDlg);
        this.mDegree = this.mGet.getOrientationDegree();
        setDegree(this.mGet.getOrientationDegree(), true);
    }

    private boolean checkCTSIntent() {
        return this.mGet.isAttachIntent() && this.mGet.getActivity().getIntent().toString().contains(sCtsContent);
    }
}
