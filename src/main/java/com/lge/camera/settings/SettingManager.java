package com.lge.camera.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.lge.camera.components.QuickButtonType;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.PreferenceProperties;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public abstract class SettingManager {
    protected PreferenceGroup mCurPrefGroup = null;
    protected Setting mCurSetting = null;
    protected CameraSettingMenu mCurSettingMenu = null;
    protected PreferenceGroup mFrontPrefGroup = null;
    protected ArrayList<QuickButtonType> mFrontQuickSettingList = new ArrayList();
    protected Setting mFrontSetting = null;
    protected CameraSettingMenu mFrontSettingMenu = null;
    protected SettingInterface mGet = null;
    protected ArrayList<String> mOrderBackCamera = new ArrayList();
    protected ArrayList<String> mOrderCurrentSetting = new ArrayList();
    protected ArrayList<String> mOrderFrontCamera = new ArrayList();
    protected PreferenceGroup mRearPrefGroup = null;
    protected ArrayList<QuickButtonType> mRearQuickSettingList = new ArrayList();
    protected Setting mRearSetting = null;
    protected CameraSettingMenu mRearSettingMenu = null;
    protected SettingBackup mSettingBackup = new SettingBackup();

    public SettingManager(SettingInterface setting) {
        this.mGet = setting;
    }

    public void inflateSetting() {
        PreferenceInflater inflater = new PreferenceInflater(this.mGet.getAppContext());
        PreferenceGroup prefGroup = (PreferenceGroup) inflater.inflate(PreferenceProperties.getRearPreference());
        SettingVariant settingVariant = new SettingVariant();
        if (prefGroup != null) {
            this.mRearPrefGroup = prefGroup;
            settingVariant.makePreferenceVariant(this.mGet.getAppContext(), prefGroup);
        }
        prefGroup = (PreferenceGroup) inflater.inflate(PreferenceProperties.getFrontPreference());
        if (prefGroup != null) {
            this.mFrontPrefGroup = prefGroup;
            settingVariant.makePreferenceVariant(this.mGet.getAppContext(), prefGroup);
        }
    }

    public void createSetting() {
        this.mRearSetting = new Setting(this.mGet, this.mGet.getAppContext(), SharedPreferenceUtilBase.SETTING_PRIMARY, this.mRearPrefGroup);
        this.mFrontSetting = new Setting(this.mGet, this.mGet.getAppContext(), SharedPreferenceUtilBase.SETTING_SECONDARY, this.mFrontPrefGroup);
        addSetting();
        this.mRearSettingMenu = new CameraSettingMenu(this.mGet, this.mGet.getAppContext(), this.mRearSetting);
        this.mFrontSettingMenu = new CameraSettingMenu(this.mGet, this.mGet.getAppContext(), this.mFrontSetting);
        setupSetting();
    }

    protected void addSetting() {
    }

    public String getSettingDesc(String key) {
        return null;
    }

    public void setupSetting() {
        boolean isRear = isRearCamera();
        this.mCurSetting = isRear ? this.mRearSetting : this.mFrontSetting;
        this.mCurSettingMenu = isRear ? this.mRearSettingMenu : this.mFrontSettingMenu;
        this.mCurPrefGroup = isRear ? this.mRearPrefGroup : this.mFrontPrefGroup;
    }

    public boolean isRearCamera() {
        if (this.mGet.getSharedPreferenceCameraId() == 0) {
            return true;
        }
        if (FunctionProperties.getCameraTypeRear() == 1 && this.mGet.getSharedPreferenceCameraId() == 2) {
            return true;
        }
        return false;
    }

    public Setting getSpecificSetting(boolean rear) {
        return rear ? this.mRearSetting : this.mFrontSetting;
    }

    public Setting getSetting() {
        return this.mCurSetting;
    }

    public CameraSettingMenu getCameraSettingMenu() {
        return this.mCurSettingMenu;
    }

    public PreferenceGroup getPrefGroup() {
        return this.mCurPrefGroup;
    }

    public void setAllSettingMenuEnable(boolean enable) {
        if (this.mCurSettingMenu != null) {
            this.mCurSettingMenu.setAllSettingMenuEnable(enable);
        }
    }

    public void setSettingMenuEnable(String key, boolean enable) {
        if (this.mCurSettingMenu != null) {
            this.mCurSettingMenu.setParentMenuEnable(key, enable);
        }
    }

    public boolean getSettingMenuEnable(String key) {
        if (this.mCurSettingMenu != null) {
            return this.mCurSettingMenu.getParentMenuEnable(key);
        }
        return false;
    }

    public void setSettingChildMenuEnable(String key, String value, boolean enable) {
        if (this.mCurSettingMenu != null) {
            this.mCurSettingMenu.setChildMenuEnable(key, value, enable);
        }
    }

    public void setAllSettingChildMenuEnable(String key, boolean enable) {
        if (this.mCurSettingMenu != null) {
            this.mCurSettingMenu.setAllChildMenuEnable(key, enable);
        }
    }

    public String getSettingValue(String key) {
        if (this.mCurSetting == null) {
            return null;
        }
        return this.mCurSetting.getSettingValue(key);
    }

    public int getSettingIndex(String key) {
        return this.mCurSetting.getSettingIndex(key);
    }

    public boolean setSetting(int settingIndex, int settingValue, boolean save) {
        String key = this.mCurSetting.getSettingKey(settingIndex);
        ListPreference listPref = this.mCurSetting.getListPreference(key);
        if (listPref == null) {
            return false;
        }
        return setSetting(key, listPref.findValueOfIndex(settingValue), save);
    }

    public boolean setSetting(String key, String value, boolean save) {
        if (key == null || value == null || "".equals(value)) {
            return false;
        }
        if (Setting.KEY_STORAGE.equals(key)) {
            AppControlUtil.setSystemSettingUseSDcard(this.mGet.getAppContext().getContentResolver(), value);
        }
        if (Setting.KEY_SWAP_CAMERA.equals(key) || Setting.KEY_TAG_LOCATION.equals(key) || Setting.KEY_STORAGE.equals(key) || Setting.KEY_VOICESHUTTER.equals(key) || Setting.KEY_MULTIVIEW_LAYOUT.equals(key) || Setting.KEY_TIMER.equals(key) || Setting.KEY_TILE_PREVIEW.equals(key) || Setting.KEY_SIGNATURE.equals(key) || Setting.KEY_LIVE_PHOTO.equals(key) || Setting.KEY_LENS_SELECTION.equals(key) || Setting.KEY_STICKER.equals(key)) {
            return setSettingAllPreferences(key, value, save);
        }
        return this.mCurSetting.setSetting(key, value, save);
    }

    private boolean setSettingAllPreferences(String key, String value, boolean save) {
        return this.mRearSetting.setSetting(key, value, save) && this.mFrontSetting.setSetting(key, value, save);
    }

    public void setAllPreferenceApply(int which, String key, String value) {
        if ((which & 1) != 0) {
            editPrefValue(this.mGet.getActivity().getSharedPreferences("rear", 0), key, value);
            if (this.mRearSetting != null) {
                this.mRearSetting.setSetting(key, value, true);
            }
        }
        if ((which & 2) != 0) {
            editPrefValue(this.mGet.getActivity().getSharedPreferences("front", 0), key, value);
            if (this.mFrontSetting != null) {
                this.mFrontSetting.setSetting(key, value, true);
            }
        }
    }

    public boolean setForcedSetting(String key, String value) {
        if (key == null || value == null) {
            return false;
        }
        if (Setting.KEY_SWAP_CAMERA.equals(key)) {
            if (this.mRearSetting.setForcedSetting(key, value) && this.mFrontSetting.setForcedSetting(key, value)) {
                return true;
            }
            return false;
        } else if (Setting.KEY_BEAUTYSHOT.equals(key) || Setting.KEY_RELIGHTING.equals(key)) {
            return this.mCurSetting.setForcedSetting(key, value);
        } else {
            return false;
        }
    }

    public int getSelectedChildIndex(String key) {
        if (this.mCurSettingMenu != null) {
            return this.mCurSettingMenu.getSelectedChildIndex(key);
        }
        return -1;
    }

    public boolean setSelectedChild(String key, int index, boolean saveSetting) {
        if (this.mCurSettingMenu != null) {
            return this.mCurSettingMenu.setSelectedChild(key, index, saveSetting);
        }
        return false;
    }

    private void editPrefValue(SharedPreferences pref, String key, String value) {
        if (pref != null) {
            Editor editor = pref.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public void backupSetting(String key, String value) {
        if (this.mSettingBackup != null) {
            this.mSettingBackup.addBackupSetting(key, value);
        }
    }

    public void restoreBackupSetting(String key, boolean saveSetting) {
        if (this.mSettingBackup != null) {
            String backupValue = this.mSettingBackup.restoreBackupSetting(key);
            CamLog.m3d(CameraConstants.TAG, "key :  " + key + " , backupValue : " + backupValue);
            setSetting(key, backupValue, saveSetting);
        }
    }

    public void closeSetting() {
        if (this.mRearPrefGroup != null) {
            this.mRearPrefGroup.close();
            this.mRearPrefGroup = null;
        }
        if (this.mFrontPrefGroup != null) {
            this.mFrontPrefGroup.close();
            this.mFrontPrefGroup = null;
        }
        if (this.mCurPrefGroup != null) {
            this.mCurPrefGroup.close();
            this.mCurPrefGroup = null;
        }
        if (this.mSettingBackup != null) {
            this.mSettingBackup.clearBackup();
        }
    }

    private boolean isUseSpecificPictureSizeMode(String shotMode) {
        if (shotMode.contains(CameraConstants.MODE_PANORAMA) || shotMode.contains(CameraConstants.MODE_SQUARE) || CameraConstants.MODE_SNAP.equals(shotMode) || CameraConstants.MODE_SLOW_MOTION.equals(shotMode) || CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(shotMode)) {
            return true;
        }
        return false;
    }

    private int findIndexFromOtherCameraPrefrence(int defaultIndex, String shotMode, int cameraId) {
        int otherCameraId = getSubCameraId(cameraId);
        if (otherCameraId == -1) {
            return defaultIndex;
        }
        ListPreference listPref = this.mCurSetting.getListPreference(SettingKeyWrapper.getPictureSizeKey("mode_normal", otherCameraId));
        if (listPref != null) {
            return listPref.findIndexOfValue(listPref.loadSavedValue());
        }
        return defaultIndex;
    }

    public boolean updatePictureSizeListPreference(ListPreference listPref, String[] supportedPictureSize, String defaultPictureSize, int cameraId, String shotMode) {
        if (listPref == null) {
            CamLog.m11w(CameraConstants.TAG, "[ConfigAuto] listPref is null, so return");
            return false;
        }
        String[] prePreviewSizeList = (String[]) listPref.getExtraInfoByNum(1);
        String[] preScreenSizeList = (String[]) listPref.getExtraInfoByNum(2);
        if (prePreviewSizeList == null || preScreenSizeList == null) {
            CamLog.m11w(CameraConstants.TAG, "[ConfigAuto] Previous size lists are null, so return");
            return false;
        }
        int i;
        CamLog.m7i(CameraConstants.TAG, "[ConfigAuto] updatePictureSizeListPreference, cameraId : " + cameraId);
        String[] newPreviewSizeList = prePreviewSizeList;
        String[] newScreenSizeList = preScreenSizeList;
        String[] newEntry = (String[]) listPref.getEntries();
        int index = listPref.findIndexOfValue(listPref.getValue());
        if (supportedPictureSize.length >= 6 && prePreviewSizeList.length <= 4) {
            CamLog.m7i(CameraConstants.TAG, "[ConfigAuto] update ListPreference for standard picture size of main camera");
            int newIndex = 0;
            CharSequence[] newPreviewSizeList2 = new String[supportedPictureSize.length];
            CharSequence[] newScreenSizeList2 = new String[supportedPictureSize.length];
            newEntry = new String[supportedPictureSize.length];
            for (i = 0; i < prePreviewSizeList.length; i++) {
                newPreviewSizeList2[newIndex] = prePreviewSizeList[i];
                newScreenSizeList2[newIndex] = preScreenSizeList[i];
                int i2 = newIndex + 1;
                newEntry[newIndex] = (String) listPref.getEntries()[i];
                newPreviewSizeList2[i2] = prePreviewSizeList[i];
                newScreenSizeList2[i2] = preScreenSizeList[i];
                newIndex = i2 + 1;
                newEntry[i2] = (String) listPref.getEntries()[i];
            }
            listPref.setExtraInfos(newPreviewSizeList2, 1);
            listPref.setExtraInfos(newScreenSizeList2, 2);
            index *= 2;
        }
        for (i = 0; i < supportedPictureSize.length; i++) {
            newEntry[i] = newEntry[i].split(" ")[0] + " (" + Utils.getMegaPixelOfPictureSize(supportedPictureSize[i], i) + ") " + supportedPictureSize[i];
        }
        listPref.setDefaultValue(defaultPictureSize);
        listPref.setEntryValues(supportedPictureSize);
        int persistIndex = findIndexFromOtherCameraPrefrence(index, shotMode, cameraId);
        listPref.setSaveSettingEnabled(false);
        listPref.persistStringValue(supportedPictureSize[persistIndex]);
        listPref.setValue(supportedPictureSize[index]);
        if (!isUseSpecificPictureSizeMode(shotMode)) {
            listPref.setSaveSettingEnabled(true);
        }
        CamLog.m7i(CameraConstants.TAG, "[ConfigAuto] persistIndex : " + persistIndex + ", persist value : " + supportedPictureSize[persistIndex] + ", index : " + index + ", set value : " + supportedPictureSize[index]);
        listPref.setEntries(newEntry);
        updateOtherCameraPictureSize(cameraId, supportedPictureSize.length, index);
        this.mCurSettingMenu.updatePictureSizeSettingMenu(listPref, index, cameraId);
        return true;
    }

    private void updateOtherCameraPictureSize(int cameraId, int supportedListLength, int index) {
        if (supportedListLength >= 6) {
            int subCameraId = getSubCameraId(cameraId);
            if (subCameraId != -1) {
                ListPreference listPref = this.mCurSetting.getListPreference(SettingKeyWrapper.getPictureSizeKey("mode_normal", subCameraId));
                CharSequence[] previewSizeSupported = null;
                if (!(listPref == null || listPref.getExtraInfoByNum(1) == null)) {
                    previewSizeSupported = listPref.getExtraInfoByNum(1);
                }
                if (previewSizeSupported != null && previewSizeSupported.length < 6) {
                    CamLog.m7i(CameraConstants.TAG, "[ConfigAuto] update ListPreference for front sub camera");
                    setSubPictureSizeListPreference(supportedListLength, listPref);
                    this.mCurSettingMenu.updatePictureSizeSettingMenu(listPref, index, subCameraId);
                }
            }
        }
    }

    private int getSubCameraId(int cameraId) {
        if (cameraId == 0 && FunctionProperties.getCameraTypeRear() == 1) {
            return 2;
        }
        if (cameraId == 1 && FunctionProperties.getCameraTypeRear() == 1) {
            return 2;
        }
        if (cameraId == 2 && FunctionProperties.getCameraTypeRear() == 1) {
            return 0;
        }
        if (cameraId == 2 && FunctionProperties.getCameraTypeRear() == 1) {
            return 1;
        }
        return -1;
    }

    private void setSubPictureSizeListPreference(int supportedListLength, ListPreference listPref) {
        String[] prePictureSizeList = (String[]) listPref.getEntryValues();
        String[] prePreviewSizeList = (String[]) listPref.getExtraInfoByNum(1);
        String[] preScreenSizeList = (String[]) listPref.getExtraInfoByNum(2);
        if (prePictureSizeList == null || prePreviewSizeList == null || preScreenSizeList == null) {
            CamLog.m11w(CameraConstants.TAG, "[ConfigAuto] Previous size lists are null, so return");
            return;
        }
        int newIndex = 0;
        String[] newPictureSizeList = new String[supportedListLength];
        CharSequence[] newPreviewSizeList = new String[supportedListLength];
        CharSequence[] newScreenSizeList = new String[supportedListLength];
        String[] newEntry = new String[supportedListLength];
        for (int i = 0; i < prePreviewSizeList.length; i++) {
            newPictureSizeList[newIndex] = prePictureSizeList[i];
            newPreviewSizeList[newIndex] = prePreviewSizeList[i];
            newScreenSizeList[newIndex] = preScreenSizeList[i];
            int i2 = newIndex + 1;
            newEntry[newIndex] = ((String) listPref.getEntries()[i]) + " (" + Utils.getMegaPixelOfPictureSize(prePictureSizeList[i], i) + ")";
            newPictureSizeList[i2] = prePictureSizeList[i];
            newPreviewSizeList[i2] = prePreviewSizeList[i];
            newScreenSizeList[i2] = preScreenSizeList[i];
            newIndex = i2 + 1;
            newEntry[i2] = ((String) listPref.getEntries()[i]) + " (" + Utils.getMegaPixelOfPictureSize(prePictureSizeList[i], i) + ")";
        }
        listPref.setExtraInfos(newPreviewSizeList, 1);
        listPref.setExtraInfos(newScreenSizeList, 2);
        listPref.setEntries(newEntry);
        listPref.setEntryValues(newPictureSizeList);
    }

    public void setFrontFlashOff() {
        if (this.mFrontSetting != null) {
            this.mFrontSetting.setSetting("flash-mode", "off", true);
        }
    }

    public void refreshSettingByCameraId() {
    }

    public void updateSpecificSettingManue(String key, String[] entryValues, String[] entreis, int selectedPosition) {
        if (this.mCurSettingMenu != null) {
            this.mCurSettingMenu.updateSpecificSettingMenu(key, entryValues, entreis, selectedPosition);
        }
    }

    public void updateGuideTextSettingMenu(String key, String guideText) {
        if (this.mCurSettingMenu != null) {
            this.mCurSettingMenu.updateGuideTextSettingMenu(key, guideText);
        }
    }
}
