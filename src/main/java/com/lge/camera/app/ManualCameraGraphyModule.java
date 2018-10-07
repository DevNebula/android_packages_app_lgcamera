package com.lge.camera.app;

import android.content.Intent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.GraphyControlManager;
import com.lge.camera.managers.GraphyDataManager;
import com.lge.camera.managers.GraphyInterface;
import com.lge.camera.managers.GraphyViewManager;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualUtil;
import com.lge.graphy.data.GraphyItem;
import java.util.ArrayList;

public class ManualCameraGraphyModule extends ManualCameraModule implements GraphyInterface {
    protected float mCurrentAperture = 0.0f;
    protected float mCurrentISO = 0.0f;
    protected float mCurrentSS = 0.0f;
    protected double mIlluminance = 1.0d;
    protected boolean mIsAngleChangedByGraphy = false;
    protected boolean mIsDataChangedByGraphy = false;
    protected boolean mIsFromLDU = false;

    public ManualCameraGraphyModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void initManualManager() {
        if (FunctionProperties.isSupportedGraphy()) {
            this.mGraphyControlManager = new GraphyControlManager(this);
            this.mGraphyDataManager = new GraphyDataManager(this);
            this.mGraphyViewManager = new GraphyViewManager(this);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] initManualManager mGraphyDataManager : " + this.mGraphyDataManager);
        }
    }

    public void onResumeAfter() {
        boolean z;
        boolean z2 = false;
        boolean isLDU = ModelProperties.isRetailModeInstalled();
        Intent intent = this.mGet.getActivity().getIntent();
        boolean isFromLDU = false;
        if (intent != null && "com.lge.graphy.action.LDU".equals(intent.getAction())) {
            isFromLDU = true;
        }
        if (isFromLDU && isLDU) {
            z = true;
        } else {
            z = false;
        }
        this.mIsFromLDU = z;
        boolean isAssistantGraphy = CameraConstantsEx.FLAG_VALUE_GRAPHY.equalsIgnoreCase(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null));
        if (this.mIsFromLDU || isAssistantGraphy) {
            z2 = true;
        }
        this.mIsFromLDU = z2;
        super.onResumeAfter();
    }

    protected void updateManualSettingValueFromMetadataCallback(float currentWB, float currentEV, float currentISO, float currentShutterSpeed, float currentMFStep) {
        super.updateManualSettingValueFromMetadataCallback(currentWB, currentEV, currentISO, currentShutterSpeed, currentMFStep);
        this.mCurrentISO = currentISO;
        this.mCurrentSS = currentShutterSpeed;
    }

    public void onPauseAfter() {
        if (this.mIsFromLDU) {
            this.mIsFromLDU = false;
        }
        super.onPauseAfter();
    }

    public boolean isFromLDU() {
        return this.mIsFromLDU;
    }

    public void setGraphyListVisibility(boolean visible, boolean animate) {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.setGraphyListVisibility(visible, animate);
        }
    }

    public void setGraphyButtonVisiblity(boolean visible) {
        if (this.mGraphyViewManager != null && !isJogZoomMoving()) {
            if (this.mManualViewManager != null && this.mManualViewManager.isDrumShowing(31)) {
                visible = false;
            }
            this.mGraphyViewManager.setGraphyButtonVisiblity(visible);
        }
    }

    public ArrayList<GraphyItem> getGraphyItems() {
        if (this.mGraphyDataManager == null) {
            return null;
        }
        return this.mGraphyDataManager.getGraphyItems();
    }

    public void setGraphyItems(ArrayList<GraphyItem> itemList) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] itemList : " + itemList);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] mGraphyViewManager : " + this.mGraphyViewManager);
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.setGraphyItems(itemList);
        }
    }

    public void sendSelectedItemToControlManager(GraphyItem item, boolean init) {
        if (this.mGraphyControlManager != null) {
            this.mGraphyControlManager.OnItemSelected(item, init);
        }
    }

    public boolean isFromGraphyApp() {
        return this.mGet.isFromGraphyApp();
    }

    public void setFromGraphyFlag(boolean flag) {
        this.mGet.setFromGraphyFlag(flag);
    }

    public void setGraphyIndex(int idx) {
        this.mGet.setGraphyIndex(idx);
    }

    public int getGraphyIndex() {
        return this.mGet.getGraphyIndex();
    }

    public void selectItem() {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.selectItem();
        }
    }

    public void selectItem(int position) {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.selectItem(position);
        }
    }

    public void spreadImageItems(int offset, int count, int category) {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.spreadImageItems(offset, count, category);
        }
    }

    public void foldImageItems(int offset, int count, int category) {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.foldImageItems(offset, count, category);
        }
    }

    public void attachBestImageItems() {
        if (this.mGraphyDataManager != null) {
            this.mGraphyDataManager.attachBestImageItems();
        }
    }

    public void removeBestImageItems() {
        if (this.mGraphyDataManager != null) {
            this.mGraphyDataManager.removeBestImageItems();
        }
    }

    public void attachMyFilterImageItems() {
        if (this.mGraphyDataManager != null) {
            this.mGraphyDataManager.attachMyFilterImageItems();
        }
    }

    public void removeMyFilterImageItems() {
        if (this.mGraphyDataManager != null) {
            this.mGraphyDataManager.removeMyFilterImageItems();
        }
    }

    public boolean isFoldedBestItem() {
        if (this.mGraphyViewManager != null) {
            return this.mGraphyViewManager.isFoldedBestItem();
        }
        return false;
    }

    public boolean isFoldedMyFilterItem() {
        if (this.mGraphyViewManager != null) {
            return this.mGraphyViewManager.isFoldedMyFilterItem();
        }
        return false;
    }

    public boolean setManualData(int type, String key, String value, boolean save) {
        boolean returnValue = super.setManualData(type, key, value, save);
        this.mIsDataChangedByGraphy = false;
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.notifyManualDataByUser(true);
            this.mGraphyViewManager.setGraphyListVisibility(false, false);
        }
        return returnValue;
    }

    public boolean setManualDataByGraphy(int type, String key, String value, boolean save) {
        this.mIsDataChangedByGraphy = true;
        return super.setManualData(type, key, value, save);
    }

    public void setGraphyData(String iso, String shutterSpeed, String wb) {
        if (this.mManualControlManager != null) {
            this.mManualControlManager.setGraphyData(iso, shutterSpeed, wb);
            this.mIsDataChangedByGraphy = true;
        }
    }

    public boolean IsDataChangedByGraphy() {
        return this.mIsDataChangedByGraphy;
    }

    public boolean checkGraphyAppInstalled() {
        if (this.mGraphyControlManager != null) {
            return this.mGraphyControlManager.isGrahyAppInstalled();
        }
        return false;
    }

    public void setEVGuideLayoutVisibility(boolean show, boolean over) {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.setEVGuideLayoutVisibility(show, over);
        }
    }

    public void selectMyFilterItem() {
        if (this.mGraphyDataManager != null) {
            this.mGraphyDataManager.selectMyFilterItem();
        }
    }

    public void requeryGraphyItems() {
        if (this.mGraphyDataManager != null) {
            this.mGraphyDataManager.requery();
        }
    }

    public int getLastGraphyIndex() {
        if (this.mGraphyControlManager != null) {
            return this.mGraphyControlManager.getLastGraphyIndex();
        }
        return -1;
    }

    public void saveLastGraphyIndex(int index) {
        if (this.mGraphyControlManager != null) {
            this.mGraphyControlManager.saveLastGraphyIndex(index);
        }
    }

    public void onShowGraphyList() {
        hideDrum();
        hideZoomBar();
        setFilmStrengthButtonVisibility(false, false);
    }

    public void onHideGraphyList() {
        setFilmStrengthButtonVisibility(true, false);
    }

    public void setButtonLocked(boolean locked, int key) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setButtonLocked(locked, key);
        }
    }

    public void setAllAuto() {
        if (this.mManualViewManager != null) {
            this.mIsDataChangedByGraphy = false;
            this.mManualViewManager.setButtonAndDrumUnselected(0);
            setButtonLocked(false, 31);
            setAELock(Boolean.valueOf(false));
            super.setManualData(4, "lg-wb", "0", true);
            super.setManualData(16, ParamConstants.MANUAL_FOCUS_STEP, "-1", true);
            setLockedFeature();
        }
    }

    public boolean isRotateDialogVisible(int type) {
        if (this.mDialogManager != null) {
            return this.mDialogManager.isRotateDialogVisible(type);
        }
        return false;
    }

    public void changeCameraAngle(boolean isWideAngle) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] isWideAngle : " + isWideAngle);
        this.mIsAngleChangedByGraphy = true;
        if (isWideAngle && getCameraId() != 2) {
            onWideAngleButtonClicked();
        } else if (!isWideAngle && getCameraId() == 2) {
            onNormalAngleButtonClicked();
        }
    }

    public double getIlluminance() {
        if (ModelProperties.isJoanRenewal()) {
            return 0.0d;
        }
        float currentAperture = 0.0f;
        if (this.mCameraDevice != null) {
            currentAperture = this.mCameraDevice.getCameraFnumber(this.mCameraDevice.getParameters());
        }
        this.mCurrentAperture = currentAperture;
        this.mIlluminance = ManualUtil.calculateIlluminance(this.mCurrentISO, this.mCurrentSS, this.mCurrentAperture);
        CamLog.m7i(CameraConstants.TAG, "[graphy][lux] mIlluminance : " + this.mIlluminance + ", mCurrentISO : " + this.mCurrentISO + ", mCurrentSS : " + this.mCurrentSS + ", mCurrentAperture : " + this.mCurrentAperture);
        return this.mIlluminance;
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (!FunctionProperties.isSupportedGraphy()) {
            return extraStr;
        }
        if (this.mGraphyViewManager != null) {
            if (this.mGraphyViewManager.getSelectedPosition() >= 0) {
                extraStr = extraStr + "graphy_selected=true;";
            } else {
                extraStr = extraStr + "graphy_selected=false;";
            }
        }
        return extraStr;
    }
}
